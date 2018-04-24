package manager;

import java.util.BitSet;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import entities.FileObject;
import entities.Parts;
import log.LoggerUtils;
import utils.ConfigurationReader;
import utils.ConfigurationReader.ConfigurationParameters;


public class FileManager {

    private BitSet rcvParts;
    private final List<IFileManager> fmModules = new LinkedList<>();
    private FileObject fileObj;
    private final double pSize;
    private final int bsSize;
    private final Parts reqParts;

    /*
     * Constructor for the File Manager
     */
    public FileManager(int pId, String fileName, int fSize, int pSize, long ucInterval) 
    {
        this.pSize = pSize;
//        System.out.println(fSize + "is the file size");
//        System.out.println(pSize + "is the piece size");
//        System.out.println((fSize/pSize));
        bsSize = (int) Math.ceil (((double)fSize)/pSize);
        LoggerUtils.getLogger().debug ("File size set to " + fSize +  "\tPart size set to " + pSize + "\tBitset size set to " + bsSize);
        rcvParts = new BitSet(bsSize);
        reqParts = new Parts(bsSize, ucInterval);
        fileObj = new FileObject(pId, fileName);
    }

    /*
     * add parts to the parts file
     */
    public synchronized void addPart(int ind, byte[] partArr) 
    {        
        final boolean isNew = !rcvParts.get(ind);
        rcvParts.set(ind);

        if(isNew) 
        {
            fileObj.WriteArrayAsParts(partArr, ind);
            for(IFileManager fmModule : fmModules) 
            {
                fmModule.partDownloaded(ind); 
            }
        }
        
        if(isFileCompleted()) 
        {
            fileObj.mergeFile(rcvParts.cardinality());
            
            for(IFileManager fmModule : fmModules)
            {
                fmModule.fileDownloaded();
            }
        }
    }

    /*
     * identify the parts that needs to be requested
     */
    public synchronized int identifyParts(BitSet partsAvl) 
    {
        partsAvl.andNot(getPartsRcvd());
        return reqParts.identifyParts(partsAvl);
    }

    /*
     * Get the parts already received
     */
    public synchronized BitSet getPartsRcvd() 
    {
        return (BitSet)rcvParts.clone();
    }

    /*
     * Returns whether the peer has the piece index or not
     */
    synchronized public boolean hasPart(int pieceIndex) 
    {
        return rcvParts.get(pieceIndex);
    }

    public synchronized void setAllParts()
    {
    	System.out.println("Inside setAllParts:  "+bsSize+" is the total bit set size");
        for (int i = 0; i < bsSize; i++)
        {
        
            rcvParts.set(i, true);
        }
        
        LoggerUtils.getLogger().debug("Received parts set to: " + rcvParts.toString());
    }

    /*
     * Get the count of the parts received
     */
    public synchronized int getNumberOfReceivedParts() 
    {
        return rcvParts.cardinality();
    }

    /*
     * get byte array(payload) of part given it's index
     */
    public byte[] getPiece(int partInd) 
    {
        byte[] piece = fileObj.getPartsArray(partInd);
        return piece;
    }

    /*
     * registering the file manager module
     */
    public void registerModule(IFileManager fmModule) 
    {
        fmModules.add(fmModule);
    }

    /*
     * split file utility using the piece size
     */
    public void splitFile()
    {
        fileObj.split((int) pSize);
    }

    /*
     * array of byte array which includes all the parts
     */
    public byte[][] getAllPieces()
    {
        return fileObj.getAllParts();
    }

    /*
     * Return the size of the bitmap
     */
    public int getBitmapSize() 
    {
        return bsSize;
    }

    /*
     * If all parts of the file are available, returns isFileCompleted as True
     */
    private boolean isFileCompleted() 
    {
    	
        for(int i = 0; i < bsSize; i++) 
        {
        	LoggerUtils.getLogger().info("part:" + i +  " Corresponding BitSet: " + rcvParts.get(i));
            if(!rcvParts.get(i)) 
            {
                return false;
            }
        }
        return true;
    }
}
