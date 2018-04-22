package entities.message_entities;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import entities.message_entities.*;
import io.ProtocolSerializable;

/*
 * Message Template class is the base class of all the messages
 */
public class MessageTemplate implements ProtocolSerializable
{
	public int length;
    public final MessageType type;
    public byte[] payload;

    /*
     * Constructor of Message Template with their type and their payloads
     */
    public MessageTemplate(MessageType type, byte[] payload) 
    {
        this.length = (payload == null) ? 1 : payload.length + 1;
        this.type = type;
        this.payload = payload;
    }

    /*
     * (non-Javadoc)
     * @see io.ProtocolSerializable#readData(java.io.DataInputStream)
     * Read the payload from the input stream
     */
    @Override
    public void readData(DataInputStream in) throws IOException 
    {
    	if (payload != null && payload.length > 0) 
        {
    		in.readFully(payload, 0, payload.length);
        }
    }

    /*
     * (non-Javadoc)
     * @see io.ProtocolSerializable#writeData(java.io.DataOutputStream)
     * 
     * Writes the message payload to the output stream
     */
    @Override
    public void writeData(DataOutputStream out) throws IOException 
    {
        out.writeInt(this.length);
        out.writeByte(this.type.type);
        
        if (payload != null && payload.length > 0) 
        {
            out.write(payload, 0, payload.length);
        }
    }
    
    /*
     * Get the piece Index from the first 4 bits of payload
     */
    public int getPieceIndex() 
    {
    	ByteBuffer temp = ByteBuffer.wrap(Arrays.copyOfRange(payload, 0, 4));
    	temp = temp.order(ByteOrder.BIG_ENDIAN);
        return temp.getInt();
    }

    /*
     * Receive the piece Index as the byte array
     */
    protected static byte[] getPieceIndexByteArray (int pieceIndex) 
    {
    	ByteBuffer temp = ByteBuffer.allocate(4);
    	temp = temp.order(ByteOrder.BIG_ENDIAN);
    	temp.putInt(pieceIndex);
        return temp.array();
    }
    
    /*
     * Creates the new Instance of the message, given it's type and the payload length
     */
    public static MessageTemplate returnNewInstance(MessageType type, int dataLength) throws ClassNotFoundException, IOException 
    {
        switch(type) 
        {
            case Choke: return new Choke();
            case Unchoke: return new UnChoke();
            case Interested: return new Interested();
            case NotInterested: return new NotInterested();
            case Have: return new Have(new byte[dataLength]);
            case BitField: return new BitField(new byte[dataLength]);
            case Request: return new Request(new byte[dataLength]);
            case Piece: return new Piece(new byte[dataLength]);
            default: return null;
        }
    }
    
}
