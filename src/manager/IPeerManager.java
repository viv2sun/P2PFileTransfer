package manager;

import java.util.Collection;

public interface IPeerManager 
{
	//returns whether the neighbor has completely downloaded the parts or not
  public void finishedDownloading();
  
  //Choke the list of peers
  public void chokeListOfPeers(Collection<Integer> chokedPeersIds);
  
  //Unchoke the list of peers
  public void unchokeListOfPeers(Collection<Integer> unchokedPeersIds);
}