package entities.message_entities;

import java.util.Arrays;

public class Piece extends MessageTemplate{

    public Piece(int pieceIndex, byte[] data) {
        super (MessageType.Piece, combinePieceIndexAndData(data, pieceIndex));
    }
    
	public Piece(byte[] payload) {
        super(MessageType.Piece, payload);
    }

    public byte[] getData() {
        if(payload != null && payload.length > 4)
        	return Arrays.copyOfRange(payload, 4, payload.length);
        return null;
    }

    private static byte[] combinePieceIndexAndData(byte[] data, int pieceIndex) {
    	int length = 4 + ((data != null) ? data.length : 0);
        byte[] wholeContent = new byte[length];
        byte[] pieceIndexArray = getPieceIndexByteArray(pieceIndex);
        System.arraycopy(pieceIndexArray, 0, wholeContent, 0, 4);
        System.arraycopy(data, 0, wholeContent, 4, data.length);
        return wholeContent;
    }
}
