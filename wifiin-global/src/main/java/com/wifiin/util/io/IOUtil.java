package com.wifiin.util.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.wifiin.util.Help;

public class IOUtil {
    private static final int BUF_SIZE=256;
    private static final ThreadLocal<BufData> BUF_POOL=new ThreadLocal<>();
    private static class BufData{
        public byte[] buf=new byte[BUF_SIZE];
        public ByteArrayOutputStream stream=new ByteArrayOutputStream(BUF_SIZE);;
    }
    private static BufData buf(){
        BufData buf=BUF_POOL.get();
        if(buf==null){
            buf=new BufData();
            BUF_POOL.set(buf);
        }
        return buf;
    }
    public static byte[] read(InputStream in) throws IOException{
        BufData data=buf();
        ByteArrayOutputStream buf=data.stream;
        byte[] bs=data.buf;
        int l=0;
        while((l=in.read(bs))>0){
            buf.write(bs,0,l);
        }
        buf.close();
        try{
            return buf.toByteArray();
        }finally{
            buf.reset();
        }
    }
    public static byte[] read(InputStream in,int len) throws IOException{
        byte[] buf=new byte[len];
        int offset=0;
        int quantity=0;
        while(offset<len && quantity>=0){
            quantity=in.read(buf,offset,buf.length-offset);
            if(quantity>0){
                offset+=quantity;
            }
        }
        return buf;
    }
	public static String readString(InputStream in, String charset) throws IOException{
		byte[] bs=read(in);
		String string=null;
		if(bs!=null && bs.length>0){
			string=new String(bs,Help.isEmpty(charset)?"UTF-8":charset);
		}
		return string;
	}
}
