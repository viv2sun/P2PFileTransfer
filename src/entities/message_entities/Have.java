package entities.message_entities;

public class Have extends MessageTemplate
{
	/*
	 * Constructor of the have message with the pieceIndex as the payload
	 */
	public Have(byte[] pieceIndex) 
	{
    super(MessageType.Have, pieceIndex);
  }

	/*
	 * Constructor of the Have message with int as the parameter
	 */
  public Have(int pieceIndex) 
  {
    this(getPieceIndexByteArray(pieceIndex));
  }
}
