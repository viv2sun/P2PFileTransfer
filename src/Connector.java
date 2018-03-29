import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

import io.ProtocolObjectOutputStream;


public class Connector implements Runnable {
	
	private static final int PEER_NOT_SET = -1;
	
	private final int id;
    private final Socket socket;
    private final ProtocolObjectOutputStream out;
    private final FileManager fm;
    private final PeerManager pm;
    private final boolean isConnected;
    private final int expectedpId;
    private final AtomicInteger pId;
    
	public Connector(int id, Socket s, FileManager fm, PeerManager pm) throws IOException {
	     this(id, false, -1, s, fm, pm);
	}
	
	public Connector(int id, boolean isConnected, int expectedPeerId,Socket socket, FileManager fm, PeerManager pm) 
			throws IOException {
		this.socket = socket;
		this.id = id;
		this.isConnected = isConnected;
		this.expectedpId = expectedPeerId;
		this.fm = fm;
		this.pm = pm;
		this.out = new ProtocolObjectOutputStream(socket.getOutputStream());
		this.pId = new AtomicInteger(PEER_NOT_SET);
	}
	
	public int getPeerId() {
	return pId.get();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
