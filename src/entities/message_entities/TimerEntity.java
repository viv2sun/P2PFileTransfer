package entities.message_entities;

import java.io.IOException;
import java.util.TimerTask;

import manager.*;
import io.ProtocolObjectOutputStream;
import log.LoggerUtils;

public class TimerEntity extends TimerTask
{

  private final Request req;
  private final FileManager fm;
  private final ProtocolObjectOutputStream out;
  private final int remPId;
  private final MessageTemplate msg;

  /*
   * Constructor of the timer entity
   */
  public TimerEntity(Request req, FileManager fm, ProtocolObjectOutputStream out, MessageTemplate msg, int remPId) 
  {
    super();
    this.req = req;
    this.fm = fm;
    this.out = out;
    this.remPId = remPId;
    this.msg = msg;
  }

  /*
   * (non-Javadoc)
   * @see java.util.TimerTask#run()
   */
  @Override
  public void run() 
  {
    if(fm.hasPart(req.getPieceIndex())) 
    {
      LoggerUtils.getLogger().debug("No re-request for piece: " + req.getPieceIndex() + " to peer " + remPId);
    }
    else 
    {
      LoggerUtils.getLogger().debug("Re-request for piece " + req.getPieceIndex() + " to peer " + remPId);
      try 
      {
        out.writeObject(msg);
      } 
      catch (IOException e) 
      {
        e.printStackTrace();
      }
    }
  }

}
