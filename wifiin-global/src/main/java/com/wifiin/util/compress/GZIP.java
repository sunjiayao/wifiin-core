package com.wifiin.util.compress;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * GZIP压缩解压
 * @author Running
 *
 */
public class GZIP {
    /**
     * 压缩
     * @param src
     * @return
     * @throws IOException
     */
	public static byte[] gzip(byte[] src) throws IOException{
		ByteArrayOutputStream byteIn=null;
		GZIPOutputStream gzip=null;
		boolean gzipClosed=false;
		try{
			byteIn=new ByteArrayOutputStream();
			gzip=new GZIPOutputStream(byteIn);
			gzip.write(src);
			gzip.close();
			gzipClosed=true;
			return byteIn.toByteArray();
		}finally{
			if(gzip!=null && !gzipClosed){
				gzip.close();
			}
		}
	}
	/**
	 * 解压
	 * @param src
	 * @return
	 * @throws IOException
	 */
	public static byte[] ungzip(byte[] src) throws IOException{
		InputStream in=null;
		ByteArrayOutputStream result=null;
		try{
			in=new GZIPInputStream(new ByteArrayInputStream(src));
			result=new ByteArrayOutputStream();
			transfer(result,in,src.length);
			return result.toByteArray();
		}finally{
			if(in!=null){
				in.close();
			}
			if(result!=null){
				result.close();
			}
		}
	}
	/**
	 * 把输入流的字节序输出到输出流
	 * @param out
	 * @param in
	 * @param buffSize
	 * @throws IOException
	 */
	private static void transfer(OutputStream out, InputStream in,int buffSize) throws IOException{
		byte[] buf=new byte[buffSize];
		int c=0;
		while((c=in.read(buf))>0){
			out.write(buf,0,c);
		}
	}
	
}
