package entities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;

import log.LoggerUtils;
import utils.fileutils.*;

public class FileObject 
{

    public File file;
    public File partsFolder;
    public static String partsLoc = "files/pieces/";

    /*
     * Constructor of the FileObject with the peerId and the file
     */
    public FileObject(int pId, String file)
    {
    	//Creating the parts file inside the parts folder
        partsFolder = new File("./peer_" +pId+ "/" +partsLoc+ file);
        partsFolder.mkdirs();
        this.file = new File(this.partsFolder.getParent() + "/../" + file);
    }

    /*
     * Get all the parts File
     */
    public byte[][] getAllParts()
    {
    	//Lists file inside the parts folder
        File[] files = this.partsFolder.listFiles(new FilenameFilter() 
        {
            @Override
            public boolean accept(File dir, String name) {
                return true;
            }
        });
        
        byte[][] arr = new byte[files.length][getPartsArray(1).length];
        
        //Receive the contents of the file as the byte array
        for (File file : files) 
        {
            arr[Integer.parseInt(file.getName())] = getByteArray(file);
        }
        
        return arr;
    }

    /*
     * get the parts byte array for the part(piece index) specified
     */
    public byte[] getPartsArray(int part) 
    {
    	//get the file
        File file = new File(this.partsFolder.getAbsolutePath() + "/" + part);
        
        //return the byte array of the parts file
        return getByteArray(file);
    }

    /*
     * Write the byte array for the piece inside the parts file
     */
    public void WriteArrayAsParts(byte[] arr, int id)
    {
        FileOutputStream fos;
        File ofile = new File(this.partsFolder.getAbsolutePath() + "/" + id);
        try 
        {
            fos = new FileOutputStream(ofile);
            fos.write(arr);
            fos.flush();
            fos.close();
        } 
        catch (FileNotFoundException e) 
        {
            LoggerUtils.getLogger().warning(e);
        } 
        catch (IOException e) {
            LoggerUtils.getLogger().warning(e);
        }
    }

    /*
     * Given a file object, returns the contents of the file as the byte array
     */
    private byte[] getByteArray(File file)
    {
        FileInputStream fis = null;
        
        try 
        {
        	//read the file
            fis = new FileInputStream(file);
            System.out.println("File length: reading  File Object " + (int) file.length());
            
            //create a byte array equal to the file length
            byte[] fileBytes = new byte[(int) file.length()];
            int bytesRead = fis.read(fileBytes, 0, (int) file.length());
            fis.close();
            assert (bytesRead == fileBytes.length);
            assert (bytesRead == (int) file.length());
            return fileBytes;
        } 
        catch (FileNotFoundException e) 
        {
            LoggerUtils.getLogger().warning(e);
        } 
        catch (IOException e) 
        {
            LoggerUtils.getLogger().warning(e);
        }
        finally 
        {
            if (fis != null) 
            {
                try 
                {
                    fis.close();
                }
                catch (IOException ex) 
                {}
            }
        }
        return null;
    }

    /*
     * Splitting the file into the specified size (each file will then put to the corresponding parts file)
     */
    public void split(int size)
    {
        Split.split(this.file, size);
        LoggerUtils.getLogger().debug("The file is split");
    }

    /*
     * Merging the contents of the parts folder to a single file
     */
    public void mergeFile(int numParts) 
    {
        Merge.merge(this.file, this.partsFolder, numParts);//already this.partsFolder
    }

}
