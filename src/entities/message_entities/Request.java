package entities.message_entities;

public class Request extends MessageTemplate {
	
	public Request(byte[] pieceIndexArray) {
        super(MessageType.Request, pieceIndexArray);
    }

    public Request (int pieceIndex) {
        this(getPieceIndexByteArray(pieceIndex));
    }
}
