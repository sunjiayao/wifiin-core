package com.wifiin.util.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;

import com.wifiin.common.CommonConstant;

public class Decryptor extends EnDecryptionUtil{
	/**
	    * Passport 解密函数
	    * 
	    * @param        string      加密后的字串
	    * @param        string      私有密匙(用于解密和加密)，长度为pow(2,n)最佳，长度为pow(2,n)+1最差
	    *  
	    * @return   string      字串经过私有密匙解密后的结果
	    * @throws IOException 
	    */
	    public static String decrypt(String txt,String key) throws IOException{
	        // txt 的结果为加密后的字串经过 base64 解码，然后与私有密匙一起，
	        return decrypt(txt,key,isBase64Key(key));
	    }
	    public static String decrypt(String txt, String key, boolean base64key) throws IOException{
	    	return new String(decrypt(Base64.decodeBase64(txt), 
	    			base64key?Base64.decodeBase64(key):key.getBytes(CommonConstant.DEFAULT_CHARSET_NAME)),
	    			CommonConstant.DEFAULT_CHARSET_NAME);
	    }
	    public static String decrypt(String txt, byte[] key) throws IOException{
	    	return new String(decrypt(Base64.decodeBase64(txt),key),CommonConstant.DEFAULT_CHARSET_NAME);
	    }
	    public static byte[] decrypt(byte[] src, byte[] key) throws UnsupportedEncodingException{
	        return decrypt(src,src.length,key);
	    }
	    public static byte[] decrypt(byte[] src, String key) throws IOException{
	    	return decrypt(src, key,isBase64Key(key));
	    }
	    public static byte[] decrypt(byte[] src, String key, boolean base64key) throws IOException{
	    	return decrypt(src,base64key?Base64.decodeBase64(key):key.getBytes(CommonConstant.DEFAULT_CHARSET_NAME));
	    }
	    public static byte[] decrypt(byte[] src, int offset, byte[] key) throws UnsupportedEncodingException{
	        // 经过 passportKey() 函数处理后的返回值
	    	src=passportKey(src,key);
	        byte[] dst=new byte[offset/2];
	        for (int i=0,j=0; i < offset;) {
	            // tmp 字串在末尾增加一位，其内容为 txt 的第 i 位,
	            // 与 txt 的第 i + 1 位取异或。然后 i = i + 1
	        	dst[j++]=(byte)(src[i++]^src[i++]);
	        }
	        return dst;
	    }
	    public static void decrypt(InputStream src, OutputStream dst, String key) throws IOException{
	    	decrypt(src,dst,key,isBase64Key(key));
	    }
	    public static void decrypt(InputStream src, OutputStream dst, String key, boolean base64key) throws IOException{
	    	decrypt(src,dst,base64key?Base64.decodeBase64(key):key.getBytes(CommonConstant.DEFAULT_CHARSET_NAME));
	    }
	    public static void decrypt(InputStream src, OutputStream dst, byte[] key) throws IOException{
	    	byte[] buf=new byte[8192];
	    	int l=0;
	    	while((l=src.read(buf))>0){
	    		dst.write(decrypt(buf,l,key));
	    	}
	    	dst.flush();
	    }
	    public static void decrypt(File src, File dst, String key) throws FileNotFoundException, IOException{
	    	decrypt(src,dst,key,isBase64Key(key));
	    }
	    public static void decrypt(File src, File dst, String key, boolean base64key) throws FileNotFoundException, IOException{
	    	decrypt(src,dst,base64key?Base64.decodeBase64(key):key.getBytes(CommonConstant.DEFAULT_CHARSET_NAME));
	    }
	    public static void decrypt(File src, File dst, byte[] key) throws FileNotFoundException, IOException{
	    	decrypt(new FileInputStream(src),new FileOutputStream(dst),key);
	    }
}
