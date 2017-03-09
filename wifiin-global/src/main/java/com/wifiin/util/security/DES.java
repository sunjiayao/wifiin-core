package com.wifiin.util.security;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

import com.wifiin.common.CommonConstant;

public class DES {
	public static final String DES_AlGORITHM="DES";
	public static final String DESEDE_ALGORITHM="DESede";
	public static final String PKCS5_PADDING="PKCS5";
	public static final String PKCS7_PADDING="PKCS7";
	
	public static final String secureAlg="SHA1PRNG";
	public static final String algorithm="DESede";
	public static final String PADDING="DESede/CBC/PKCS5Padding";

	private IvParameterSpec iv;
	private SecretKey secretKey;
	private String padding;
	private String charset;
	
	private DES(){}
	
	public static DES init(String algorithm,String padding,String key,String iv) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, UnsupportedEncodingException{
		return init(algorithm,padding,secureAlg,key,iv,CommonConstant.DEFAULT_CHARSET_NAME);
	}
	public static DES init(String algorithm,String padding,String key,String iv,String charset) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, UnsupportedEncodingException{
		return init(algorithm,padding,secureAlg,key,iv,charset);
	}
	public static DES init(String algorithm,String padding,String secureAlg,String key,String iv,String charset) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, UnsupportedEncodingException{
		return init(algorithm,padding,secureAlg,key.getBytes(charset),iv.getBytes(charset),charset);
	}
	
	public static DES init(String algorithm,String padding,byte[] key,byte[] iv) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException{
		return init(algorithm,padding,key,iv,CommonConstant.DEFAULT_CHARSET_NAME);
	}
	public static DES init(String algorithm,String padding,byte[] key, byte[] iv,String charset) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException{
		return init(algorithm,padding,secureAlg,key,iv,charset);
	}
	public static DES init(String algorithm, String padding,String secureAlg,byte[] key, byte[] iv,String charset) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException{
		DES encryptor=new DES();
		SecureRandom secure=SecureRandom.getInstance(secureAlg);
		secure.setSeed(key);
		KeyGenerator kgen=KeyGenerator.getInstance(algorithm);
		kgen.init(secure);
		DESedeKeySpec spec = new DESedeKeySpec(kgen.generateKey().getEncoded());  
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(algorithm);  
        encryptor.secretKey = keyFactory.generateSecret(spec);  
        encryptor.iv = new IvParameterSpec(iv);
        encryptor.padding=padding;
        encryptor.charset=charset;
		return encryptor;
	}
	public byte[] cipher(byte[] content, int mode) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchProviderException{
        Cipher cipher = Cipher.getInstance(padding);  
        cipher.init(mode, secretKey, iv);  
        return cipher.doFinal(content);
	}

	public byte[] encrypt(byte[] src) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchProviderException{
		return cipher(src,Cipher.ENCRYPT_MODE);
	}
	public byte[] decrypt(byte[] src) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchProviderException{
		return cipher(src,Cipher.DECRYPT_MODE);
	}

	public String encryptToBase64(String src) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchProviderException, UnsupportedEncodingException{
		return Base64.encodeBase64String(encrypt(src.getBytes(charset)));
	}
	public String decryptFromBase64(String src) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchProviderException, UnsupportedEncodingException{
		return new String(decrypt(Base64.decodeBase64(src)),charset);
	}
	public static void main(String[] args) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchProviderException, UnsupportedEncodingException, InvalidKeySpecException {
		String key="ABCDEFGHIJ0123456789ABCD";
		String iv="A6xrvqTZ";
		String content="this is a test，包括中文";
		DES des=init(algorithm,PADDING,secureAlg,key,iv,"utf8");
		
		String encrypted=des.encryptToBase64(content);
		System.out.println(encrypted);
		System.out.println(des.decryptFromBase64(encrypted));
		
		KeyGenerator kgen=KeyGenerator.getInstance(algorithm);
		SecureRandom secure=SecureRandom.getInstance(secureAlg);
		secure.setSeed(key.getBytes());
		kgen.init(secure);
		
//        SecretKey sec = keyFactory.generateSecret(spec);
        SecretKey sec=new SecretKeySpec(key.getBytes(), algorithm);//本行与上一行可以互相代替，但是加密结果不同
        IvParameterSpec ivParameters = new IvParameterSpec(iv.getBytes());  

        Cipher cipher = Cipher.getInstance(PADDING);  
        cipher.init(Cipher.ENCRYPT_MODE, sec, ivParameters);
        encrypted=Base64.encodeBase64String(cipher.doFinal(content.getBytes()));
        System.out.println(encrypted);  
        
        cipher=Cipher.getInstance(PADDING);
        cipher.init(Cipher.DECRYPT_MODE, sec, ivParameters);
        System.out.println(new String(cipher.doFinal(Base64.decodeBase64(encrypted))));
	}
}
