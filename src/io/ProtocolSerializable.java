package io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface ProtocolSerializable 
{
	public void readData(DataInputStream in) throws IOException;
    public void writeData(DataOutputStream out) throws IOException;
}
