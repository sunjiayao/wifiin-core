package com.wifiin.util.security;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * RSA命令行，
 * 第一个参数标识使用公钥还是私钥，执行加密或解密
 * puben:公钥加密
 * pubde:公钥解密
 * prien:私钥加密
 * pride:私钥解密
 * 第二个参数是密钥
 * 第三个参数是待加密或解密的内容
 * 加解密的结果输出到控制台
 * RSACmd puben key content 
 * RSACmd pubde key content
 * RSACmd prien key content
 * RSACmd pride key content
 * @author wujingrun
 */
public class RSACmd {
	public static void main(String[] args) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, IOException {
		switch(args[0].toLowerCase()){
		case "puben":
			System.out.println(RSA.encryptByPublicKey(args[2], args[1]));
			break;
		case "pubde":
			System.out.println(RSA.decryptByPublicKey(args[2], args[1]));
			break;
		case "prien":
			System.out.println(RSA.encryptByPrivateKey(args[2], args[1]));
			break;
		case "pride":
			System.out.println(RSA.decryptByPrivateKey(args[2], args[1]));
			break;
		}
	}
}
