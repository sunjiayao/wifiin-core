package com.wifiin.util.security;

/**													
 *  LICENSE AND TRADEMARK NOTICES													
 *  													
 *  Except where noted, sample source code written by Motorola Mobility Inc. and													
 *  provided to you is licensed as described below.													
 *  													
 *  Copyright (c) 2012, Motorola, Inc.													
 *  All  rights reserved except as otherwise explicitly indicated.													
 *													
 *  Redistribution and use in source and binary forms, with or without													
 *  modification, are permitted provided that the following conditions are met:													
 *													
 *  - Redistributions of source code must retain the above copyright notice,													
 *  this list of conditions and the following disclaimer.													
 *													
 *  - Redistributions in binary form must reproduce the above copyright notice,													
 *  this list of conditions and the following disclaimer in the documentation													
 *  and/or other materials provided with the distribution.													
 *													
 *  - Neither the name of Motorola, Inc. nor the names of its contributors may													
 *  be used to endorse or promote products derived from this software without													
 *  specific prior written permission.													
 *													
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"													
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE													
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE													
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE													
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR													
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF													
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS													
 *  INTERRUPTION) HOWEVER  CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN													
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)													
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE													
 *  POSSIBILITY OF SUCH DAMAGE.													
 *  													
 *  Other source code displayed may be licensed under Apache License, Version													
 *  2.													
 *  													
 *  Copyright 卢漏 2012, Android Open Source Project. All rights reserved unless													
 *  otherwise explicitly indicated.													
 *  													
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not													
 *  use this file except in compliance with the License. You may obtain a copy													
 *  of the License at													
 *  													
 *  http://www.apache.org/licenses/LICENSE-2.0.													
 *  													
 *  Unless required by applicable law or agreed to in writing, software													
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT													
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the													
 *  License for the specific language governing permissions and limitations													
 *  under the License.													
 *  													
 */													
													
// Please refer to the accompanying article at 													
// http://developer.motorola.com/docs/using_the_advanced_encryption_standard_in_android/													
													
// A tutorial guide to using AES encryption in Android													
// First we generate a 256 bit secret key; then we use that secret key to AES encrypt a plaintext message.													
// Finally we decrypt the ciphertext to get our original message back.													
// We don't keep a copy of the secret key - we generate the secret key whenever it is needed, 													
// so we must remember all the parameters needed to generate it -													
// the salt, the IV, the human-friendly passphrase, all the algorithms and parameters to those algorithms.													
// Peter van der Linden, April 15 2012													
													
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.collect.Maps;
import com.wifiin.common.CommonConstant;
import com.wifiin.util.Help;
import com.wifiin.util.digest.MessageDigestUtil;

public class EnhancedSymmetricEncryptor {
	public static final String KEY_GENERATION_ALGPBEWITHSHAANDTWOFISH_CBC = "PBEWITHSHAANDTWOFISH-CBC";
	public static final String KEY_GENERATION_ALG_PBKDF2WithHmacSHA1 = MessageDigestUtil.PBKDF2_HMAC_SHA1;//"PBKDF2WithHmacSHA1";
	public static final String KEY_GENERATION_ALG_PBKDF2WithHmacSHA256 = MessageDigestUtil.PBKDF2_HMAC_SHA256;//"PBKDF2WithHmacSHA256";
    public static final String KEY_GENERATION_ALG_PBKDF2WithHmacSHA384 = MessageDigestUtil.PBKDF2_HMAC_SHA384;//"PBKDF2WithHmacSHA384";
    public static final String KEY_GENERATION_ALG_PBKDF2WithHmacSHA512 = MessageDigestUtil.PBKDF2_HMAC_SHA512;//"PBKDF2WithHmacSHA512";
	private static final byte[] salt={ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF };
	private static final byte[] ivbytes={ 0xA, 1, 0xB, 5, 4, 0xF, 7, 9, 0x17, 3, 1, 6, 8, 0xC, 0xD, 91 };
	private static final String secureAlg="SHA1PRNG";
	private static final String padding="PKCS7";
	public static final String ECB_MODE="ECB";
	public static final String CBC_MODE="CBC";
	public static final String CFB_MODE="CFB";
	public static final String OFB_MODE="OFB";
	private static final int hashIterations=100;
	private static final int keyLength=256;
	private String cipherModePadding;
	private SecretKeySpec sk = null;												
	private IvParameterSpec iv;
	private SecureRandom secure;
	private String charset;
	public String getCipherModePadding() {
		return cipherModePadding;
	}
	public void setCipherModePadding(String cipherModePadding) {
		this.cipherModePadding = cipherModePadding;
	}
	public SecretKeySpec getSk() {
		return sk;
	}
	public void setSk(SecretKeySpec sk) {
		this.sk = sk;
	}
	public IvParameterSpec getIv() {
		return iv;
	}
	public void setIv(IvParameterSpec iv) {
		this.iv = iv;
	}
	public SecureRandom getSecure() {
		return secure;
	}
	public void setSecure(SecureRandom secure) {
		this.secure = secure;
	}
	public String getCharset() {
		return charset;
	}
	public void setCharset(String charset) {
		this.charset = charset;
	}
	public EnhancedSymmetricEncryptor(){}
	public EnhancedSymmetricEncryptor(String algorithm,String keyFactoryAlgorithm,String secureAlgorithm,String padding,int hashIterations,int keyLength,char[] key,byte[] salt, byte[] iv) throws NoSuchAlgorithmException, InvalidKeySpecException{
			this(algorithm,keyFactoryAlgorithm,secureAlgorithm,padding,hashIterations,keyLength,key,salt,iv,"utf8");
	}
	public EnhancedSymmetricEncryptor(String algorithm,String keyFactoryAlgorithm,String secureAlgorithm,String padding,int hashIterations,int keyLength,char[] key,byte[] salt, byte[] iv, String charset) throws NoSuchAlgorithmException, InvalidKeySpecException{
		this(algorithm, keyFactoryAlgorithm, secureAlgorithm, CBC_MODE, padding, hashIterations, keyLength, key, salt, iv, charset);
	}
	public EnhancedSymmetricEncryptor(String algorithm,String keyFactoryAlgorithm,String secureAlgorithm,String mode,String padding,int hashIterations,int keyLength,char[] key,byte[] salt, byte[] iv, String charset) throws NoSuchAlgorithmException, InvalidKeySpecException{
	    this.charset=charset;
        this.iv = new IvParameterSpec(iv);
        cipherModePadding=algorithm+"/"+mode+"/"+padding+"Padding";
        secure=SecureRandom.getInstance(secureAlgorithm);
        secure.setSeed(salt);
        this.sk = new SecretKeySpec(
                SecretKeyFactory.getInstance(keyFactoryAlgorithm).generateSecret(new PBEKeySpec(key, salt, hashIterations, keyLength)).getEncoded(), algorithm);
	}
	public EnhancedSymmetricEncryptor(String algorithm,String keyFactoryAlgorithm,String secureAlgorithm,String padding,int hashIterations,int keyLength,String key,String salt, String iv, String charset) throws NoSuchAlgorithmException, InvalidKeySpecException, UnsupportedEncodingException{
		this(algorithm,keyFactoryAlgorithm,secureAlgorithm,padding,hashIterations,keyLength,key.toCharArray(),salt.getBytes(charset),iv.getBytes(charset),charset);
	}
	public EnhancedSymmetricEncryptor(String algorithm,String keyFactoryAlgorithm,String secureAlgorithm,String padding,int hashIterations,int keyLength,String key,String salt, String iv) throws NoSuchAlgorithmException, InvalidKeySpecException, UnsupportedEncodingException{
		this(algorithm,keyFactoryAlgorithm,secureAlgorithm,padding,hashIterations,keyLength,key.toCharArray(),salt.getBytes("utf8"),iv.getBytes("utf8"),"utf8");
	}
	public EnhancedSymmetricEncryptor(String algorithm,String key,String charset) throws NoSuchAlgorithmException, InvalidKeySpecException, UnsupportedEncodingException{
		this(algorithm,KEY_GENERATION_ALG_PBKDF2WithHmacSHA1,secureAlg,padding,hashIterations,keyLength,key.toCharArray(),salt,ivbytes,charset);
	}
	public EnhancedSymmetricEncryptor(String algorithm,String key,String salt, String iv,String charset) throws NoSuchAlgorithmException, InvalidKeySpecException, UnsupportedEncodingException{
		this(algorithm,KEY_GENERATION_ALG_PBKDF2WithHmacSHA1,secureAlg,padding,hashIterations,keyLength,key.toCharArray(),salt.getBytes(charset),iv.getBytes(charset),charset);
	}
	public EnhancedSymmetricEncryptor(String algorithm,String key) throws NoSuchAlgorithmException, InvalidKeySpecException, UnsupportedEncodingException{
		this(algorithm,KEY_GENERATION_ALG_PBKDF2WithHmacSHA1,secureAlg,padding,hashIterations,keyLength,key.toCharArray(),salt,ivbytes,"utf8");
	}
	private EnhancedSymmetricEncryptor(EnhancedSymmetricEncryptorParam param) throws NoSuchAlgorithmException, InvalidKeySpecException{
	    this(param.algorithm, param.keyFactoryAlgorithm, param.secureAlgorithm, param.mode, param.padding, param.hashIterations, param.keyLength, 
	            param.key, param.salt, param.iv, param.charset);
	}
	private static class EnhancedSymmetricEncryptorParam{
	    public String algorithm;
	    public String keyFactoryAlgorithm=KEY_GENERATION_ALG_PBKDF2WithHmacSHA1;
	    public String secureAlgorithm=secureAlg;
	    public String mode=CBC_MODE;
	    public String padding=EnhancedSymmetricEncryptor.padding;
	    public int hashIterations=EnhancedSymmetricEncryptor.hashIterations;
	    public int keyLength=EnhancedSymmetricEncryptor.keyLength;
	    public char[] key;
	    public byte[] salt=EnhancedSymmetricEncryptor.salt;
	    public byte[] iv=ivbytes;
	    public String charset=CommonConstant.DEFAULT_CHARSET_NAME;
	    public EnhancedSymmetricEncryptorParam(){}
        public EnhancedSymmetricEncryptorParam(String algorithm,String keyFactoryAlgorithm,String secureAlgorithm,
                String mode,String padding,int hashIterations,int keyLength,char[] key,byte[] salt,byte[] iv,
                String charset){
            this.algorithm=algorithm;
            this.keyFactoryAlgorithm=keyFactoryAlgorithm;
            this.secureAlgorithm=secureAlgorithm;
            this.mode=mode;
            this.padding=padding;
            this.hashIterations=hashIterations;
            this.keyLength=keyLength;
            this.key=key;
            this.salt=salt;
            this.iv=iv;
            this.charset=charset;
        }
        public void setKey(String key){
            this.key=key.toCharArray();
        }
        public void setSalt(String salt) throws UnsupportedEncodingException{
            if(Help.isNotEmpty(salt)){
                this.salt=salt.getBytes(charset);
            }
        }
        public void setIv(String iv) throws UnsupportedEncodingException{
            if(Help.isNotEmpty(salt)){
                this.iv=iv.getBytes(charset);
            }
        }
        private int hash;
        public int hashCode(){
            if(hash==0){
                hash=new HashCodeBuilder().append(algorithm).append(keyFactoryAlgorithm).append(secureAlgorithm)
                        .append(mode).append(padding).append(hashIterations).append(keyLength).append(key).append(salt).append(iv).append(charset).toHashCode();
            }
            return hash;
        }
        public boolean equals(Object o){
            if(o==null){
                return false;
            }
            if(o==this){
                return true;
            }
            if(!(o instanceof EnhancedSymmetricEncryptorParam)){
                return false;
            }
            EnhancedSymmetricEncryptorParam p=(EnhancedSymmetricEncryptorParam)o;
            return new EqualsBuilder().append(algorithm,p.algorithm).append(keyFactoryAlgorithm,p.keyFactoryAlgorithm).append(secureAlgorithm,p.secureAlgorithm)
                    .append(mode,p.mode).append(padding,p.padding).append(hashIterations,p.hashIterations).append(keyLength,p.keyLength)
                    .append(key,p.key).append(salt,p.salt).append(iv,p.iv).append(charset,p.charset).isEquals();
        }
	}
	private static final ThreadLocal<Map<EnhancedSymmetricEncryptorParam,EnhancedSymmetricEncryptor>> ENCRYPTOR_PARAMS=new ThreadLocal<>();
	private static Map<EnhancedSymmetricEncryptorParam,EnhancedSymmetricEncryptor> getEncryptorMap(){
	    Map<EnhancedSymmetricEncryptorParam,EnhancedSymmetricEncryptor> map=ENCRYPTOR_PARAMS.get();
        if(map==null){
            map=Maps.newHashMap();
            ENCRYPTOR_PARAMS.set(map);
        }
        return map;
	}
	public static EnhancedSymmetricEncryptor getAES(String key) throws NoSuchAlgorithmException, InvalidKeySpecException, UnsupportedEncodingException{
	    return getAES(key,null,null,null);
	}
	public static EnhancedSymmetricEncryptor getAES(String key,String charset) throws NoSuchAlgorithmException, InvalidKeySpecException, UnsupportedEncodingException{
	    return getAES(key,null,null,charset);
	}
	public static EnhancedSymmetricEncryptor getAES(String key,String salt,String iv,String charset) throws NoSuchAlgorithmException, InvalidKeySpecException, UnsupportedEncodingException{
	    Map<EnhancedSymmetricEncryptorParam,EnhancedSymmetricEncryptor> map=getEncryptorMap();
        EnhancedSymmetricEncryptorParam p=new EnhancedSymmetricEncryptorParam();
        p.algorithm="AES";
        p.setKey(key);
        p.setSalt(salt);
        p.setIv(iv);
        return map.computeIfAbsent(p,(param)->{
            try{
                return new EnhancedSymmetricEncryptor(param);
            }catch(NoSuchAlgorithmException | InvalidKeySpecException e){
                throw new SecurityException(e);
            }
        });
	}
	public String encryptToBase64(String src) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException{
		return encryptToBase64(src.getBytes(charset));
	}
	public String encryptToBase64(byte[] src) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		return Base64.encodeBase64String(encrypt(src));
	}
	public byte[] encrypt(byte[] src) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException{
		return cipher(Cipher.ENCRYPT_MODE,src);
	}

	public String decryptFromBase64(String src) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {												
		return new String(decrypt(Base64.decodeBase64(src)),charset);
	}
	public byte[] decrypt(byte[] src) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException{
		return cipher(Cipher.DECRYPT_MODE,src);
	}
	private byte[] cipher(int mode,byte[] src) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException{
		Cipher c = Cipher.getInstance(cipherModePadding);
		c.init(mode, sk, iv,secure);
		return c.doFinal(src);
	}						
	public static void main(String[] args) throws UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException {
//		long l=0;
//		String encrypt=null;
//		for(int i=0;i<10000;i++){
//			String src=RandomStringUtils.random(300, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789+=-_:\",=\\/$#;][{}");
//			long s=System.currentTimeMillis();
//			encrypt=getAES("19hlcHCh13070t43","k760jv5Ab1HdPffs","yx30x0j603N9zjgm","UTF-8").encryptToBase64(src);
//			l+=(System.currentTimeMillis()-s);
//		}
//		System.out.println(l+"    "+l/10000.0);
//		System.out.println(encrypt.length()+"    "+encrypt);
//		System.out.println(getAES("r1pcL0e9jJfur0Zv","Pwwgspjh0ukzupxy","zifteQKXAqzlaJ5w","UTF-8").decryptFromBase64("69gRLRUPO3Kz0evAr0XAbw=="));
//		System.out.println("4bKD8T/2zyzebKxCH0pMyAv0xi6zLJSatUOskOF3fY0ty+itTEdzMlhY9/iJqbrM".length());
//		System.out.println(getAES("19hlcHCh13070t43","k760jv5Ab1HdPffs","yx30x0j603N9zjgm","UTF-8").decryptFromBase64("vAu8j0fo3auHBxUzvaxxrQ=="));
	}
}
