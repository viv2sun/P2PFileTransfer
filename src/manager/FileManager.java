package manager;

import java.util.BitSet;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import entities.FileObject;
import entities.Parts;
import utils.ConfigurationReader;
import utils.ConfigurationReader.ConfigurationParameters;


public class FileManager {

    private BitSet rcvParts;
    private final List<IFileManager> fmModules = new LinkedList<>();
    private FileObject fileObj;
    private final double pSize;
    private final int bsSize;
    private final Parts reqParts;

    public FileManager(int pId, Properties prop) 
    {
        this (pId, prop.getProperty (ConfigurationReader.ConfigurationParameters.FileName.toString()),
                Integer.parseInt(prop.getProperty(ConfigurationReader.ConfigurationParameters.FileSize.toString())), 
                Integer.parseInt(prop.getProperty(ConfigurationReader.ConfigurationParameters.PieceSize.toString())),
                Integer.parseInt(prop.getProperty(ConfigurationReader.ConfigurationParameters.UnchokingInterval.toString())) * 1000);
    }

    
    public FileManager(int pId, String fileName, int fSize, int pSize, long ucInterval) 
    {
        this.pSize = pSize;
        bsSize = (int) Math.ceil (fSize/pSize);
        //LogHelper.getLogger().debug ("File size set to " + fSize +  "\tPart size set to " + pSize + "\tBitset size set to " + bsSize);
        rcvParts = new BitSet(bsSize);
        reqParts = new Parts(bsSize, ucInterval);
        fileObj = new FileObject(pId, fileName);
    }

    public synchronized void addPart(int ind, byte[] partArr) 
    {        
        final boolean isNew = !rcvParts.get(ind);
        rcvParts.set(ind);

        if(isNew) 
        {
            fileObj.WriteArrayAsParts(partArr, ind);
            for(IFileManager fmModule : fmModules) 
            {
                fmModule.pieceArrived (ind);
            }
        }
        
        if(isFileCompleted()) 
        {
            fileObj.mergeFile(rcvParts.cardinality());
            
            for(IFileManager fmModule : fmModules)
            {
                fmModule.fileCompleted();
            }
        }
    }

    
    public synchronized int identifyParts(BitSet partsAvl) 
    {
        partsAvl.andNot(getReceivedParts());
        return reqParts.identifyParts(partsAvl);
    }

    public synchronized BitSet getReceivedParts () {
        return (BitSet) rcvParts.clone();
    }

    synchronized public boolean hasPart(int pieceIndex) {
        return rcvParts.get(pieceIndex);
    }

    public synchronized void setAllParts()
    {
        for (int i = 0; i < bsSize; i++) {
            rcvParts.set(i, true);
        }
        //LogHelper.getLogger().debug("Received parts set to: " + rcvParts.toString());
    }

    public synchronized int getNumberOfReceivedParts() 
    {
        return rcvParts.cardinality();
    }

    public byte[] getPiece(int partInd) 
    {
        byte[] piece = fileObj.getPartsArray(partInd);
        return piece;
    }

    public void registerModule(IFileManager fmModule) 
    {
        fmModules.add(fmModule);
    }

    public void splitFile()
    {
        fileObj.split((int) pSize);
    }

    public byte[][] getAllPieces()
    {
        return fileObj.getAllParts();
    }

    public int getBitmapSize() 
    {
        return bsSize;
    }

    private boolean isFileCompleted() 
    {
        for(int i = 0; i < bsSize; i++) 
        {
            if(!rcvParts.get(i)) 
            {
                return false;
            }
        }
        return true;
    }
}
