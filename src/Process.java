import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import entities.PeerObject;
import entities.message_entities.*;
import log.LoggerMain;
import log.LoggerUtils;
import utils.ConfigurationReader;
import manager.*;

public class Process implements Runnable, IFileManager, IPeerManager
{
	private final int id;
	private final int port;
	private final boolean hasFile;
	private final Properties config;
	private final FileManager fm;
	private final PeerManager pm;
    private final AtomicBoolean hasCompleteFile = new AtomicBoolean(false);
    private final AtomicBoolean peersFileCompleted = new AtomicBoolean(false);
    private final AtomicBoolean finished = new AtomicBoolean(false);
    private final LoggerMain logMain;
    private final Set<Connector> connectorSet = Collections.newSetFromMap(new ConcurrentHashMap<Connector, Boolean>());
	
	public Process(int id, String addr, int port, boolean hasFile, List<PeerObject> list) throws FileNotFoundException, Exception 
	{
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
		this.pm = new PeerManager(id, peerList, fm.getBitmapSize(), config);
		// create an event logger
		this.hasCompleteFile.set(hasFile);
		this.logMain = new LoggerMain(id);
		 
	}
	
	public void init() {
		fm.registerModule(this);
		pm.registerModule(this);
		if(hasFile) 
		{
			fm.splitFile();
			fm.setAllParts();
		}
		else 
		{
			//log that no file is present with type debug
		}
		
		Thread t= new Thread(pm);
		t.setName(pm.getClass().getName());
		t.start();
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
	
	@Override
    public void run() 
	{
        try 
        {
            ServerSocket serverSocket = new ServerSocket(this.port);
            
            while (!finished.get()) 
            {
                try 
                {
                    LoggerUtils.getLogger().debug(Thread.currentThread().getName() + ": Peer " + this.id + " listening on port " + this.port + ".");
                    addConnector(new Connector(this.id, serverSocket.accept(), fm, pm));

                } 
                catch (Exception e) 
                {
                    LoggerUtils.getLogger().warning(e);
                }
            }
        } 
        catch (IOException ex) 
        {
            LoggerUtils.getLogger().warning(ex);
        } 
        finally 
        {
            LoggerUtils.getLogger().warning(Thread.currentThread().getName()
                    + " terminating, TCP connections will no longer be accepted.");
        }
    }

    void connectToPeers(Collection<PeerObject> peersToConnectTo) 
    {
        Iterator<PeerObject> iter = peersToConnectTo.iterator();
        
        while (iter.hasNext()) 
        {
            do 
            {
                Socket socket = null;
                PeerObject peer = iter.next();
                try 
                {
                    LoggerUtils.getLogger().debug(" Connecting to peer: " + peer.getId()
                            + " (" + peer.getAddress() + ":" + peer.getPort() + ")");
                    socket = new Socket(peer.getAddress(), peer.getPort());
                    if (addConnector(new Connector(this.id, true, peer.getId(),
                            socket, fm, pm))) 
                    {
                        iter.remove();
                        LoggerUtils.getLogger().debug(" Connected to peer: " + peer.getId()
                                + " (" + peer.getAddress() + ":" + peer.getPort() + ")");

                    }
                }
                catch (ConnectException ex) 
                {
                    LoggerUtils.getLogger().warning("could not connect to peer " + peer.getId()
                            + " at address " + peer.getAddress() + ":" + peer.getPort());
                    if (socket != null) 
                    {
                        try 
                        {
                            socket.close();
                        } 
                        catch (IOException ex1)
                        {}
                    }
                }
                catch (IOException ex) 
                {
                    if (socket != null) 
                    {
                        try 
                        {
                            socket.close();
                        } 
                        catch (IOException ex1)
                        {}
                    }
                    LoggerUtils.getLogger().warning(ex);
                }
            }
            while (iter.hasNext());

            // Keep trying until they all connect
            iter = peersToConnectTo.iterator();
            try 
            {
                Thread.sleep(5);
            } 
            catch (InterruptedException ex) 
            {	}
        }
    }

    @Override
    public void neighborsCompletedDownload() 
    {
        LoggerUtils.getLogger().debug("all peers completed download");
        peersFileCompleted.set(true);
        if(hasCompleteFile.get() && peersFileCompleted.get()) 
        {
            // The process can quit
            finished.set(true);
            System.exit(0);
        }
    }

    @Override
    public synchronized void fileCompleted() 
    {
        LoggerUtils.getLogger().debug("local peer completed download");
        this.logMain.fileDownloadedMessage();
        hasCompleteFile.set(true);
        if (hasCompleteFile.get() && peersFileCompleted.get()) 
        {
            // The process can quit
            finished.set(true);
            System.exit(0);
        }
    }

    @Override
    public synchronized void pieceArrived(int partIdx) 
    {
        for (Connector connHanlder : connectorSet) 
        {
            connHanlder.send(new Have(partIdx));
            if (!pm.isPeerInteresting(connHanlder.getPeerId(), fm.getReceivedParts())) 
            {
                connHanlder.send(new NotInterested());
            }
        }
    }
    
    @Override
    public synchronized void chockedPeers(Collection<Integer> chokedPeersIds) 
    {
        for (Connector ch : connectorSet) 
        {
            if (chokedPeersIds.contains(ch.getPeerId())) 
            {
                LoggerUtils.getLogger().debug("Choking " + ch.getPeerId());
                ch.send(new Choke());
            }
        }
    }

    @Override
    public synchronized void unchockedPeers(Collection<Integer> unchokedPeersIds) 
    {
        for (Connector ch : this.connectorSet) 
        {
            if (unchokedPeersIds.contains(ch.getPeerId())) 
            {
                LoggerUtils.getLogger().debug("Unchoking " + ch.getPeerId());
                ch.send(new UnChoke());
            }
        }
    }
	
	
}