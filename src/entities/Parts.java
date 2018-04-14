package entities;

import java.util.BitSet;

import utils.ConfigurationReader;

public class Parts 
{
    private final BitSet partsReq;
    private final long timeout;

    public Parts(int parts, long interval) 
    {
        partsReq = new BitSet(parts);
        timeout = interval * 2;
    }

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
                            //LogHelper.getLogger().debug("clearing requested parts for pert " + partId);
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
