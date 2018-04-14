package entities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import utils.fileutils.*;

public class FileObject 
{

    public File file;
    public File partsFolder;
    public static String partsLoc = "files/parts/";

    public FileObject(int pId, String file)
    {
        partsFolder = new File("./peer_" +pId+ "/" +partsLoc+ file);
        partsFolder.mkdirs();
        this.file = new File(partsFolder.getParent() + "/../" + file);
    }

    public byte[][] getAllParts()
    {
        File[] files = partsFolder.listFiles(new FilenameFilter() 
        {
            @Override
            public boolean accept(File dir, String name) {
                return true;
            }
        });
        
        byte[][] arr = new byte[files.length][getPartsArray(1).length];
        
        for (File file : files) 
        {
            arr[Integer.parseInt(file.getName())] = getByteArray(file);
        }
        
        return arr;
    }

    public byte[] getPartsArray(int part) 
    {
        File file = new File(partsFolder.getAbsolutePath() + "/" + part);
        return getByteArray(file);
    }

    public void WriteArrayAsParts(byte[] arr, int id)
    {
        FileOutputStream fos;
        File ofile = new File(partsFolder.getAbsolutePath() + "/" + id);
        try 
        {
            fos = new FileOutputStream(ofile);
            fos.write(arr);
            fos.flush();
            fos.close();
        } 
        catch (FileNotFoundException e) 
        {
            //LogHelper.getLogger().warning(e);
        } 
        catch (IOException e) {
            //LogHelper.getLogger().warning(e);
        }
    }

    private byte[] getByteArray(File file)
    {
        FileInputStream fis = null;
        try 
        {
            fis = new FileInputStream(file);
            byte[] fileBytes = new byte[(int) file.length()];
            int bytesRead = fis.read(fileBytes, 0, (int) file.length());
            fis.close();
            assert (bytesRead == fileBytes.length);
            assert (bytesRead == (int) file.length());
            return fileBytes;
        } 
        catch (FileNotFoundException e) 
        {
            //LogHelper.getLogger().warning(e);
        } 
        catch (IOException e) 
        {
            //LogHelper.getLogger().warning(e);
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

    public void split(int size)
    {
        Split.split(this.file, size);
        //LogHelper.getLogger().debug("File has been split");
    }

    public void mergeFile(int numParts) {
        Merge.merge(this.file, this.partsFolder, numParts);
    }

}
