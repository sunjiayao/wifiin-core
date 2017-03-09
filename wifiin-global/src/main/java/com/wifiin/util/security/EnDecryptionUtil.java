package com.wifiin.util.security;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import com.wifiin.util.Help;
import com.wifiin.util.digest.MessageDigestUtil;
import com.wifiin.util.regex.RegexUtil;

class EnDecryptionUtil extends MessageDigestUtil{
	/**
     * Passport 密匙处理函数
     * 
     * @param        byte[]      待加密或待解密的字节
     * @param        string      私有密匙(用于解密和加密)
     *  
     * @return   string      处理后的密匙
     * @throws UnsupportedEncodingException 
     */
    protected static byte[] passportKey(byte[] src, byte[] encryptKey) throws UnsupportedEncodingException{
    	byte[] keys = digest(encryptKey);
        // 变量初始化
        int sl=src.length;
        byte [] dst=new byte[sl];
        for(int i = 0,ekc=keys.length-1; i < sl; i++) {
            // 如果 ctr = encryptKey 的长度，则 ctr 清零
            // tmp 字串在末尾增加一位，其内容为 txt 的第 i 位,
            // 与 encryptKey 的第 ekc&i 位取异或。
        	dst[i]=(byte)(src[i]^keys[ekc&i]);
        }
        return dst;
        
    }
    protected static byte[] digestCoupleUUIDBytes(){
    	return digest(coupleUUIDBytes());
    }
    protected static byte[] coupleUUIDBytes(){
    	byte[] dst=new byte[32];
    	populate(UUID.randomUUID(),dst,0);
    	populate(UUID.randomUUID(),dst,16);
    	return dst;
    }
    protected static byte[] singleUUIDBytes(){
    	byte[] dst=new byte[16];
    	populate(UUID.randomUUID(),dst,0);
    	return dst;
    }
    private static byte[] populate(UUID src, byte[] dst, int offset){
    	populate(src.getLeastSignificantBits(),dst,offset);
		populate(src.getMostSignificantBits(),dst,offset+8);
		return dst;
    }
    private static byte[] populate(long src, byte[] dst, int offset){
    	for(int i=offset+7;i>=offset;i--){
    		dst[i]=(byte)(src&0xff);
    		src>>>=8;
    	}
    	return dst;
    }
    protected static byte[] digest(byte[] src){
    	return digest(src,src);
    }
    protected static byte[] digest(byte[] md5, byte[] sha){
    	byte[] dst=new byte[md5.length+sha.length];
    	System.arraycopy(md5, 0, dst, 0, md5.length);
    	System.arraycopy(sha, 0, dst, md5.length, sha.length);
    	return Help.merge(md5,sha);
    }
    protected static boolean isBase64Key(String key){
    	return RegexUtil.isBase64(key)&& key.length()%4==0;
    }
}
