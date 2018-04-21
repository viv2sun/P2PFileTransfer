package entities;

import java.util.BitSet;

import log.LoggerUtils;
import utils.ConfigurationReader;

public class Parts 
{
	
    private final BitSet partsReq;
    private final long timeout;

    /*
     * Constructor of Parts
     */
    public Parts(int parts, long interval) 
    {
        partsReq = new BitSet(parts);
        timeout = interval * 2;
    }

    /*
     * identitying the parts that is requested and return the part id if found, else return -1
     */
    public synchronized int identifyParts(BitSet partsCanBeRequested) 
    {
        partsCanBeRequested.andNot(this.partsReq);
        
        if (!partsCanBeRequested.isEmpty()) 
        {
            final int partId = ConfigurationReader.GetRandomIndex(partsCanBeRequested);
            partsReq.set(partId);

            new java.util.Timer().schedule(
                new java.util.TimerTask() 
                {
                    @Override
                    public void run() 
                    {
                        synchronized (partsReq) 
                        {
                            partsReq.clear(partId);
                            LoggerUtils.getLogger().debug("The parts requested is cleared for " + partId);
                        }
                    }
                }, 
                timeout 
            );
            return partId;
        }
        return -1;
    }

}
