package utils.fileutils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import log.LoggerUtils;


public class Split 
{
	/*
	 * Utilities for the splitting file into different parts file
	 */
	 public static void split(File inFile, int partSize)
	 {
	        FileInputStream fis;
	        String newFile;
	        FileOutputStream fos;
	        int fileSize = (int) inFile.length();
	        int totalChunks = 0;
	        int read = 0;
	        int rLen = partSize;
	        byte[] bytePart;
	        try 
	        {
	            fis = new FileInputStream(inFile);
	            
	            //splitting the file into different parts depending upon the piece size
	            while (fileSize > 0) 
	            {
	                if (fileSize <= 5) 
	                {
	                    rLen = fileSize;
	                }
	                
	                bytePart = new byte[rLen];
	                read = fis.read(bytePart, 0, rLen);
	                fileSize = fileSize - read;
	                assert (read == bytePart.length);
	                totalChunks++;
	                newFile = inFile.getParent() + "/pieces/" + inFile.getName() + "/" + Integer.toString(totalChunks - 1);
	                fos = new FileOutputStream(new File(newFile));
	                fos.write(bytePart);
	                fos.flush();
	                fos.close();
	                bytePart = null;
	                fos = null;
	            }
	            fis.close();
	        } 
	        catch (IOException e) 
	        {
	        	LoggerUtils.getLogger().warning(e);
	        }
	    }
}
