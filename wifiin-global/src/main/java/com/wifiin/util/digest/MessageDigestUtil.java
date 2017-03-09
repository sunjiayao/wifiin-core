package com.wifiin.util.digest;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.DestroyFailedException;

import org.apache.commons.codec.binary.Base64;

import com.google.common.collect.Maps;
import com.wifiin.common.CommonConstant;

public class MessageDigestUtil{
    private static final String DEFAULT_CHARSET=CommonConstant.DEFAULT_CHARSET_NAME;
	public static final int MESSAGE_DIGEST_OUTPUT_TYPE_BASE64=0;
	public static final int MESSAGE_DIGEST_OUTPUT_TYPE_HEX=1;
	
	public static final String MESSAGE_DIGEST_MD2="md2";
	public static final String MESSAGE_DIGEST_MD5="md5";
	
	public static final String MESSAGE_DIGEST_SHA256="SHA-256";
	public static final String MESSAGE_DIGEST_SHA512="SHA-512";
	public static final String MESSAGE_DIGEST_SHA384="SHA-384";
	public static final String MESSAGE_DIGEST_SHA1="sha-1";
	
	public static final String MESSAGE_DIGEST_HMAC_SHA1="hmac-sha1";
	public static final String MESSAGE_DIGEST_HMAC_SHA256="hmac-sha256";
	public static final String MESSAGE_DIGEST_HMAC_SHA384="hmac-sha384";
	public static final String MESSAGE_DIGEST_HMAC_SHA512="hmac-sha512";
	public static final String MESSAGE_DIGEST_HMAC_MD5="hmac-md5";
	public static final String MESSAGE_DIGEST_HMAC_MD2="hmac-md2";
	
	public static final String PBKDF2_HMAC_SHA1 = "PBKDF2WithHmacSHA1";
	public static final String PBKDF2_HMAC_SHA256 = "PBKDF2WithHmacSHA256";
	public static final String PBKDF2_HMAC_SHA384 = "PBKDF2WithHmacSHA384";
	public static final String PBKDF2_HMAC_SHA512 = "PBKDF2WithHmacSHA512";
	
	private static final char[] HEXBUF=new char[]{'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
	
	private static final ThreadLocal<Map<String,MessageDigest>> MESSAGE_DIGEST_MAP=new ThreadLocal<>();
	private static MessageDigest getInstance(String algorithm) throws NoSuchAlgorithmException{
	    Map<String,MessageDigest> mds=MESSAGE_DIGEST_MAP.get();
	    if(mds==null){
	        mds=Maps.newHashMap();
	        MESSAGE_DIGEST_MAP.set(mds);
	    }
	    MessageDigest md=mds.get(algorithm);
	    if(md==null){
	        md=MessageDigest.getInstance(algorithm);
	        mds.put(algorithm,md);
	    }
	    return md;
	}
	public static String messageDigest(long src, String algorithm, int outputType){
		byte[] b=new byte[8];
		for(int i=0;i<8;i++){
			b[i]=(byte)(src>>>(7-i)*8);
		}
		return messageDigest(b,algorithm, outputType);
	}
	
	public static String messageDigest(String src, String charset, String algorithm, int outputType){
		try{
			return messageDigest(src.getBytes(charset), algorithm, outputType);
		}catch(UnsupportedEncodingException e){
			throw new SecurityException(e);
		}
	}
	public static byte[] messageDigest(String src, String algorithm) throws UnsupportedEncodingException{
		return messageDigest(src,DEFAULT_CHARSET,algorithm);
	}
	public static byte[] messageDigest(String src, String charset, String algorithm) throws UnsupportedEncodingException{
		return messageDigest(src.getBytes(charset),algorithm);
	}
	
	public static byte[] messageDigest(byte[] src, String algorithm){
		return messageDigest(src,0,src.length,algorithm);
	}
	public static byte[] messageDigest(byte[] src,int offset,int length,String algorithm){
	    MessageDigest md=null;
		try{
		    md=getInstance(algorithm);
			md.update(src,offset,length);
			return md.digest();
		}catch(NoSuchAlgorithmException e){
			throw new SecurityException(e);
		}finally{
		    if(md!=null){
		        md.reset();
		    }
		}
	}
	public static String messageDigest(byte[] src, String algorithm, int outputType){
        return output(messageDigest(src,algorithm),outputType);
    }
    public static String messageDigest(byte[] src,int offset,int length,String algorithm,int outputType){
        return output(messageDigest(src,offset,length,algorithm),outputType);
    }
    
    private static String output(byte[] src, int outputType){
        switch(outputType){
        case MESSAGE_DIGEST_OUTPUT_TYPE_BASE64:
            return Base64.encodeBase64String(src);
        case MESSAGE_DIGEST_OUTPUT_TYPE_HEX://此处一定要用十六进制数组，如果直接用BigInteger转十六进制，开头是0的情况就被忽略掉了
            int mask=0xf;
            int l=src.length;
            char[] hex=new char[l*2];
            for(int i=0,hexIdx=0;i<l;i++){
                byte b=src[i];
                hex[hexIdx++]=HEXBUF[(b>>>4)&mask];
                hex[hexIdx++]=HEXBUF[b&mask];
            }
            return new String(hex);
        default:
            throw new IllegalArgumentException("illegal outputType value, only MessageDigestUtil.MESSAGE_DIGEST_OUTPUT_TYPE_BASE64 and MessageDigestUtil.MESSAGE_DIGEST_OUTPUT_TYPE_HEX are accepted");
        }
    }
	
    public static String md2Hex(String src, String charset){
        return messageDigest(src,charset,MESSAGE_DIGEST_MD2,MESSAGE_DIGEST_OUTPUT_TYPE_HEX);
    }
    public static String md2Base64(String src, String charset){
        return messageDigest(src,charset,MESSAGE_DIGEST_MD2,MESSAGE_DIGEST_OUTPUT_TYPE_BASE64);
    }
    public static String md2Hex(String src){
        return messageDigest(src,DEFAULT_CHARSET,MESSAGE_DIGEST_MD2,MESSAGE_DIGEST_OUTPUT_TYPE_HEX);
    }
    public static String md2Base64(String src){
        return messageDigest(src,DEFAULT_CHARSET,MESSAGE_DIGEST_MD2,MESSAGE_DIGEST_OUTPUT_TYPE_BASE64);
    }
    public static byte[] md2(String src) throws UnsupportedEncodingException{
        return messageDigest(src,MESSAGE_DIGEST_MD2);
    }
    public static byte[] md2(byte[] src){
        return messageDigest(src,MESSAGE_DIGEST_MD2);
    }
    public static byte[] md2(byte[] src, int offset,int length){
        return messageDigest(src,offset,length,MESSAGE_DIGEST_MD2);
    }
    public static String md2Base64(byte[] src){
        return output(md2(src),MESSAGE_DIGEST_OUTPUT_TYPE_BASE64);
    }
    public static String md2Base64(byte[] src,int offset,int length){
        return output(md2(src,offset,length),MESSAGE_DIGEST_OUTPUT_TYPE_BASE64);
    }
    public static String md2Hex(byte[] src){
        return output(md2(src),MESSAGE_DIGEST_OUTPUT_TYPE_HEX);
    }
    public static String md2Hex(byte[] src,int offset,int length){
        return output(md2(src,offset,length),MESSAGE_DIGEST_OUTPUT_TYPE_HEX);
    }
    
	public static String md5Hex(String src, String charset){
        return messageDigest(src,charset,MESSAGE_DIGEST_MD5,MESSAGE_DIGEST_OUTPUT_TYPE_HEX);
    }
    public static String md5Base64(String src, String charset){
        return messageDigest(src,charset,MESSAGE_DIGEST_MD5,MESSAGE_DIGEST_OUTPUT_TYPE_BASE64);
    }
    public static String md5Hex(String src){
        return messageDigest(src,DEFAULT_CHARSET,MESSAGE_DIGEST_MD5,MESSAGE_DIGEST_OUTPUT_TYPE_HEX);
    }
    public static String md5Base64(String src){
        return messageDigest(src,DEFAULT_CHARSET,MESSAGE_DIGEST_MD5,MESSAGE_DIGEST_OUTPUT_TYPE_BASE64);
    }
    public static byte[] md5(String src) throws UnsupportedEncodingException{
        return messageDigest(src,MESSAGE_DIGEST_MD5);
    }
	public static byte[] md5(byte[] src){
		return messageDigest(src,MESSAGE_DIGEST_MD5);
	}
	public static byte[] md5(byte[] src, int offset,int length){
		return messageDigest(src,offset,length,MESSAGE_DIGEST_MD5);
	}
	public static String md5Base64(byte[] src){
		return output(md5(src),MESSAGE_DIGEST_OUTPUT_TYPE_BASE64);
	}
	public static String md5Base64(byte[] src,int offset,int length){
		return output(md5(src,offset,length),MESSAGE_DIGEST_OUTPUT_TYPE_BASE64);
	}
	public static String md5Hex(byte[] src){
		return output(md5(src),MESSAGE_DIGEST_OUTPUT_TYPE_HEX);
	}
	public static String md5Hex(byte[] src,int offset,int length){
		return output(md5(src,offset,length),MESSAGE_DIGEST_OUTPUT_TYPE_HEX);
	}
	
	@Deprecated
    public static byte[] sha(String src) throws UnsupportedEncodingException{
        return sha1(src);
    }
	public static String sha1Hex(String src, String charset){
        return messageDigest(src,charset,MESSAGE_DIGEST_SHA1,MESSAGE_DIGEST_OUTPUT_TYPE_HEX);
    }
    @Deprecated
    public static String shaHex(String src, String charset){
        return sha1Hex(src,charset);
    }
    public static String sha1Base64(String src, String charset){
        return messageDigest(src,charset,MESSAGE_DIGEST_SHA1,MESSAGE_DIGEST_OUTPUT_TYPE_BASE64);
    }
    @Deprecated
    public static String shaBase64(String src, String charset){
        return sha1Base64(src,charset);
    }
    public static String sha1Hex(String src){
        return messageDigest(src,DEFAULT_CHARSET,MESSAGE_DIGEST_SHA1,MESSAGE_DIGEST_OUTPUT_TYPE_HEX);
    }
    @Deprecated
    public static String shaHex(String src){
        return sha1Hex(src);
    }
    public static String sha1Base64(String src){
        return messageDigest(src,DEFAULT_CHARSET,MESSAGE_DIGEST_SHA1,MESSAGE_DIGEST_OUTPUT_TYPE_BASE64);
    }
    @Deprecated
    public static String shaBase64(String src){
        return sha1Base64(src);
    }
	
	public static byte[] sha1(String src) throws UnsupportedEncodingException{
        return messageDigest(src,MESSAGE_DIGEST_SHA1);
    }
	public static byte[] sha1(byte[] src){
		return messageDigest(src,MESSAGE_DIGEST_SHA1);
	}
	public static byte[] sha1(byte[] src, int offset,int length){
		return messageDigest(src,offset,length,MESSAGE_DIGEST_SHA1);
	}
	public static String sha1Base64(byte[] src){
		return output(sha1(src),MESSAGE_DIGEST_OUTPUT_TYPE_BASE64);
	}
	public static String sha1Base64(byte[] src,int offset,int length){
		return output(sha1(src,offset,length),MESSAGE_DIGEST_OUTPUT_TYPE_BASE64);
	}
	public static String sha1Hex(byte[] src){
		return output(sha1(src),MESSAGE_DIGEST_OUTPUT_TYPE_HEX);
	}
	public static String sha1Hex(byte[] src,int offset,int length){
		return output(sha1(src,offset,length),MESSAGE_DIGEST_OUTPUT_TYPE_HEX);
	}
	
	
	public static byte[] sha256(String src) throws UnsupportedEncodingException{
        return messageDigest(src,MESSAGE_DIGEST_SHA256);
    }
	public static byte[] sha256(byte[] src){
        return messageDigest(src,MESSAGE_DIGEST_SHA256);
    }
    public static byte[] sha256(byte[] src, int offset,int length){
        return messageDigest(src,offset,length,MESSAGE_DIGEST_SHA256);
    }
    public static String sha256Base64(byte[] src){
        return output(sha256(src),MESSAGE_DIGEST_OUTPUT_TYPE_BASE64);
    }
    public static String sha256Base64(byte[] src,int offset,int length){
        return output(sha256(src,offset,length),MESSAGE_DIGEST_OUTPUT_TYPE_BASE64);
    }
    public static String sha256Hex(byte[] src){
        return output(sha256(src),MESSAGE_DIGEST_OUTPUT_TYPE_HEX);
    }
    public static String sha256Hex(byte[] src,int offset,int length){
        return output(sha256(src,offset,length),MESSAGE_DIGEST_OUTPUT_TYPE_HEX);
    }
    public static String sha256Hex(String src, String charset){
        return messageDigest(src,charset,MESSAGE_DIGEST_SHA256,MESSAGE_DIGEST_OUTPUT_TYPE_HEX);
    }
    public static String sha256Base64(String src, String charset){
        return messageDigest(src,charset,MESSAGE_DIGEST_SHA256,MESSAGE_DIGEST_OUTPUT_TYPE_BASE64);
    }
    public static String sha256Hex(String src){
        return messageDigest(src,DEFAULT_CHARSET,MESSAGE_DIGEST_SHA256,MESSAGE_DIGEST_OUTPUT_TYPE_HEX);
    }
    public static String sha256Base64(String src){
        return messageDigest(src,DEFAULT_CHARSET,MESSAGE_DIGEST_SHA256,MESSAGE_DIGEST_OUTPUT_TYPE_BASE64);
    }
	

    public static byte[] sha512(String src) throws UnsupportedEncodingException{
        return messageDigest(src,MESSAGE_DIGEST_SHA512);
    }
    public static byte[] sha512(byte[] src){
        return messageDigest(src,MESSAGE_DIGEST_SHA512);
    }
    public static byte[] sha512(byte[] src, int offset,int length){
        return messageDigest(src,offset,length,MESSAGE_DIGEST_SHA512);
    }
    public static String sha512Base64(byte[] src){
        return output(sha512(src),MESSAGE_DIGEST_OUTPUT_TYPE_BASE64);
    }
    public static String sha512Base64(byte[] src,int offset,int length){
        return output(sha512(src,offset,length),MESSAGE_DIGEST_OUTPUT_TYPE_BASE64);
    }
    public static String sha512Hex(byte[] src){
        return output(sha512(src),MESSAGE_DIGEST_OUTPUT_TYPE_HEX);
    }
    public static String sha512Hex(byte[] src,int offset,int length){
        return output(sha512(src,offset,length),MESSAGE_DIGEST_OUTPUT_TYPE_HEX);
    }
    public static String sha512Hex(String src, String charset){
        return messageDigest(src,charset,MESSAGE_DIGEST_SHA512,MESSAGE_DIGEST_OUTPUT_TYPE_HEX);
    }
    public static String sha512Base64(String src, String charset){
        return messageDigest(src,charset,MESSAGE_DIGEST_SHA512,MESSAGE_DIGEST_OUTPUT_TYPE_BASE64);
    }
    public static String sha512Hex(String src){
        return messageDigest(src,DEFAULT_CHARSET,MESSAGE_DIGEST_SHA512,MESSAGE_DIGEST_OUTPUT_TYPE_HEX);
    }
    public static String sha512Base64(String src){
        return messageDigest(src,DEFAULT_CHARSET,MESSAGE_DIGEST_SHA512,MESSAGE_DIGEST_OUTPUT_TYPE_BASE64);
    }
    
    public static byte[] sha384(String src) throws UnsupportedEncodingException{
        return messageDigest(src,MESSAGE_DIGEST_SHA384);
    }
    public static byte[] sha384(byte[] src){
        return messageDigest(src,MESSAGE_DIGEST_SHA384);
    }
    public static byte[] sha384(byte[] src, int offset,int length){
        return messageDigest(src,offset,length,MESSAGE_DIGEST_SHA384);
    }
    public static String sha384Base64(byte[] src){
        return output(sha384(src),MESSAGE_DIGEST_OUTPUT_TYPE_BASE64);
    }
    public static String sha384Base64(byte[] src,int offset,int length){
        return output(sha384(src,offset,length),MESSAGE_DIGEST_OUTPUT_TYPE_BASE64);
    }
    public static String sha384Hex(byte[] src){
        return output(sha384(src),MESSAGE_DIGEST_OUTPUT_TYPE_HEX);
    }
    public static String sha384Hex(byte[] src,int offset,int length){
        return output(sha384(src,offset,length),MESSAGE_DIGEST_OUTPUT_TYPE_HEX);
    }
    public static String sha384Hex(String src, String charset){
        return messageDigest(src,charset,MESSAGE_DIGEST_SHA384,MESSAGE_DIGEST_OUTPUT_TYPE_HEX);
    }
    public static String sha384Base64(String src, String charset){
        return messageDigest(src,charset,MESSAGE_DIGEST_SHA384,MESSAGE_DIGEST_OUTPUT_TYPE_BASE64);
    }
    public static String sha384Hex(String src){
        return messageDigest(src,DEFAULT_CHARSET,MESSAGE_DIGEST_SHA384,MESSAGE_DIGEST_OUTPUT_TYPE_HEX);
    }
    public static String sha384Base64(String src){
        return messageDigest(src,DEFAULT_CHARSET,MESSAGE_DIGEST_SHA384,MESSAGE_DIGEST_OUTPUT_TYPE_BASE64);
    }
    private static final ThreadLocal<Map<String,Mac>> MAC_CACHE=new ThreadLocal<>();
    private static Mac getInstance(String key,String charset,String algorithm) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException{
        Map<String,Mac> map=MAC_CACHE.get();
        if(map==null){
            map=new HashMap<>();
            MAC_CACHE.set(map);
        }
        String tag=key+algorithm;
        Mac mac=map.get(tag);
        if(mac==null){
            SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(charset), algorithm);
            mac=Mac.getInstance(algorithm);
            mac.init(signingKey);
            map.put(tag,mac);
        }
        return mac;
    }
	public static byte[] hmac(byte[] src,String key,String charset,String algorithm) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException{
		Mac mac=getInstance(key,charset,algorithm);
        try{
            return mac.doFinal(src);
        }finally{
            if(mac!=null){
                mac.reset();
            }
        }
	}
	public static String hmacBase64(byte[] src,String key,String charset,String algorithm) throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException{
		return output(hmac(src,key,charset,algorithm),MESSAGE_DIGEST_OUTPUT_TYPE_BASE64);
	}
	public static String hmacHex(byte[] src,String key,String charset,String algorithm) throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException{
		return output(hmac(src,key,charset,algorithm),MESSAGE_DIGEST_OUTPUT_TYPE_HEX);
	}
	public static String hmac(String src,String key, String algorithm,int outputType, String charset) throws UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException{
        return output(hmac(src.getBytes(charset),key,charset,algorithm),outputType);
    }
    public static String hmacBase64(String src,String key,String algorithm,String charset) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException{
        return hmac(src,key,algorithm,MessageDigestUtil.MESSAGE_DIGEST_OUTPUT_TYPE_BASE64,charset);
    }
    public static String hmacHex(String src,String key,String algorithm,String charset) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException{
        return hmac(src,key,algorithm,MessageDigestUtil.MESSAGE_DIGEST_OUTPUT_TYPE_HEX,charset);
    }
    
	public static byte[] hmacSha1(byte[] src,String key,String charset) throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException{
		return hmac(src,key,charset,MESSAGE_DIGEST_HMAC_SHA1);
	}
	public static String hmacSha1Base64(byte[] src,String key,String charset) throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException{
		return output(hmacSha1(src,key,charset),MESSAGE_DIGEST_OUTPUT_TYPE_BASE64);
	}
	public static String hmacSha1Hex(byte[] src,String key,String charset) throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException{
		return output(hmacSha1(src,key,charset),MESSAGE_DIGEST_OUTPUT_TYPE_HEX);
	}
	public static String hmacSha1(String src,String key,int outputType,String charset) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException{
		return hmac(src,key,MESSAGE_DIGEST_HMAC_SHA1,outputType,charset);
	}
	public static String hmacSha1Base64(String src,String key,String charset) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException{
		return hmacBase64(src,key,MESSAGE_DIGEST_HMAC_SHA1,charset);
	}
	public static String hmacSha1Hex(String src,String key,String charset) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException{
		return hmacHex(src,key,MESSAGE_DIGEST_HMAC_SHA1,charset);
	}
	
	public static byte[] hmacSha256(byte[] src,String key,String charset) throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException{
        return hmac(src,key,charset,MESSAGE_DIGEST_HMAC_SHA256);
    }
    public static String hmacSha256Base64(byte[] src,String key,String charset) throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException{
        return output(hmacSha256(src,key,charset),MESSAGE_DIGEST_OUTPUT_TYPE_BASE64);
    }
    public static String hmac256Hex(byte[] src,String key,String charset) throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException{
        return output(hmacSha256(src,key,charset),MESSAGE_DIGEST_OUTPUT_TYPE_HEX);
    }
    public static String hmacSha256Hex(byte[] src,String key,String charset) throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException{
        return output(hmacSha256(src,key,charset),MESSAGE_DIGEST_OUTPUT_TYPE_HEX);
    }
    public static String hmacSha256(String src,String key,int outputType,String charset) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException{
        return hmac(src,key,MESSAGE_DIGEST_HMAC_SHA256,outputType,charset);
    }
    public static String hmacSha256Base64(String src,String key,String charset) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException{
        return hmacBase64(src,key,MESSAGE_DIGEST_HMAC_SHA256,charset);
    }
    public static String hmacSha256Hex(String src,String key,String charset) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException{
        return hmacHex(src,key,MESSAGE_DIGEST_HMAC_SHA256,charset);
    }
	
    public static byte[] hmacSha384(byte[] src,String key,String charset) throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException{
        return hmac(src,key,charset,MESSAGE_DIGEST_HMAC_SHA384);
    }
    public static String hmacSha384Base64(byte[] src,String key,String charset) throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException{
        return output(hmacSha384(src,key,charset),MESSAGE_DIGEST_OUTPUT_TYPE_BASE64);
    }
    public static String hmac384Hex(byte[] src,String key,String charset) throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException{
        return output(hmacSha384(src,key,charset),MESSAGE_DIGEST_OUTPUT_TYPE_HEX);
    }
    public static String hmacSha384Hex(byte[] src,String key,String charset) throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException{
        return output(hmacSha384(src,key,charset),MESSAGE_DIGEST_OUTPUT_TYPE_HEX);
    }
    public static String hmacSha384(String src,String key,int outputType,String charset) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException{
        return hmac(src,key,MESSAGE_DIGEST_HMAC_SHA384,outputType,charset);
    }
    public static String hmacSha384Base64(String src,String key,String charset) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException{
        return hmacBase64(src,key,MESSAGE_DIGEST_HMAC_SHA384,charset);
    }
    public static String hmacSha384Hex(String src,String key,String charset) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException{
        return hmacHex(src,key,MESSAGE_DIGEST_HMAC_SHA384,charset);
    }
    
    public static byte[] hmacSha512(byte[] src,String key,String charset) throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException{
        return hmac(src,key,charset,MESSAGE_DIGEST_HMAC_SHA512);
    }
    public static String hmacSha512Base64(byte[] src,String key,String charset) throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException{
        return output(hmacSha512(src,key,charset),MESSAGE_DIGEST_OUTPUT_TYPE_BASE64);
    }
    public static String hmac512Hex(byte[] src,String key,String charset) throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException{
        return output(hmacSha512(src,key,charset),MESSAGE_DIGEST_OUTPUT_TYPE_HEX);
    }
    public static String hmacSha512Hex(byte[] src,String key,String charset) throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException{
        return output(hmacSha512(src,key,charset),MESSAGE_DIGEST_OUTPUT_TYPE_HEX);
    }
    public static String hmacSha512(String src,String key,int outputType,String charset) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException{
        return hmac(src,key,MESSAGE_DIGEST_HMAC_SHA512,outputType,charset);
    }
    public static String hmacSha512Base64(String src,String key,String charset) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException{
        return hmacBase64(src,key,MESSAGE_DIGEST_HMAC_SHA512,charset);
    }
    public static String hmacSha512Hex(String src,String key,String charset) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException{
        return hmacHex(src,key,MESSAGE_DIGEST_HMAC_SHA512,charset);
    }
    
    public static byte[] hmacMd2(byte[] src,String key,String charset) throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException{
        return hmac(src,key,charset,MESSAGE_DIGEST_HMAC_MD2);
    }
    public static String hmacMd2Base64(byte[] src,String key,String charset) throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException{
        return output(hmacMd2(src,key,charset),MESSAGE_DIGEST_OUTPUT_TYPE_BASE64);
    }
    public static String hmacMd2Hex(byte[] src,String key,String charset) throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException{
        return output(hmacMd2(src,key,charset),MESSAGE_DIGEST_OUTPUT_TYPE_HEX);
    }
    public static String hmacMd2(String src,String key,int outputType,String charset) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException{
        return hmac(src,key,MESSAGE_DIGEST_HMAC_MD2,outputType,charset);
    }
    public static String hmacMd2Base64(String src,String key,String charset) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException{
        return hmacBase64(src,key,MESSAGE_DIGEST_HMAC_MD2,charset);
    }
    public static String hmacMd2Hex(String src,String key,String charset) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException{
        return hmacHex(src,key,MESSAGE_DIGEST_HMAC_MD2,charset);
    }
    
    public static byte[] hmacMd5(byte[] src,String key,String charset) throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException{
        return hmac(src,key,charset,MESSAGE_DIGEST_HMAC_MD5);
    }
    public static String hmacMd5Base64(byte[] src,String key,String charset) throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException{
        return output(hmacMd5(src,key,charset),MESSAGE_DIGEST_OUTPUT_TYPE_BASE64);
    }
    public static String hmacMd5Hex(byte[] src,String key,String charset) throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException{
        return output(hmacMd5(src,key,charset),MESSAGE_DIGEST_OUTPUT_TYPE_HEX);
    }
    public static String hmacMd5(String src,String key,int outputType,String charset) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException{
        return hmac(src,key,MESSAGE_DIGEST_HMAC_MD5,outputType,charset);
    }
    public static String hmacMd5Base64(String src,String key,String charset) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException{
        return hmacBase64(src,key,MESSAGE_DIGEST_HMAC_MD5,charset);
    }
    public static String hmacMd5Hex(String src,String key,String charset) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException{
        return hmacHex(src,key,MESSAGE_DIGEST_HMAC_MD5,charset);
    }
    
    
    public static byte[] pkbdf(char[] src, byte[] salt, int iterations,int bytes,String algorithms) throws NoSuchAlgorithmException, InvalidKeySpecException, DestroyFailedException{
        PBEKeySpec spec = new PBEKeySpec(src, salt, iterations, bytes * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(algorithms);
        SecretKey sk=skf.generateSecret(spec);
        return sk.getEncoded();
    }
    public static byte[] pbkdf2HmacSha1(char[] src, byte[] salt, int iterations, int bytes)
            throws NoSuchAlgorithmException, InvalidKeySpecException, DestroyFailedException{
        return pkbdf(src,salt,iterations,bytes,PBKDF2_HMAC_SHA1);
    }
    public static String pbkdf2HmacSha1(String src, String salt,int iterations, int bytes, int outputType) throws NoSuchAlgorithmException, InvalidKeySpecException, DestroyFailedException{
        return output(pbkdf2HmacSha1(src.toCharArray(),salt.getBytes(),iterations,bytes),outputType);
    }
    public static String pbkdf2HmacSha1Base64(String src, String salt,int iterations, int bytes) throws NoSuchAlgorithmException, InvalidKeySpecException, DestroyFailedException{
        return pbkdf2HmacSha1(src,salt,iterations,bytes,MESSAGE_DIGEST_OUTPUT_TYPE_BASE64);
    }
    public static String pbkdf2HmacSha1Hex(String src, String salt,int iterations, int bytes) throws NoSuchAlgorithmException, InvalidKeySpecException, DestroyFailedException{
        return pbkdf2HmacSha1(src,salt,iterations,bytes,MESSAGE_DIGEST_OUTPUT_TYPE_HEX);
    }
    
    public static byte[] pbkdf2HmacSha256(char[] src, byte[] salt, int iterations, int bytes)
            throws NoSuchAlgorithmException, InvalidKeySpecException, DestroyFailedException{
        return pkbdf(src,salt,iterations,bytes,PBKDF2_HMAC_SHA256);
    }
    public static String pbkdf2HmacSha256(String src, String salt,int iterations, int bytes, int outputType) throws NoSuchAlgorithmException, InvalidKeySpecException, DestroyFailedException{
        return output(pbkdf2HmacSha256(src.toCharArray(),salt.getBytes(),iterations,bytes),outputType);
    }
    public static String pbkdf2HmacSha256Base64(String src, String salt,int iterations, int bytes) throws NoSuchAlgorithmException, InvalidKeySpecException, DestroyFailedException{
        return pbkdf2HmacSha256(src,salt,iterations,bytes,MESSAGE_DIGEST_OUTPUT_TYPE_BASE64);
    }
    public static String pbkdf2HmacSha256Hex(String src, String salt,int iterations, int bytes) throws NoSuchAlgorithmException, InvalidKeySpecException, DestroyFailedException{
        return pbkdf2HmacSha256(src,salt,iterations,bytes,MESSAGE_DIGEST_OUTPUT_TYPE_HEX);
    }
    
    public static byte[] pbkdf2HmacSha384(char[] src, byte[] salt, int iterations, int bytes)
            throws NoSuchAlgorithmException, InvalidKeySpecException, DestroyFailedException{
        return pkbdf(src,salt,iterations,bytes,PBKDF2_HMAC_SHA384);
    }
    public static String pbkdf2HmacSha384(String src, String salt,int iterations, int bytes, int outputType) throws NoSuchAlgorithmException, InvalidKeySpecException, DestroyFailedException{
        return output(pbkdf2HmacSha384(src.toCharArray(),salt.getBytes(),iterations,bytes),outputType);
    }
    public static String pbkdf2HmacSha384Base64(String src, String salt,int iterations, int bytes) throws NoSuchAlgorithmException, InvalidKeySpecException, DestroyFailedException{
        return pbkdf2HmacSha384(src,salt,iterations,bytes,MESSAGE_DIGEST_OUTPUT_TYPE_BASE64);
    }
    public static String pbkdf2HmacSha384Hex(String src, String salt,int iterations, int bytes) throws NoSuchAlgorithmException, InvalidKeySpecException, DestroyFailedException{
        return pbkdf2HmacSha384(src,salt,iterations,bytes,MESSAGE_DIGEST_OUTPUT_TYPE_HEX);
    }
    
    public static byte[] pbkdf2HmacSha512(char[] src, byte[] salt, int iterations, int bytes)
            throws NoSuchAlgorithmException, InvalidKeySpecException, DestroyFailedException{
        return pkbdf(src,salt,iterations,bytes,PBKDF2_HMAC_SHA512);
    }
    public static String pbkdf2HmacSha512(String src, String salt,int iterations, int bytes, int outputType) throws NoSuchAlgorithmException, InvalidKeySpecException, DestroyFailedException{
        return output(pbkdf2HmacSha512(src.toCharArray(),salt.getBytes(),iterations,bytes),outputType);
    }
    public static String pbkdf2HmacSha512Base64(String src, String salt,int iterations, int bytes) throws NoSuchAlgorithmException, InvalidKeySpecException, DestroyFailedException{
        return pbkdf2HmacSha512(src,salt,iterations,bytes,MESSAGE_DIGEST_OUTPUT_TYPE_BASE64);
    }
    public static String pbkdf2HmacSha512Hex(String src, String salt,int iterations, int bytes) throws NoSuchAlgorithmException, InvalidKeySpecException, DestroyFailedException{
        return pbkdf2HmacSha512(src,salt,iterations,bytes,MESSAGE_DIGEST_OUTPUT_TYPE_HEX);
    }
}