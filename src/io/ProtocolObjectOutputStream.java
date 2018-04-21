package io;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.OutputStream;
import entities.message_entities.*;

public class ProtocolObjectOutputStream extends DataOutputStream implements ObjectOutput 
{
	/*
	 * Constructor of the Protocol Object Output Stream
	 */
	public ProtocolObjectOutputStream(OutputStream outputStream) 
	{
        super(outputStream);
    }

	/*
	 * (non-Javadoc)
	 * @see java.io.ObjectOutput#writeObject(java.lang.Object)
	 */
    @Override
    public void writeObject(Object o) throws IOException 
    {
    	//Writes the payload ot the output stream
    	if(o instanceof MessageTemplate) 
    	{
    		MessageTemplate temp = (MessageTemplate) o;
            temp.writeData(this);
        }
    	else if(o instanceof HandShakeMessageTemplate) 
    	{
        	HandShakeMessageTemplate temp = (HandShakeMessageTemplate) o;
            temp.writeData(this);
        }
    	else 
    	{
    		throw new UnsupportedOperationException("Unsupported Message Type: " + o.getClass().getName());
    	}
    }
}
