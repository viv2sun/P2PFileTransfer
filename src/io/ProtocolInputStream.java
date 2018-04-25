package io;

import entities.message_entities.*;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;

/*
 * Used to Read the data from the input stream and de-serialize it
 */
public class ProtocolInputStream extends DataInputStream implements ObjectInput
{
	private boolean isHSSignalReceived = false;

	/*
	 * Constructor of the Protocol Input Stream
	 */
  public ProtocolInputStream(InputStream inputStream) 
  {
    super(inputStream);
  }

  /*
   * (non-Javadoc)
   * @see java.io.ObjectInput#readObject()
   */
  @Override
  public Object readObject() throws ClassNotFoundException, IOException 
  {
    if (isHSSignalReceived) 
    {
      final int len = readInt();
      final int dataLength = len - 1;
      MessageTemplate message = MessageTemplate.returnNewInstance(MessageType.valueOf(readByte()), dataLength);
      message.readData(this);
      return message;
    }
    else 
    {
      HandShakeMessageTemplate handshake = new HandShakeMessageTemplate();
      handshake.readData(this);
      isHSSignalReceived = true;
      return handshake;
    }
  }
}
