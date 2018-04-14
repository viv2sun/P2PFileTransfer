package manager;

import java.util.Collection;

public interface IPeerManager {
	
    public void neighborsCompletedDownload();
    public void chockedPeers(Collection<Integer> chokedPeersIds);
    public void unchockedPeers(Collection<Integer> unchokedPeersIds);
}

