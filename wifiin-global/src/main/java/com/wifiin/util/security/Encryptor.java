package com.wifiin.util.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;

import com.wifiin.common.CommonConstant;

public class Encryptor extends EnDecryptionUtil{
	
    /**
    * Passport 加密函数
    * 
    * @param        string      等待加密的原字串
    * @param        string      私有密匙(用于解密和加密)，长度为pow(2,n)最佳，长度为pow(2,n)+1最差
    * 
    * @return   string      原字串经过私有密匙加密后的结果
     * @throws IOException 
    */
    public static String encrypt(String txt,String key) throws IOException{
        return encrypt(txt,key,isBase64Key(key));
    }
    public static String encrypt(String txt, String key, boolean base64key) throws IOException{
    	return Base64.encodeBase64String(encrypt(txt.getBytes(CommonConstant.DEFAULT_CHARSET_NAME),
    			base64key?Base64.decodeBase64(key):key.getBytes(CommonConstant.DEFAULT_CHARSET_NAME)));
    }
    public static String encrypt(String src,byte[] key) throws UnsupportedEncodingException{
    	return Base64.encodeBase64String(encrypt(src.getBytes(CommonConstant.DEFAULT_CHARSET_NAME),key));
    }
    public static byte[] encrypt(byte[] src, String key) throws IOException{
    	return encrypt(src,key,isBase64Key(key));
    }
    public static byte[] encrypt(byte[] src, String key, boolean base64key) throws IOException{
    	return encrypt(src,base64key?Base64.decodeBase64(key):key.getBytes(CommonConstant.DEFAULT_CHARSET_NAME));
    }
    public static byte[] encrypt(byte[] src, byte[] key) throws UnsupportedEncodingException{
    	return encrypt(src,src.length,key);
    }
    private static byte[] encrypt(byte[] src,int offset, byte[] key) throws UnsupportedEncodingException{
    	//随机uuid并 MD5+sha
        byte[] encryptKey = digestCoupleUUIDBytes();
        int ekc=encryptKey.length-1;
        byte[] dst=new byte[offset*2];
        for(int i = 0,j=0; i < offset; i++) {
            // tmp 字串在末尾增加两位，其第一位内容为 encryptKey 的第 ctr 位，
            // 第二位内容为 txt 的第 i 位与 encryptKey 的 ekc&i 位取异或
            byte a1=encryptKey[ekc&i];
            dst[j++]=a1;
            dst[j++]=(byte)(src[i]^a1);
        }
        return passportKey(dst,key);
    }
    public static void encrypt(InputStream src, OutputStream dst, byte[] key) throws IOException{
    	byte[] buf=new byte[8192];
    	int l=0;
    	while((l=src.read(buf))>0){
    		dst.write(encrypt(buf,l,key));
    	}
    	dst.flush();
    }
    public static void encrypt(File src, File dst, byte[] key) throws FileNotFoundException, IOException{
    	encrypt(new FileInputStream(src),new FileOutputStream(dst),key);
    }
    public static void encrypt(InputStream src, OutputStream dst, String key) throws IOException{
    	encrypt(src,dst,key,isBase64Key(key));
    }
    public static void encrypt(InputStream src, OutputStream dst, String key, boolean base64key) throws IOException{
    	encrypt(src,dst,base64key?Base64.decodeBase64(key):key.getBytes(CommonConstant.DEFAULT_CHARSET_NAME));
    }
    public static void encrypt(File src, File dst, String key) throws FileNotFoundException, IOException{
    	encrypt(src,dst,key,isBase64Key(key));
    }
    public static void encrypt(File src, File dst, String key, boolean base64key) throws FileNotFoundException, IOException{
    	encrypt(src,dst,base64key?Base64.decodeBase64(key):key.getBytes(CommonConstant.DEFAULT_CHARSET_NAME));
    }
    
    public static void main(String[] args) throws IOException {
    	String uuid=UUID.randomUUID().toString();
    	
    	String s="";
    	long ms=System.currentTimeMillis();
    	s=encrypt("xbcnt123",uuid);
    	System.out.println(System.currentTimeMillis()-ms);
    	ms=System.currentTimeMillis();
//    	for(int i=0;i<100000;i++){
//    		encrypt("xbcnt123",uuid);
//    	}
    	System.out.println(System.currentTimeMillis()-ms);
//    	System.out.println(uuid);
//    	System.out.println(MessageDigestUtil.md5Base64(uuid).length()+"  "+md5(uuid).length()+"  "+uuid.replace("-","").length());
		System.out.println(s+"  "+s.length());
		System.out.println(Decryptor.decrypt(s,uuid));
		System.out.println(Base64.encodeBase64String(new byte[]{(byte)128}));
		System.out.println(encrypt(singleUUIDBytes(),singleUUIDBytes()).length);
		System.out.println(isBase64Key("gA=="));
	}
}