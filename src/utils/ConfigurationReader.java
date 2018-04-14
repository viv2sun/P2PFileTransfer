package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.BitSet;
import java.util.Properties;

public class ConfigurationReader {

	public static final String CONFIGURATION_FILE = "common.cfg";
	
	public enum ConfigurationParameters {
		NumberOfPreferredNeighbors,
		UnchokingInterval,
		OptimisticUnchokingInterval,
		FileName,
		FileSize,
		PieceSize
	}
	
	public static Properties readConfigFile(Reader inputReader) throws Exception {

		final Properties configurationProperties = new Properties() 
		{
	        @Override
	        public synchronized void load(Reader reader) throws IOException {
	            BufferedReader in = new BufferedReader(reader);
	            String line = in.readLine();
	            while(line != null) {
	            	String[] words = line.split(" ");
	            	setProperty(words[0].trim(), words[1].trim());
	            	line = in.readLine();
	            }
	       }
		};
	
	    configurationProperties.load(inputReader);
    
	    return configurationProperties;
	}
	
	public static int GetRandomIndex(BitSet bitset) 
	{
        if (bitset.isEmpty()) 
        {
            throw new RuntimeException ("The bitset is empty, cannot find a set element");
        }
        
        String set = bitset.toString();
        
        String[] indexes = set.substring(1, set.length()-1).split(",");
        
        return Integer.parseInt(indexes[(int)(Math.random()*(indexes.length-1))].trim());
    }

}

