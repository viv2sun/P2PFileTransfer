package entities;

import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class PeerObject {
	private final int id;
    private final String address;
    private final int port;
    private final int hasFile;
    public AtomicInteger dwnloadedFrom;
    public BitSet rcvParts;
    private final AtomicBoolean interested;
    
    public PeerObject(int id) {
        this (id, "127.0.0.1", 0, 0);
    }

    public PeerObject(int id, String address, int port, int hasFile) {
        this.id = id;
        this.address = address;
        this.port = port;
        this.hasFile = hasFile;
        this.dwnloadedFrom = new AtomicInteger (0);
        this.rcvParts = new BitSet();
        this.interested = new AtomicBoolean (false);
    }

	public int getId() {
		return id;
	}

	public String getAddress() {
		return address;
	}

	public int getPort() {
		return port;
	}

	public boolean hasFile() {
		return (hasFile == 1);
	}

	public boolean getInterested() {
		return interested.get();
	}
	
	public void setInterestedFlag(boolean flag) {
        interested.set(flag);
    }
	
	@Override
    public boolean equals(Object o) {
		
        if (o == null) return false;
        
        if (o instanceof PeerObject) {
            return ((PeerObject) o).getId() == this.id;
        }
        
        return false;
    }

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
    @Override
    public int hashCode() {
        return Objects.hashCode(Integer.toString(id));
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
    	StringBuilder temp = new StringBuilder();
    	temp.append("Id-").append(id).append(" Address-").append(address).append(" Port-").append(port);
        return temp.toString();
    }

    /*
     * 
     */
    public static Set<Integer> getPeerIds (Collection<PeerObject> peers) {
        Set<Integer> ids = new HashSet<>();
        for (PeerObject peer : peers) {
            ids.add(peer.getId());
        }
        return ids;
    }

}
