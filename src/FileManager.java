import java.util.BitSet;
import java.util.Properties;

import utils.ConfigurationReader.ConfigurationParameters;


public class FileManager {
	private BitSet rcvParts;
	private final double pieceSize;
    private final int bitsetSize;
    private final BitSet reqPart;
    private final String fileName;
    private final int fileSize;
    private long timeOut;
	
	public FileManager(int id, Properties config) {
		   fileName = config.getProperty (ConfigurationParameters.FileName.toString());
           fileSize = Integer.parseInt(config.getProperty(ConfigurationParameters.FileSize.toString())); 
           pieceSize = Integer.parseInt(config.getProperty(ConfigurationParameters.PieceSize.toString()));
           long unchokingInterval = Integer.parseInt(config.getProperty(ConfigurationParameters.UnchokingInterval.toString())) * 1000;
           bitsetSize = (int) Math.ceil (fileSize/pieceSize);
           //log the file size
           rcvParts = new BitSet (bitsetSize);
           reqPart = new BitSet(bitsetSize);
           timeOut = unchokingInterval * 2;
           
	}
	

	public Object getMapSize() {
		// TODO Auto-generated method stub
		return null;
	}

	public void register(Process process) {
		// TODO Auto-generated method stub
		
	}

	public void splitFile() {
		// TODO Auto-generated method stub
		
	}

	public void setAllBitField() {
		// TODO Auto-generated method stub
		
	}
}
