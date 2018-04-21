package manager;

import java.util.Collection;

public interface IPeerManager 
{
	//returns whether the neighbor has completely downloaded the parts  or not
    public void neighborsCompletedDownload();
    
    //Choke the list of peers
    public void chockedPeers(Collection<Integer> chokedPeersIds);
    
    //Unchoke the list of peers
    public void unchockedPeers(Collection<Integer> unchokedPeersIds);
}

