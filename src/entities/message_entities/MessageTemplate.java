package entities.message_entities;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import entities.message_entities.*;
import io.ProtocolSerializable;

public class MessageTemplate implements ProtocolSerializable{
	public int length;
    public final MessageType type;
    public byte[] payload;

    public MessageTemplate(MessageType type) {
        this (type, null);
    }

    public MessageTemplate(MessageType type, byte[] payload) {
        this.length = (payload == null) ? 1 : payload.length + 1;
        this.type = type;
        this.payload = payload;
    }

    @Override
    public void readData(DataInputStream in) throws IOException {
        if (payload != null) {
            in.readFully(payload, 0, length - 1);
        }
    }

    @Override
    public void writeData(DataOutputStream out) throws IOException {
        out.writeInt (this.length);
        out.writeByte (type.value());
        if (payload != null) {
            out.write (payload, 0, length - 1);
        }
    }
    
    public int getPieceIndex() {
    	ByteBuffer temp = ByteBuffer.wrap(payload, 0, 4);
    	temp = temp.order(ByteOrder.BIG_ENDIAN);
        return temp.getInt();
    }

    protected static byte[] getPieceIndexByteArray (int pieceIndex) {
    	ByteBuffer temp = ByteBuffer.allocate(4);
    	temp = temp.order(ByteOrder.BIG_ENDIAN);
    	temp.putInt(pieceIndex);
        return temp.array();
    }
    
    public MessageType getType() {
        return type;
    }
    
    public static MessageTemplate returnNewInstance(MessageType type, int dataLength) throws ClassNotFoundException, IOException {
        switch(type) {
            case Choke: return new Choke();
            case Unchoke: return new UnChoke();
            case Interested: return new Interested();
            case NotInterested: return new NotInterested();
            case Have: return new Have(new byte[dataLength]);
            case BitField: return new BitField(new byte[dataLength]);
            case Request: return new Request(new byte[dataLength]);
            case Piece: return new Piece(new byte[dataLength]);
            default: throw new ClassNotFoundException("Invalid message type: " + type.toString());
        }
    }
    
}
