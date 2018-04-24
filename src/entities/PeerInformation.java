package entities;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

public class PeerInformation 
{
	public static final String CONFIG_FILE = "PeerInfo.cfg";
    private final List<PeerObject> peerList = new LinkedList<PeerObject>();

    /*
     * Read the peer information from the config file
     */
    public void readFromConfigFile(Reader reader) throws FileNotFoundException, IOException, ParseException 
    {
        BufferedReader in = new BufferedReader(reader);
        
        String line = in.readLine();
//        System.out.println(line);
        while(line != null) {
        	
        	String[] words = line.trim().split(" ");
//        	System.out.println(words[0].trim());
        	int id = Integer.parseInt(words[0].trim());
        	String address = words[1].trim();
        	int port = Integer.parseInt(words[2].trim());
        	int hasFile = Integer.parseInt(words[3].trim());
        	
        	//adding new peer object for the peer that is read in the PeerInfo.cfg file
        	peerList.add(new PeerObject(id, address, port, hasFile));
        	line = in.readLine();
        }
    }

    /*
     * Get the list of peer objects
     */
    public List<PeerObject> getPeerList() 
    {
        return peerList;
    }
}
