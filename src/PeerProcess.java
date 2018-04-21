import java.io.FileReader;
import java.io.Reader;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import entities.PeerInformation;
import entities.PeerObject;
import log.LoggerUtils;
import utils.ConfigurationReader;

public class PeerProcess 
{
	public static void main(String[] args) throws Exception 
	{
		if(args.length < 1) 
		{
			String msg = "the number of arguments passed to the program is " + args.length;
			msg +=  " while it should be 1.\nUsage: java peerProcess peerId";
			
		    // Use logger and and log this as type severe, which in-turns
			LoggerUtils.getLogger().severe(msg);
		}
		
		int pId = Integer.parseInt(args[0]);
		
	    // configure the peer 
		String hostAddress = "localhost";
		int portNumber = 6008;
		boolean hasFile = false;
		
        List<PeerObject> pList = new LinkedList<>();
        PeerInformation pInfo = new PeerInformation();
        Reader peerReader = null;
        
        try 
        {
	        peerReader = new FileReader(PeerInformation.CONFIG_FILE);
	        
	        //reading the peer information from the PeerInfo.cfg file
	        pInfo.readFromConfigFile(peerReader);
	        	        
	        for(PeerObject pObj: pInfo.getPeerList()) 
	        {
	        	if(pObj.getId() == pId) 
	        	{
	        		hostAddress = pObj.getAddress();
	        		portNumber = pObj.getPort();
	        		hasFile = pObj.hasFile();
	        		break;
	                // break is to make sure for loop ends once the peer is done connecting 
	        	    // to all peers before it
	        	}
	        	else 
	        	{
	        		pList.add(pObj);
	        		// log the peer configuration after a new peer is added.
	        		LoggerUtils.getLogger().conf ("Read configuration for peer: " + pObj);
	        	}
	        }
        }
        catch(Exception e) 
        {
        	LoggerUtils.getLogger().severe(e);
            return;
        }
        
        finally 
        {
        	try 
        	{ 
        		peerReader.close(); 
        	}
            catch(Exception e) 
        	{	
            	LoggerUtils.getLogger().severe("Could not close the file handler");
        	}
        }
        
        Process process = new Process(pId, hostAddress, portNumber, hasFile, pInfo.getPeerList());
        process.init();
        Thread t = new Thread(process);
        t.start();
        
        //log the peer information of connecting peers and size
        LoggerUtils.getLogger().debug ("Connecting to " + pList.size() + " peers.");
        
        process.connect(pList);
        try {
        	peerReader.close();
        	Thread.sleep(5);
        }
        catch (Exception e){
        	e.printStackTrace();
        }

	}
}
