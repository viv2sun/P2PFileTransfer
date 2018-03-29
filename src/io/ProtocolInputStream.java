package io;

import entities.message_entities.*;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;

public class ProtocolInputStream extends DataInputStream implements ObjectInput{
	private boolean isHandShakeReceived = false;

    public ProtocolInputStream(InputStream inputStream) {
        super(inputStream);
    }

    @Override
    public Object readObject() throws ClassNotFoundException, IOException {
        if (isHandShakeReceived) {
            final int length = readInt();
            final int dataLength = length - 1;
            MessageTemplate message = MessageTemplate.returnNewInstance(MessageType.valueOf(readByte()), dataLength);
            message.readData(this);
            return message;
        }
        else {
            HandShakeMessageTemplate handshake = new HandShakeMessageTemplate();
            handshake.readData(this);
            isHandShakeReceived = true;
            return handshake;
        }
    }
}
