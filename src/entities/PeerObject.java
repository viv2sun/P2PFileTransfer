package entities;

import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class PeerObject 
{
	
	private final int id;
    private final String address;
    private final int port;
    private final int hasFile;
    public AtomicInteger dwnloadedFrom;
    public BitSet rcvParts;
    private final AtomicBoolean interested;
    
    /*
     * Constructor of the Peer Object
     */
    public PeerObject(int id) 
    {
        this (id, "127.0.0.3", 0, 0);
    }

    /*
     * Parameterized constructor of the peer object
     */
    public PeerObject(int id, String address, int port, int hasFile) {
        this.id = id;
        this.address = address;
        this.port = port;
        this.hasFile = hasFile;
        this.dwnloadedFrom = new AtomicInteger (0);
        this.rcvParts = new BitSet();
        this.interested = new AtomicBoolean (false);
    }

    /*
     * Returns the PeerId
     */
	public int getId() 
	{
		return id;
	}

	/*
	 * Returns the IP address of the peer
	 */
	public String getAddress() 
	{
		return address;
	}

	/*
	 * Returns the port number of the peer
	 */
	public int getPort() 
	{
		return port;
	}

	/*
	 * returns whether the peer contains the file or not
	 */
	public boolean hasFile() 
	{
		return (hasFile == 1);
	}

	/*
	 * returns the interested flag
	 */
	public boolean getInterested() 
	{
		return interested.get();
	}
	
	/*
	 * Sets the interested flag to a value
	 */
	public void setInterestedFlag(boolean flag) 
	{
        interested.set(flag);
    }
	
	@Override
    public boolean equals(Object o) 
	{
		PeerObject obj = (PeerObject)o;
		
        if (obj == null) 
        {
        	return false;
        }
        
        return obj.getId() == this.id;
    }

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
    @Override
    public int hashCode() 
    {
        return this.id;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
    	StringBuilder temp = new StringBuilder();
    	temp.append("PeerId-").append(id).append(" Address-").append(address).append(" Port-").append(port);
        return temp.toString();
    }

    /*
     * Returns the set of Peer Ids
     */
    public static Set<Integer> getPeerIds (Collection<PeerObject> peers) 
    {
        Set<Integer> ids = new HashSet<>();
        for (PeerObject peer : peers) {
            ids.add(peer.getId());
        }
        return ids;
    }

}
