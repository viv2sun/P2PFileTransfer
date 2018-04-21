package manager;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import utils.ConfigurationReader;
import entities.PeerObject;
import log.LoggerMain;
import log.LoggerUtils;

public class PeerManager implements Runnable 
{
    private final int size;
	private final int maxNumberOfNeighbors;
    private final List<PeerObject> peerObjects = new ArrayList<>();
    private final int intervalForUnchoking;
    private final Set<PeerObject> preferredSetOfPeers = new HashSet<>();
    private final UnchokerManager unchokerManager;
    private final List<IPeerManager> peerManagerModules = new LinkedList<>();
    private final AtomicBoolean isRandomSelectionNeeded = new AtomicBoolean(false);
    public LoggerMain logger;

    /*
     * Constructor of the peer manager
     */
    public PeerManager(int peerId, List<PeerObject> peers, int size, Properties prop) 
    {
        peerObjects.addAll(peers);
        maxNumberOfNeighbors = Integer.parseInt(
                prop.getProperty(ConfigurationReader.ConfigurationParameters.NumberOfPreferredNeighbors.toString()));
        intervalForUnchoking = Integer.parseInt(
                prop.getProperty(ConfigurationReader.ConfigurationParameters.UnchokingInterval.toString())) * 1000;
        unchokerManager = new UnchokerManager(prop);
        this.size = size;
        logger = new LoggerMain(peerId, LoggerUtils.getLogger());
    }

    /*
     * Get the interval for unchoking the peer
     */
    public double getIntervalForUnchoking() 
    {
        return this.intervalForUnchoking;
    }
    
    /*
     * Given a peerId, set it's interested flag
     */
    public synchronized void SetPeerInterested(int pId) 
    {
        PeerObject peer = searchPeer(pId);
        
        if (peer != null) 
        {
            peer.setInterestedFlag(true);
        }
    }

    /*
     * Given a peer Id, set it's interested flag to false
     */
    public synchronized void SetPeerNotInterested(int pId) 
    {
        PeerObject peer = searchPeer(pId);
        
        if (peer != null) 
        {
            peer.setInterestedFlag(false);
        }
    }

    /*
     * Retrieve the list of interested peers
     */
    public synchronized List<PeerObject> getAllInterestedPeers() 
    {
        List<PeerObject> peersInterested = new ArrayList<>();
        
        for (PeerObject peer : peerObjects)
        {
            if(peer.getInterested())
            {
                peersInterested.add(peer);
            }
        }
        return peersInterested;
    }

    /*
     * Determine whether the peer is interesting based on it's bitfield
     */
    public synchronized boolean isPeerInteresting(int pId, BitSet bitSet) 
    {
        PeerObject peer  = searchPeer(pId);
        
        if (peer != null) 
        {
            BitSet currBitSet = (BitSet) peer.rcvParts.clone();
            currBitSet.andNot(bitSet);
            return !currBitSet.isEmpty();
        }
        
        return false;
    }

    /*
     * Set the parts received for peer
     */
    public synchronized void getRcvdPart(int pId, int size) 
    {
        PeerObject peer  = searchPeer(pId);
        
        if (peer != null) 
        {
            peer.dwnloadedFrom.addAndGet(size);
        }
    }

    /*
     * returns whether the peer can upload or not
     */
    public synchronized boolean canUploadToPeerOrNot(int pId) 
    {
        PeerObject peer = new PeerObject(pId);
        
        return (preferredSetOfPeers.contains(peer) ||
                unchokerManager.unchokedPeers.contains(peer));
    }

    /*
     * Starts the random selection as soon as the file is completed
     */
    public synchronized void fileCompleted() 
    {
        isRandomSelectionNeeded.set(true);
    }

    /*
     * Determines whether the bitfield for the peer has arrived
     */
    public synchronized void hasBitSetInfoArrived(int pId, BitSet bitSet) 
    {
        PeerObject peer  = searchPeer(pId);
        
        if (peer != null) 
        {
            peer.rcvParts = bitSet;
        }
        
        neighborsCompletedDownload();
    }

    /*
     * Determine whether the parts have arrived for the peer
     */
    public synchronized void hasPartsArrived(int pId, int partsId) 
    {
        PeerObject peer  = searchPeer(pId);
        
        if (peer != null) 
        {
            peer.rcvParts.set(partsId);
        }
        
        neighborsCompletedDownload();
    }

    /*
     * Get parts bitfield of the peer
     */
    public synchronized BitSet getRcvPartsOfPeer(int pId) 
    {
        PeerObject peer  = searchPeer(pId);
        
        if (peer != null) 
        {
            return (BitSet)peer.rcvParts.clone();
        }
        
        return new BitSet();
    }

    /*
     * Search a peer and return it
     */
    public synchronized PeerObject searchPeer(int pId) 
    {
        for (PeerObject peer : peerObjects) 
        {
            if (peer.getId() == pId) 
            {
                return peer;
            }
        }
        
        LoggerUtils.getLogger().warning("Peer " + pId + " not found");        
        return null;
    }

    /*
     * determines whether the neighbors have completed the download or not
     */
    public synchronized void neighborsCompletedDownload() 
    {
        for (PeerObject peer : peerObjects) 
        {
            if (peer.rcvParts.cardinality() < size) 
            {
                LoggerUtils.getLogger().debug("The Peer: " + peer.getId() + " has not completed the download");
                return;
            }
        }
        
        for (IPeerManager listener : peerManagerModules) 
        {
            listener.neighborsCompletedDownload();
        }
    }

    /*
     * Registering the peer manager module
     */
    public synchronized void registerModule(IPeerManager module) 
    {
        peerManagerModules.add(module);
    }
    
	class UnchokerManager extends Thread 
	{
		
        private final int noOfUnchokedNeighbors;
        private final int unchokingInterval;
        private final List<PeerObject> chokedPeers = new ArrayList<>();
        
        final Set<PeerObject>  unchokedPeers =
                Collections.newSetFromMap(new ConcurrentHashMap<PeerObject, Boolean>());

        /*
         * Constructor of the Unchoker Manager
         */
        UnchokerManager(Properties conf) 
        {
            super("UnchokerManager");
            noOfUnchokedNeighbors = 1;
            unchokingInterval = Integer.parseInt(
                    conf.getProperty(ConfigurationReader.ConfigurationParameters.NumberOfPreferredNeighbors.toString())) * 1000;
        }

        /*
         * Set a new list of choked peers
         */
        synchronized void SetChokedPeers(Collection<PeerObject> chokedPeers) 
        {
            chokedPeers.clear();
            chokedPeers.addAll(chokedPeers);
        }

        @Override
        public void run() 
        {
            while (true) 
            {
                try 
                {
                    Thread.sleep(unchokingInterval);
                } 
                catch (InterruptedException ex) 
                { }

                synchronized (this) 
                {
                    //Randomly select a peer to unchoke
                    if (!chokedPeers.isEmpty()) 
                    {
                        Collections.shuffle(chokedPeers);
                        unchokedPeers.clear();
                        unchokedPeers.addAll(chokedPeers.subList(0,
                                Math.min(noOfUnchokedNeighbors, chokedPeers.size())));
                    }
                }

                if (chokedPeers.size() > 0) 
                {
                	String debugMsg = "State: Optimistically Unchoked(";
                	debugMsg += noOfUnchokedNeighbors + "):" +  LoggerUtils.returnPeerIdStringFromObject(new ArrayList(unchokedPeers));
                    LoggerUtils.getLogger().debug(debugMsg);
                    logger.optimisticallyUnchokedNeighborsChangeLog(LoggerUtils.returnPeerIdStringFromObject((new ArrayList(unchokedPeers))));
                }
                for (IPeerManager module : peerManagerModules) 
                {
                    module.unchockedPeers(PeerObject.getPeerIds(unchokedPeers));
                }
            }
        }
    }
	
	@Override
    public void run() 
	{

        unchokerManager.start();

        while (true) 
        {
            try 
            {
                Thread.sleep(intervalForUnchoking);
            } 
            catch (InterruptedException ex) 
            {	}

            List<PeerObject> peersInterested = getAllInterestedPeers();
            
            if (isRandomSelectionNeeded.get()) 
            {
                LoggerUtils.getLogger().debug("Randomly selecting the peers");
                Collections.shuffle(peersInterested);
            }
            else 
            {
                Collections.sort(peersInterested, (o1, o2) -> 
                {
				    PeerObject p1 = (PeerObject)o1;
				    PeerObject p2 = (PeerObject)o2;
				    
				    return (p2.dwnloadedFrom.get() - p1.dwnloadedFrom.get());
				});
            }

            List<PeerObject> unchokedPeers = null;

            Set<Integer> peersChokedIds = new HashSet<>();
            Set<Integer> preferredNeighIds = new HashSet<>();
            Map<Integer, Long> dwnloadedBytes = new HashMap<>();

            synchronized (this) 
            {                
                for (PeerObject peer : peerObjects) 
                {
                    dwnloadedBytes.put(peer.getId(), peer.dwnloadedFrom.longValue());
                    peer.dwnloadedFrom.set(0);
                }

                preferredSetOfPeers.clear();
                preferredSetOfPeers.addAll(peersInterested.subList(0, Math.min(maxNumberOfNeighbors, peersInterested.size())));
                
                if (preferredSetOfPeers.size() > 0) 
                {
                    logger.preferredNeighborChangeLog(LoggerUtils.returnPeerIdStringFromObject(new ArrayList(preferredSetOfPeers)));
                }

                
                List<PeerObject> peersChoked = new LinkedList<>(peerObjects);
                peersChoked.removeAll(preferredSetOfPeers);
                peersChokedIds.addAll(PeerObject.getPeerIds(peersChoked));

                
                if (maxNumberOfNeighbors >= peersInterested.size()) 
                {
                    unchokedPeers = new ArrayList<>();
                }
                else 
                {
                    unchokedPeers = peersInterested.subList(maxNumberOfNeighbors, peersInterested.size());
                }

                preferredNeighIds.addAll (PeerObject.getPeerIds(preferredSetOfPeers));
            }

            // debug
            LoggerUtils.getLogger().debug("State: Unchoked (" + this.maxNumberOfNeighbors + "):" + LoggerUtils.returnPeerIdString(new ArrayList(preferredNeighIds)));
            LoggerUtils.getLogger().debug("State: Choked:" + LoggerUtils.returnPeerIdStringFromObject(new ArrayList(peersChokedIds)));
           LoggerUtils.getLogger().debug("State: Intersted (" + LoggerUtils.returnPeerIdStringFromObject(new ArrayList(peersInterested)));
            
            for (Entry<Integer,Long> entry : dwnloadedBytes.entrySet()) 
            {
                String pref = preferredNeighIds.contains(entry.getKey()) ? " *" : "";
                
                LoggerUtils.getLogger().debug("BYTES DOWNLOADED FROM  PEER " + entry.getKey() + ": "
                        + entry.getValue() + " (INTERESTED PEERS: "
                        + peersInterested.size()+ ": " + LoggerUtils.returnPeerIdStringFromObject(peersInterested)
                        + ")\t" + pref);
            }
            
            for (IPeerManager module : peerManagerModules) 
            {
                module.chockedPeers(peersChokedIds);
                module.unchockedPeers(preferredNeighIds);
            }
                        
            if (unchokedPeers != null) 
            {
                unchokerManager.SetChokedPeers(unchokedPeers);
            }
        }
	}
}
