package io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface ProtocolSerializable 
{
	//reads the data from the input stream
	public void readData(DataInputStream in) throws IOException;
	
	//writes the data back to the output stream
  public void writeData(DataOutputStream out) throws IOException;
}
