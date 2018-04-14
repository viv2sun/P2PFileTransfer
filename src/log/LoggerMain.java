package log;

public class LoggerMain
{

    private final LoggerUtils logUtils;
    private final String msg; 

    public LoggerMain(int peerId) 
    {
        this (peerId, LoggerUtils.getLogger());
    }

    public LoggerMain(int pId, LoggerUtils LoggerUtils) 
    {
        msg = ": Peer " + pId;
        logUtils = LoggerUtils;
    }

    public void peerConnection(int peerId, boolean isConnectingPeer) 
    {
        final String msg = getLogMsgHeader() + (isConnectingPeer ? " makes a connection to Peer %d." : " is connected from Peer %d.");
        logUtils.info (String.format (msg, peerId));
    }

    public void changeOfPrefereedNeighbors(String preferredNeighbors) 
    {
        final String msg = getLogMsgHeader() + " has preferred neighbors %s";
        logUtils.info (String.format (msg, preferredNeighbors));
    }

    public void changeOfOptimisticallyUnchokedNeighbors(String preferredNeighbors) 
    {
        final String msg = getLogMsgHeader() + " has the optimistically unchoked neighbor %s";
        logUtils.info (String.format (msg, preferredNeighbors));
    }

    public void chokeMessage(int peerId) 
    {
        final String msg = getLogMsgHeader() + " is choked by %d.";
        logUtils.info (String.format (msg, peerId));
    }

    public void unchokeMessage(int peerId) 
    {
        final String msg = getLogMsgHeader() + " is unchoked by %d.";
        logUtils.info (String.format (msg, peerId));
    }

    public void haveMessage (int peerId, int pieceIdx) 
    {
        final String msg = getLogMsgHeader() + " received the 'have' message from %d for the piece %d.";
        logUtils.info (String.format (msg, peerId, pieceIdx));
    }

    public void interestedMessage(int peerId) 
    {
        final String msg = getLogMsgHeader() + " received the 'interested' message from %d.";
        logUtils.info (String.format (msg, peerId));
    }

    public void notInterestedMessage(int peerId) 
    {
        final String msg = getLogMsgHeader() + " received the 'not interested' message from %d.";
        logUtils.info (String.format (msg, peerId));
    }

    public void pieceDownloadedMessage(int peerId, int pieceIdx, int currNumberOfPieces) 
    {
        final String msg = getLogMsgHeader() + " has downloaded the piece %d from peer %d. Now the number of pieces it has is %d.";
        logUtils.info (String.format (msg, pieceIdx, peerId, currNumberOfPieces));
    }

    public void fileDownloadedMessage() 
    {
        final String msg = getLogMsgHeader() + " has downloaded the complete file.";
        logUtils.info (String.format (msg));
    }

    private String getLogMsgHeader() 
    {
        return (String.format (msg));
    }

}
