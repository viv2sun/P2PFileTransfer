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

    public PeerManager(int peerId, List<PeerObject> peers, int size, Properties prop) {
        peerObjects.addAll(peers);
        maxNumberOfNeighbors = Integer.parseInt(
                prop.getProperty(ConfigurationReader.ConfigurationParameters.NumberOfPreferredNeighbors.toString()));
        intervalForUnchoking = Integer.parseInt(
                prop.getProperty(ConfigurationReader.ConfigurationParameters.UnchokingInterval.toString())) * 1000;
        unchokerManager = new UnchokerManager(prop);
        this.size = size;
        //_eventLogger = new EventLogger (peerId);
    }

    public double getIntervalForUnchoking() 
    {
        return this.intervalForUnchoking;
    }
    
    public synchronized void SetPeerInterested(int pId) 
    {
        PeerObject peer = searchPeer(pId);
        
        if (peer != null) 
        {
            peer.setInterestedFlag(true);
        }
    }

    public synchronized void SetPeerNotInterested(int pId) 
    {
        PeerObject peer = searchPeer(pId);
        
        if (peer != null) 
        {
            peer.setInterestedFlag(false);
        }
    }

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

    public synchronized void getRcvdPart(int pId, int size) 
    {
        PeerObject peer  = searchPeer(pId);
        
        if (peer != null) 
        {
            peer.dwnloadedFrom.addAndGet(size);
        }
    }

    public synchronized boolean canUploadToPeerOrNot(int pId) 
    {
        PeerObject peer = new PeerObject(pId);
        
        return (preferredSetOfPeers.contains(peer) ||
                unchokerManager.unchokedPeers.contains(peer));
    }

    public synchronized void fileCompleted() 
    {
        isRandomSelectionNeeded.set(true);
    }

    public synchronized void hasBitSetInfoArrived(int pId, BitSet bitSet) 
    {
        PeerObject peer  = searchPeer(pId);
        
        if (peer != null) 
        {
            peer.rcvParts = bitSet;
        }
        
        neighborsCompletedDownload();
    }

    public synchronized void hasPartsArrived(int pId, int partsId) 
    {
        PeerObject peer  = searchPeer(pId);
        
        if (peer != null) 
        {
            peer.rcvParts.set(partsId);
        }
        
        neighborsCompletedDownload();
    }

    public synchronized BitSet getRcvPartsOfPeer(int pId) 
    {
        PeerObject peer  = searchPeer(pId);
        
        if (peer != null) 
        {
            return (BitSet)peer.rcvParts.clone();
        }
        
        return new BitSet();
    }

    public synchronized PeerObject searchPeer(int pId) 
    {
        for (PeerObject peer : peerObjects) 
        {
            if (peer.getId() == pId) 
            {
                return peer;
            }
        }
        //LogHelper.getLogger().warning("Peer " + peerId + " not found");
        return null;
    }

    public synchronized void neighborsCompletedDownload() 
    {
        for (PeerObject peer : peerObjects) 
        {
            if (peer.rcvParts.cardinality() < size) 
            {
                //LogHelper.getLogger().debug("Peer " + peer.getPeerId() + " has not completed yet");
                return;
            }
        }
        
        for (IPeerManager listener : peerManagerModules) 
        {
            listener.neighborsCompletedDownload();
        }
    }

    public synchronized void registerModule(IPeerManager module) 
    {
        peerManagerModules.add(module);
    }
    
	class UnchokerManager extends Thread {
		
        private final int noOfUnchokedNeighbors;
        private final int unchokingInterval;
        private final List<PeerObject> chokedPeers = new ArrayList<>();
        
        final Set<PeerObject>  unchokedPeers =
                Collections.newSetFromMap(new ConcurrentHashMap<PeerObject, Boolean>());

        UnchokerManager(Properties conf) 
        {
            super("UnchokerManager");
            noOfUnchokedNeighbors = 1;
            unchokingInterval = Integer.parseInt(
                    conf.getProperty(ConfigurationReader.ConfigurationParameters.NumberOfPreferredNeighbors.toString())) * 1000;
        }

        synchronized void SetChokedPeers(Collection<PeerObject> chokedPeers) {
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
                    /*LogHelper.getLogger().debug("STATE: OPT UNCHOKED(" + _numberOfOptimisticallyUnchokedNeighbors + "):" + LogHelper.getPeerIdsAsString (_optmisticallyUnchokedPeers));
                    _eventLogger.changeOfOptimisticallyUnchokedNeighbors(LogHelper.getPeerIdsAsString (_optmisticallyUnchokedPeers));*/
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
                //LogHelper.getLogger().debug("selecting preferred peers randomly");
                Collections.shuffle(peersInterested);
            }
            else 
            {
                Collections.sort(peersInterested, (o1, o2) -> {
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

                // 2) SELECT THE PREFERRED PEERS BY SELECTING THE HIGHEST RANKED
                // Select the highest ranked neighbors as "preferred"
                preferredSetOfPeers.clear();
                preferredSetOfPeers.addAll(peersInterested.subList(0, Math.min(maxNumberOfNeighbors, peersInterested.size())));
                
                if (preferredSetOfPeers.size() > 0) 
                {
                    //_eventLogger.changeOfPrefereedNeighbors(LogHelper.getPeerIdsAsString (_preferredPeers));
                }

                // 3) SELECT ALL THE INTERESTED AND UNINTERESTED PEERS, REMOVE THE PREFERRED. THE RESULTS ARE THE CHOKED PEERS
                List<PeerObject> peersChoked = new LinkedList<>(peerObjects);
                peersChoked.removeAll(preferredSetOfPeers);
                peersChokedIds.addAll(PeerObject.getPeerIds(peersChoked));

                // 4) SELECT ALLE THE INTERESTED PEERS, REMOVE THE PREFERRED. THE RESULTS ARE THE CHOKED PEERS THAT ARE "OPTIMISTICALLY-UNCHOKABLE"
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
//            LogHelper.getLogger().debug("STATE: INTERESTED:" + LogHelper.getPeerIdsAsString (interestedPeers));
//            LogHelper.getLogger().debug("STATE: UNCHOKED (" + _numberOfPreferredNeighbors + "):" + LogHelper.getPeerIdsAsString2 (preferredNeighborsIDs));
//            LogHelper.getLogger().debug("STATE: CHOKED:" + LogHelper.getPeerIdsAsString2 (chokedPeersIDs));
            
            for (Entry<Integer,Long> entry : dwnloadedBytes.entrySet()) 
            {
                String pref = preferredNeighIds.contains(entry.getKey()) ? " *" : "";
                
                /*LogHelper.getLogger().debug("BYTES DOWNLOADED FROM  PEER " + entry.getKey() + ": "
                        + entry.getValue() + " (INTERESTED PEERS: "
                        + interestedPeers.size()+ ": " + LogHelper.getPeerIdsAsString (interestedPeers)
                        + ")\t" + PREFERRED);*/
            }

            // 5) NOTIFY PROCESS, IT WILL TAKE CARE OF SENDING CHOKE AND UNCHOKE MESSAGES
            for (IPeerManager module : peerManagerModules) 
            {
                module.chockedPeers(peersChokedIds);
                module.unchockedPeers(preferredNeighIds);
            }
            
            // 6) NOTIFY THE OPTIMISTICALLY UNCHOKER THREAD WITH THE NEW SET OF UNCHOKABLE PEERS

            if (unchokedPeers != null) {
                unchokerManager.SetChokedPeers(unchokedPeers);
            }
        }
	}
}
