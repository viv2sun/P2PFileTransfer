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

public class HandShakeMessageTemplate implements ProtocolSerializable{
	
	private final String protocolName = "P2PFILESHARINGPROJ";
    private final byte[] zeroBits = new byte[10];
    private byte[] peerId = new byte[4];

    private HandShakeMessageTemplate(byte[] peerId) {
        if (peerId.length <= 4) {
        	this.peerId = peerId;
        }
        else {
        	throw new ArrayIndexOutOfBoundsException("The max length of peerID is 4. The length is: " + peerId.length);
        }
    }
    
    public HandShakeMessageTemplate(int peerId) {
        this(ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(peerId).array());
    }
    
    public HandShakeMessageTemplate() {
    	
    }

    @Override
    public void readData(DataInputStream inputStream) throws IOException {
        // Read and check protocol Id
        byte[] protocolName = new byte[this.protocolName.length()];
        int protocolNameLength = inputStream.read(protocolName, 0, this.protocolName.length());
        int zeroBitsLength = inputStream.read(zeroBits, 0,  zeroBits.length);
        int peerIdLength = inputStream.read(peerId, 0, peerId.length);
        
        if(!protocolName.equals(this.protocolName))
        	throw new ProtocolException("Invalid Protocol Name: " + protocolName);
        
        if (zeroBitsLength < zeroBits.length)
            throw new ProtocolException("Invalid Zero Bits Length: " + zeroBitsLength);

        if (peerIdLength < peerId.length) {
            throw new ProtocolException("Invalid Peer ID length:  " + peerIdLength);
        }
    }    

    @Override
    public void writeData(DataOutputStream outputStream) throws IOException {
    	
        byte[] protocolNameArray = this.protocolName.getBytes(Charset.forName("US-ASCII"));
        
        outputStream.write(protocolNameArray, 0, protocolNameArray.length);
        outputStream.write(zeroBits, 0, zeroBits.length);
        outputStream.write(peerId, 0, peerId.length);
    }

    public int returnPeerId() {
    	ByteBuffer temp = ByteBuffer.wrap(this.peerId);
    	temp.order(ByteOrder.BIG_ENDIAN);
        return temp.getInt();
    }
}
