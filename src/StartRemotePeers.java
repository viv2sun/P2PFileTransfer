/*
 *                     CEN5501C Project2
 * This is the program starting remote processes.
 * This program was only tested on CISE SunOS environment.
 * If you use another environment, for example, linux environment in CISE 
 * or other environments not in CISE, it is not guaranteed to work properly.
 * It is your responsibility to adapt this program to your running environment.
 */

import java.io.*;
import java.text.ParseException;
import java.util.*;

import entities.PeerInformation;
import entities.PeerObject;
import log.LoggerUtils;

/*
 * The StartRemotePeers class begins remote peer processes. 
 * It reads configuration file PeerInfo.cfg and starts remote peer processes.
 * You must modify this program a little bit if your peer processes are written in C or C++.
 * Please look at the lines below the comment saying IMPORTANT.
 */
public class StartRemotePeers 
{
	
	List<PeerObject> peerList;
	
	public StartRemotePeers(List<PeerObject> list) 
	{
		peerList = list;
	}
	
	public static void main(String[] args) 
	{
        //configuration file that contains all the peer information
        final String cfgFile = PeerInformation.CONFIG_FILE;
        
        FileReader in = null;
        try 
        {
            in = new FileReader(cfgFile);
            PeerInformation peerInfo = new PeerInformation();
            
            //reading information from the config file
            peerInfo.readFromConfigFile(in);
            
            //getting peer list from the peer info
            StartRemotePeers myStart = new StartRemotePeers(peerInfo.getPeerList());
					
			// get current path
			String path = System.getProperty("user.dir");
			
			// start clients at remote hosts
			for (int i = 0; i < myStart.peerList.size(); i++) 
			{
				
				PeerObject pObj = (PeerObject) myStart.peerList.get(i);
				
				System.out.println("Start remote peer " + pObj.getId() +  " at " + pObj.getAddress() );
				
				// *********************** IMPORTANT *************************** //
				// If your program is JAVA, use this line.
				Runtime.getRuntime().exec("ssh " + pObj.getAddress() + " cd " + path + "; java PeerProcess " + pObj.getId());
				
				// If your program is C/C++, use this line instead of the above line. 
				//Runtime.getRuntime().exec("ssh " + pInfo.peerAddress + " cd " + path + "; ./peerProcess " + pInfo.peerId);
			}		
			System.out.println("Starting all remote peers has done." );
        }
        catch (IOException | ParseException e) 
        {
            LoggerUtils.getLogger().severe(e);
        }
        finally 
        {
            try 
            { 
            	in.close(); 
            }
            catch (Exception e) 
            {
            	LoggerUtils.getLogger().severe(e);
            }
        }
	}
}
