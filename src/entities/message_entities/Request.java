package entities.message_entities;

public class Request extends MessageTemplate 
{
	/*
	 * Constructor for the Request class with the piece index array
	 */
	public Request(byte[] pieceIndexArray) 
	{
    super(MessageType.Request, pieceIndexArray);
  }

	/*
	 * Constructor for request with the pieceIndex as Integer
	 */
  public Request (int pieceIndex) 
  {
    this(getPieceIndexByteArray(pieceIndex));
  }
}
