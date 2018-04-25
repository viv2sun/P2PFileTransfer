package entities.message_entities;


public enum MessageType 
{
  Choke((byte) 0),
  Unchoke((byte) 1),
  Interested((byte) 2),
  NotInterested((byte) 3),
  Have((byte) 4),
  BitField((byte) 5),
  Request((byte) 6),
  Piece((byte) 7);

  public final byte type;
  
  /*
   * Constructor of MessageType
   */
  MessageType(byte type) 
  {
    this.type = type;
  }

  /*
   * value of message type based on the number that is passed
   */
  public static MessageType valueOf(byte b) 
  {
    for (MessageType t : MessageType.values()) 
    {
      if (t.type == b) 
      {
        return t;
      }
    }
    return null;
  }
}
