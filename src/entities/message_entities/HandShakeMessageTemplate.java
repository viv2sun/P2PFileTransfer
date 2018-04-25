package entities.message_entities;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Arrays;

import io.ProtocolSerializable;

public class HandShakeMessageTemplate implements ProtocolSerializable
{
	
	private final String protocolName = "P2PFILESHARINGPROJ"; //Will be the Handshake header
	
  private final byte[] zeroBits = new byte[10];
  private byte[] peerId = new byte[4];

  /*
   * Constructor of HandShake assigning only PeerId
   */
  private HandShakeMessageTemplate(byte[] peerId) 
  {
    this.peerId = peerId;
  }
  
  /*
   * Constructor returning the byte array of peerId
   */
  public HandShakeMessageTemplate(int peerId) 
  {
    this(ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(peerId).array());
  }
  
  /*
   * Default Constructor
   */
  public HandShakeMessageTemplate() 
  {
  	
  }

  /*
   * (non-Javadoc)
   * @see io.ProtocolSerializable#readData(java.io.DataInputStream)
   * Reading the protocol name, zero bits and peerId from the input stream
   */
  @Override
  public void readData(DataInputStream inputStream) throws IOException 
  {
    // Read and check protocol Id
    byte[] protocolName = new byte[this.protocolName.length()];
    int protocolNameLength = inputStream.read(protocolName, 0, this.protocolName.length());
    int zeroBitsLength = inputStream.read(zeroBits, 0, zeroBits.length);
    int peerIdLength = inputStream.read(peerId, 0, peerId.length);
    
    if(!this.protocolName.equals(new String(protocolName, "US-ASCII")))
    	throw new ProtocolException("Invalid Protocol Name: " + protocolName);
    
    if (zeroBitsLength < zeroBits.length)
      throw new ProtocolException("Invalid Zero Bits Length: " + zeroBitsLength);

    if (peerIdLength < peerId.length) {
      throw new ProtocolException("Invalid Peer ID length: " + peerIdLength);
    }
  }  

  /*
   * (non-Javadoc)
   * @see io.ProtocolSerializable#writeData(java.io.DataOutputStream)
   * Write the protocol name, zero bits and the peerId in the output stream
   */
  @Override
  public void writeData(DataOutputStream outputStream) throws IOException 
  {
  	
    byte[] protocolNameArray = this.protocolName.getBytes(Charset.forName("US-ASCII"));
    
    outputStream.write(protocolNameArray, 0, protocolNameArray.length);
    outputStream.write(zeroBits, 0, zeroBits.length);
    outputStream.write(peerId, 0, peerId.length);
  }

  /*
   * Returns the peer Id as int from the byte array
   */
  public int returnPeerId() 
  {
  	ByteBuffer temp = ByteBuffer.wrap(this.peerId);
  	temp.order(ByteOrder.BIG_ENDIAN);
    return temp.getInt();
  }
}
