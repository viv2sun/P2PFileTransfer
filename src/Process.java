import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import entities.PeerObject;
import utils.ConfigurationReader;

public class Process implements Runnable{
	private final int id;
	private final int port;
	private final boolean hasFile;
	private final Properties config;
	private final FileManager fm;
	private final PeerManager pm;
    private final AtomicBoolean hasCompleteFile = new AtomicBoolean(false);
    private final AtomicBoolean finished = new AtomicBoolean(false);
    private final Set<Connector> connectorSet = Collections.newSetFromMap(new ConcurrentHashMap<Connector, Boolean>());
	
	public Process(int id, String addr, int port, boolean hasFile, List<PeerObject> list) throws FileNotFoundException, Exception {
		this.id = id;
		this.port = port;
		this.hasFile = hasFile;
		this.config = ConfigurationReader
				      .readConfigFile(new FileReader(ConfigurationReader.CONFIGURATION_FILE));
		this.fm = new FileManager(id, config);
		ArrayList<PeerObject> peerList = new ArrayList<>(list);
		for(PeerObject peer : peerList) {
			if(peer.getId()== id) {
				list.remove(id);
				break;
				//check what's happening here. Being added in peerProcess and getting removed here for no reason.
			}
		}
		this.pm = new PeerManager(id, peerList, fm.getMapSize(), config);
		// create an event logger
		this.hasCompleteFile.set(hasFile);
		 
	}
	
	public void init() {
		fm.register(this);
		pm.register(this);
		if(hasFile) {
			//log and split the file
			fm.splitFile();
			fm.setAllBitField();
		}
		else {
			//log that no file is present with type debug
		}
		Thread t= new Thread(pm);
		t.setName(pm.getClass().getName());
		t.start();
	}

	@Override
	public void run() {
		try {
			ServerSocket socket = new ServerSocket(port);
			while(!finished.get()) {
				// log name id and port
                addConnector(new Connector(id, socket.accept(), fm, pm));
			}
		} catch (IOException e) {
			//log exception with warning
			e.printStackTrace();
		}
	}

	public void connect(List<PeerObject> pList) {
		Iterator<PeerObject> itr = pList.iterator();
        while (itr.hasNext()) {
			do {
				Socket s = new Socket();
				PeerObject pObj = itr.next();
				try {
					// log debug connecting to peerid and port 
					s = new Socket(pObj.getAddress(),pObj.getPort());
					if (addConnector(new Connector(id, true, pObj.getId(),s,fm, pm))) {
	                    itr.remove();
	                    //log connected to peer
	                }
				}
				catch(Exception e) {
					// log could not connect to exception
				}
			}
			while(itr.hasNext());
			itr = pList.iterator();
			try {
                Thread.sleep(5);
            } 
			catch (InterruptedException ex) {
            }
        }    
	}
	
	
	private synchronized boolean addConnector(Connector ctr) {
        if (!connectorSet.contains(ctr)) {
        	connectorSet.add(ctr);
            Thread t = new Thread(ctr);
            t.start();
            try {
                wait(10);
            } catch (InterruptedException e) {
               // log exceptions
            }

        }
        else {
             // trying to connect but connection already exists log error
        }
        return true;
    }
	
	
}