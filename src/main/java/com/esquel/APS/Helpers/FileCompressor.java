package com.esquel.APS.Helpers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 用于友压缩/解压文件
 * @author ZhangKent
 *
 */
public class FileCompressor {
	  private int k = 1;
	  
	    public FileCompressor() {  
	          
	    }  
	   
	   public static void execute() {  
		   FileCompressor book = new FileCompressor();  
	        try {  
	            book.zip("C:\\Users\\WangKarl\\Desktop\\111.zip",  
	                    new File("C:\\Users\\WangKarl\\Desktop\\111"));  
	        } catch (Exception e) {  
	            e.printStackTrace();  
	        }  
	  
	    }
	  
	    private void zip(String zipFileName, File inputFile) throws Exception {  
	        System.out.println("started...");  
	        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(  
	                zipFileName));  
	        BufferedOutputStream bo = new BufferedOutputStream(out);  
	        zip(out, inputFile, inputFile.getName(), bo);  
	        bo.close();  
	        out.close(); 
	        System.out.println("finished");  
	    }  
	  
	    private void zip(ZipOutputStream out, File f, String base,  
	            BufferedOutputStream bo) throws Exception { 
	        if (f.isDirectory()) {  
	            File[] fl = f.listFiles();  
	            if (fl.length == 0) {  
	                out.putNextEntry(new ZipEntry(base + "/"));
	                System.out.println(base + "/");  
	            }  
	            for (int i = 0; i < fl.length; i++) {  
	                zip(out, fl[i], base + "/" + fl[i].getName(), bo); 
	            }  
	            System.out.println(k);  
	            k++;  
	        } else {  
	            out.putNextEntry(new ZipEntry(base)); 
	            System.out.println(base);  
	            FileInputStream in = new FileInputStream(f);  
	            BufferedInputStream bi = new BufferedInputStream(in);  
	            int b;  
	            while ((b = bi.read()) != -1) {  
	                bo.write(b); 
	            }  
	            bi.close();  
	            in.close(); 
	            bo.flush();
	        }  
	    }  
}
