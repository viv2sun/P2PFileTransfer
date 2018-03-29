import java.io.FileReader;
import java.io.Reader;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import entities.PeerInformation;
import entities.PeerObject;

public class PeerProcess {
	public static void main(String[] args) throws Exception {
		if(args.length < 1) {
			throw new Exception("Invalid number of arguments");
		    // Use logger and and log this as type severe, which in-turns 
		    // throws an exception
		}
		int pId = Integer.parseInt(args[0]);
	    // configure the peer 
		String hostAddress = "localhost"; // Can retrieve from args[1]
		int portNumber = 6008;
		boolean hasFile = false;
		
        List<PeerObject> pList = new LinkedList<>();
        PeerInformation pInfo = new PeerInformation();
        Reader peerReader = new FileReader(PeerInformation.CONFIG_FILE);
        pInfo.readFromConfigFile(peerReader);
        for(PeerObject pObj: pInfo.getPeerList()) {
        	if(pObj.getId() == pId) {
        		hostAddress = pObj.getAddress();
        		portNumber = pObj.getPort();
        		hasFile = pObj.hasFile();
        		break;
                // break is to make sure for loop ends once the peer is done connecting 
        	    // to all peers before it
        	}
        	else {
        		pList.add(pObj);
        		// log the peer configuration after a new peer is added.
        	}
        }
        
        Process process = new Process(pId, hostAddress, portNumber, hasFile, pInfo.getPeerList());
        process.init();
        Thread t = new Thread(process);
        t.start();
        
        //log the peer information of connecting peers and size
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
