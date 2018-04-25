package utils.fileutils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import log.LoggerUtils;

public class Merge 
{
	/*
	 * Utils for merging the different parts file into a single file
	 */
	 public static void merge(File file, File partsDir, int numParts) 
	 {
	    File out = file;
	    FileOutputStream fos;
	    FileInputStream fis;
	    byte[] fileBytes;
	    int bytesRead = 0;
	    List<File> list = new ArrayList<>();
	    
	    //get all the parts file from the parts directory
	    for (int i = 0; i < numParts; i++) 
	    {
	      list.add(new File(partsDir.getPath() + "/" + i));
	    }
	    try 
	    {
	      fos = new FileOutputStream(out);
	      
	      //Iterate through the parts file and flush it to the file object that's passed
	      for (File f : list) 
	      {
	        fis = new FileInputStream(f);
	        fileBytes = new byte[(int) f.length()];
	        bytesRead = fis.read(fileBytes, 0, (int) f.length());
	        assert (bytesRead == fileBytes.length);
	        assert (bytesRead == (int) f.length());
	        fos.write(fileBytes);
	        fos.flush();
	        fileBytes = null;
	        fis.close();
	        fis = null;
	      }
	      fos.close();
	      fos = null;
	    } 
	    catch (Exception e)
	    {
	    	LoggerUtils.getLogger().warning(e);
	    }
	  }
}
