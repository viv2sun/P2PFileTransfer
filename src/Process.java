import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.text.DefaultEditorKit.CopyAction;

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
	
    /*
     * Constructor of Process
     */
	public Process(int id, String addr, int port, boolean hasFile, List<PeerObject> list) throws FileNotFoundException, Exception 
	{
		this.id = id;
		this.port = port;
		this.hasFile = hasFile;
		this.config = ConfigurationReader
				      .readConfigFile(new FileReader(ConfigurationReader.CONFIGURATION_FILE));
		
		int fSize = Integer.parseInt(config.getProperty(ConfigurationReader.ConfigurationParameters.FileSize.toString()));
    	String fileName = config.getProperty (ConfigurationReader.ConfigurationParameters.FileName.toString());
    	int pSize = Integer.parseInt(config.getProperty(ConfigurationReader.ConfigurationParameters.PieceSize.toString()));
    	int ucInterval = Integer.parseInt(config.getProperty(ConfigurationReader.ConfigurationParameters.UnchokingInterval.toString())) * 1000;
    	
		this.fm = new FileManager(id, fileName, fSize, pSize, ucInterval);
//		System.out.println(fileName);
		
		ArrayList<PeerObject> peerList = new ArrayList<>(list);
		for(PeerObject peer : peerList) {
			if(peer.getId()== id) {
				peerList.remove(peer);
				break;
				//check what's happening here. Being added in peerProcess and getting removed here for no reason.
			}
		}
		this.pm = new PeerManager(id, peerList, fm.getBitmapSize(), config);
		// set the files that have it already correspondingly and create event logger
		
			
		
		this.hasCompleteFile.set(hasFile);
		this.logMain = new LoggerMain(id, LoggerUtils.getLogger());
		 
	}
	
	public void init() 
	{
		fm.registerModule(this);
		pm.registerModule(this);
		
		if(hasFile) 
		{
			//first copy the file here.
//			Files.copy(source, out)
			//if the peer has the file ,create pieces of it 
			fm.splitFile();
			fm.setAllParts();
		}
		else 
		{
			//log that no file is present with type debug
			LoggerUtils.getLogger().debug("Peer doesn't have the file");
		}
		
		Thread t= new Thread(pm);
		t.setName(pm.getClass().getName());
		t.start();
	}

	/*
	 * Connecting to peer list
	 */
	public void connect(List<PeerObject> pList) 
	{
		Iterator<PeerObject> itr = pList.iterator();
        while (itr.hasNext()) 
        {
			do {
				Socket s = new Socket();
				PeerObject pObj = itr.next();
				try 
				{
					String debugMsg = "";
					
					debugMsg += " Connecting to peer: " + pObj.getId() + " (" + pObj.getAddress() + ":" 
							+ pObj.getPort() + ")";					
					
					// log connecting to peerid
					LoggerUtils.getLogger().debug(debugMsg);
					
					s = new Socket(pObj.getAddress(),pObj.getPort());
				
					if (addConnector(new Connector(id, true, pObj.getId(),s,fm, pm))) {
	                    itr.remove();
	                    debugMsg = " Connected to peer: " + pObj.getId() + " (" + pObj.getAddress() + ":" 
								+ pObj.getPort() + ")";
	                    
	                    //log connected to peer
	                    LoggerUtils.getLogger().debug(debugMsg);
	                }
				}
				catch(Exception e) 
				{					
					String debugMsg = "";
					debugMsg += " Could not be connected to Peer: " + pObj.getId() + " (" + pObj.getAddress() + ":" 
							+ pObj.getPort() + ")";	
					
					// log could not connect to exception
					LoggerUtils.getLogger().warning(debugMsg);
				}
			}
			while(itr.hasNext());
			itr = pList.iterator();
			try {
                Thread.sleep(5);
            } 
			catch (InterruptedException ex) 
			{	}
        }    
	}
	
	/*
	 * Adding connection handler
	 */
	private synchronized boolean addConnector(Connector ctr) 
	{
        if (!connectorSet.contains(ctr)) 
        {
        	connectorSet.add(ctr);
            Thread t = new Thread(ctr);
            t.start();
            try 
            {
                wait(10);
            } 
            catch (InterruptedException e) 
            {
               // log exceptions
            	LoggerUtils.getLogger().warning(e);
            }

        }
        else 
        {
        	String debugMsg = "Peer " + ctr.getPeerId() + " is trying to connect, but there is an already existing connection";
        	
             // trying to connect but connection already exists log error
        	LoggerUtils.getLogger().debug(debugMsg);
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

	/*
	 * (non-Javadoc)
	 * @see manager.IPeerManager#neighborsCompletedDownload()
	 */
    @Override
    public void finishedDownloading() 
    {
        LoggerUtils.getLogger().debug("All the peers have completely downloaded the file");
        
        peersFileCompleted.set(true);
        if(hasCompleteFile.get() && peersFileCompleted.get()) 
        {
            finished.set(true);
            System.exit(0);
        }
    }

    /*
     * (non-Javadoc)
     * @see manager.IFileManager#fileDownloaded()
     */
    @Override
    public synchronized void fileDownloaded() 
    {
        LoggerUtils.getLogger().debug("The local peer has completed downloading the file");
        this.logMain.fileDownloadedMessage();
        hasCompleteFile.set(true);
        
        if (hasCompleteFile.get() && peersFileCompleted.get()) 
        {
            finished.set(true);
            System.exit(0);
        }
    }

    /*
     * (non-Javadoc)
     * @see manager.IFileManager#partDownloaded(int)
     */
    @Override
    public synchronized void partDownloaded(int partIdx) 
    {
        for (Connector connector : connectorSet) 
        {
            connector.send(new Have(partIdx));
            
            if (!pm.isPeerInteresting(connector.getPeerId(), fm.getPartsRcvd())) 
            {
                connector.send(new NotInterested());
            }
        }
    }
    
    /*
     * (non-Javadoc)
     * @see manager.IPeerManager#chokeListOfPeers(java.util.Collection)
     */
    @Override
    public synchronized void chokeListOfPeers(Collection<Integer> chokedPeersIds) 
    {
        for (Connector connector : connectorSet) 
        {
            if (chokedPeersIds.contains(connector.getPeerId())) 
            {
                LoggerUtils.getLogger().debug("Choking " + connector.getPeerId());
                connector.send(new Choke());
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see manager.IPeerManager#unchockeListOfPeers(java.util.Collection)
     */
    @Override
    public synchronized void unchokeListOfPeers(Collection<Integer> unchokedPeersIds) 
    {
        for (Connector connector : this.connectorSet) 
        {
            if (unchokedPeersIds.contains(connector.getPeerId())) 
            {
                LoggerUtils.getLogger().debug("Unchoking " + connector.getPeerId());
                connector.send(new UnChoke());
            }
        }
    }
	
	
}