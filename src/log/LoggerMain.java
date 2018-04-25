package log;

public class LoggerMain
{

  private final LoggerUtils logUtils;
  private final String msg; 
  
  /*
   * Constructor for the logger main
   */
  public LoggerMain(int pId, LoggerUtils LoggerUtils) 
  {
    msg = ": Peer " + pId;
    logUtils = LoggerUtils;
  }

  /*
   * logger for establishing peer connection
   */
  public void establishPeerConnectionLog(int peerId, boolean isConnectingPeer) 
  {
  	String temp = this.msg;
  	
  	if(isConnectingPeer) {
  		temp += " makes a connection to Peer " + peerId + ".";
  	}
  	else {
  		temp += " is connected from Peer " + peerId + ".";
  	}
    logUtils.info(temp);
  }

  /*
   * logger for the change of the preferred neighbors 
   */
  public void preferredNeighborChangeLog(String preferredNeighbors) 
  {
  	String temp = this.msg;  	
  	temp = " The preferred neighbors of " + temp + " are: " + preferredNeighbors;
  	
    logUtils.info(temp);
  }

  /*
   * logger for the change of the optimistically unchoked neighbors
   */
  public void optimisticallyUnchokedNeighborsChangeLog(String preferredNeighbors) 
  {
  	String temp = this.msg;  	
  	temp = "The optimistically unchoked neighbor of " + temp + ": " + preferredNeighbors;
  	
    logUtils.info(temp);
  }

  /*
   * logger for the choke message
   */
  public void chokeMessage(int peerId) 
  {
  	String temp = this.msg;  	
  	temp += " is choked by " + peerId +".";
  	
    logUtils.info(temp);
  }

  /*
   * logger for the unchoke message
   */
  public void unchokeMessage(int peerId) 
  {
  	String temp = msg;  	
  	temp += " is unchoked by " + peerId +".";
  	
    logUtils.info(temp);
  }

  /*
   * logger for the have message
   */
  public void haveMessage (int peerId, int pieceIdx) 
  {
  	String temp = msg;  	
  	temp += " received the 'have' message from " + peerId +" for the piece " + pieceIdx + ".";
  	
    logUtils.info(temp);
  }

  /*
   * logger for the interested message
   */
  public void interestedMessage(int peerId) 
  {
  	String temp = msg;
  	temp += " received the 'interested' message from " + peerId + ".";
  	
    logUtils.info(temp);
  }

  /*
   * logger for the not interested message
   */
  public void notInterestedMessage(int peerId) 
  {
  	String temp = msg;
  	temp += " received the 'not interested' message from " + peerId + ".";
  	
    logUtils.info(temp);
  }

  /*
   * logger for the piece downloaded message
   */
  public void pieceDownloadedMessage(int peerId, int pieceIdx, int currNumberOfPieces) 
  {
  	String temp = msg;
  	temp = "Piece: " + pieceIdx + " has been downloaded by " + temp+" from peer " + peerId + " Now the number of pieces it has is " + currNumberOfPieces + ".";
  	
    logUtils.info(temp);
  }

  /*
   * Logger for the file downloaded message
   */
  public void fileDownloadedMessage() 
  {
  	String temp = msg;
  	temp = "Download completed for "+temp;
  	
    logUtils.info(temp);
  }

}
