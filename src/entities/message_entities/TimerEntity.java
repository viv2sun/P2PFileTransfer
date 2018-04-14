package entities.message_entities;

import java.io.IOException;
import java.util.TimerTask;

import manager.*;
import io.ProtocolObjectOutputStream;

public class TimerEntity extends TimerTask
{

    private final Request req;
    private final FileManager fm;
    private final  ProtocolObjectOutputStream out;
    private final int remPId;
    private final MessageTemplate msg;

    public TimerEntity(Request req, FileManager fm, ProtocolObjectOutputStream out, MessageTemplate msg, int remPId) 
    {
        super();
        this.req = req;
        this.fm = fm;
        this.out = out;
        this.remPId = remPId;
        this.msg = msg;
    }

    @Override
    public void run() 
    {
        if(fm.hasPart(req.getPieceIndex())) 
        {
            /*LogHelper.getLogger().debug("Not rerequesting piece " + req.getPieceIndex()
                    + " to peer " + remPId);*/
        }
        else 
        {
            /*LogHelper.getLogger().debug("Rerequesting piece " + req.getPieceIndex()
                    + " to peer " + remPId);*/
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
