import java.util.BitSet;

import manager.*;
import entities.*;
import entities.message_entities.*;
import log.*;


public class MessageHandler 
{
    private boolean isPeerChoked;
    private final int remPeerId;
    private final FileManager fm;
    private final PeerManager pm;
    private final LoggerMain logMain;

    MessageHandler(int remPeerId, FileManager fm, PeerManager pm, LoggerMain logMain) 
    {
        isPeerChoked = true;
        this.fm = fm;
        this.pm = pm;
        this.remPeerId = remPeerId;
        this.logMain = logMain;
    }

    public MessageTemplate handle(HandShakeMessageTemplate handShake) 
    {
        BitSet bitset = fm.getReceivedParts();
        if(!bitset.isEmpty()) 
        {
            return (new BitField(bitset));
        }
        return null;
    }

    public MessageTemplate handle(MessageTemplate msg) 
    {
        switch(msg.type) 
        {
            case Choke: 
            {
                isPeerChoked = true;
                logMain.chokeMessage(remPeerId);
                return null;
            }
            case Unchoke: 
            {
                isPeerChoked = false;
                logMain.unchokeMessage(remPeerId);                
                return requestPiece();
            }
            case Interested: 
            {
                logMain.interestedMessage(remPeerId);
                pm.SetPeerInterested(remPeerId);
                return null;
            }
            case NotInterested: 
            {
                logMain.notInterestedMessage(remPeerId);
                pm.SetPeerNotInterested(remPeerId);
                return null;
            }
            case Have: 
            {
                Have have = (Have) msg;
                final int pieceId = have.getPieceIndex();
                logMain.haveMessage(remPeerId, pieceId);
                pm.hasPartsArrived(remPeerId, pieceId);

                if(fm.getReceivedParts().get(pieceId)) 
                {
                    return new NotInterested();
                }
                else 
                {
                    return new Interested();
                }
            }
            case BitField: 
            {
                BitField bitfield = (BitField) msg;
                BitSet bitset = bitfield.getBitSet();
                pm.hasBitSetInfoArrived(remPeerId, bitset);

                bitset.andNot(fm.getReceivedParts());
                if (bitset.isEmpty()) 
                {
                    return new NotInterested();
                } 
                else 
                {
                    // the peer has parts that this peer does not have
                    return new Interested();
                }
            }
            case Request: 
            {
                Request request = (Request) msg;
                if (pm.canUploadToPeerOrNot(remPeerId)) 
                {
                    byte[] piece = fm.getPiece(request.getPieceIndex());
                    if (piece != null) 
                    {
                        return new Piece(request.getPieceIndex(), piece);
                    }
                }
                return null;
            }
            case Piece: 
            {
                Piece piece = (Piece) msg;
                fm.addPart(piece.getPieceIndex(), piece.getData());
                pm.getRcvdPart(remPeerId, piece.getData().length);
                logMain.pieceDownloadedMessage(remPeerId, piece.getPieceIndex(), fm.getNumberOfReceivedParts());
                return requestPiece();
            }
        }

        return null;
    }

    private MessageTemplate requestPiece() 
    {
        if (!isPeerChoked) 
        {
            int partId = fm.identifyParts(pm.getRcvPartsOfPeer(remPeerId));
            if (partId >= 0) 
            {
                LoggerUtils.getLogger().debug("Requesting part " + partId + " to " + remPeerId);
                return new Request (partId);
            }
            else 
            {
                LoggerUtils.getLogger().debug("No parts can be requested to " + remPeerId);
            }
        } 
        return null;
    }

}
