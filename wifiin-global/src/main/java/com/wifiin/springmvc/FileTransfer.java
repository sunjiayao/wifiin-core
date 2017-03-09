package com.wifiin.springmvc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.springframework.web.multipart.commons.CommonsMultipartFile;

public class FileTransfer {
    public static void transfer(CommonsMultipartFile src,String destPath) throws IllegalStateException, IOException{
        transfer(src,new File(destPath));
    }
    
    public static void transfer(CommonsMultipartFile src,File dest) throws IllegalStateException, IOException{
        if(!dest.exists()){
            dest.mkdirs();
        }
        if(dest.isDirectory()){
            dest=new File(dest,src.getName());
        }
        src.transferTo(dest);
    }
    
    public static void transfer(CommonsMultipartFile src, OutputStream dest) throws IOException{
        InputStream in=src.getInputStream();
        byte[] buf=new byte[1024];
        int len=0;
        while((len=in.read(buf))>0){
            dest.write(buf, 0, len);
        }
    }
}
