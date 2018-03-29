package entities.message_entities;

public class Have extends MessageTemplate{
	
	public Have(byte[] pieceIndex) {
        super(MessageType.Have, pieceIndex);
    }

    public Have(int pieceIndex) {
        this(getPieceIndexByteArray(pieceIndex));
    }
}
