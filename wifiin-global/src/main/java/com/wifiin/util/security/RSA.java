package com.wifiin.util.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.collect.Maps;
import com.wifiin.common.CommonConstant;

public class RSA{
    public static final String RSA_CRYPTO_ALGORITHMS="RSA";
    public static final int KEY_SIZE=1024;
    public static final String SHA1_WITH_RSA_SIGN_ALGORITHMS = "SHA1WithRSA";
    public static final String SHA256_WITH_RSA_SIGN_ALGORITHMS = "SHA256WithRSA";
    public static final String SHA384_WITH_RSA_SIGN_ALGORITHMS = "SHA384WithRSA";
    public static final String SHA512_WITH_RSA_SIGN_ALGORITHMS = "SHA512WithRSA";
    
    public static final String MD2_WITH_RSA_SIGN_ALGORITHMS = "MD2WithRSA";
    public static final String MD5_WITH_RSA_SIGN_ALGORITHMS = "MD5WithRSA";
    
    private static final Map<String,byte[]> KEY_CACHE=new ConcurrentHashMap<>();
    private static final ThreadLocal<byte[]> ENCRYPTION_BUF=new ThreadLocal<>();
    private static final ThreadLocal<byte[]> DECRYPTION_BUF=new ThreadLocal<>();
    /**
     * 生成密钥对
     * @param keySize  密钥对的位数，只能取值512或1024。如果传的值小于等于0，会使用1024
     * @return 返回的数组第一个元素是私钥，第二个是公钥
     * @throws NoSuchAlgorithmException
     */
    public static  Key[] generateKeyPair(int keySize) throws NoSuchAlgorithmException{
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA_CRYPTO_ALGORITHMS);
        keyPairGenerator.initialize(keySize<=0?KEY_SIZE:keySize);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        Key privateKey = keyPair.getPrivate();
        Key publicKey = keyPair.getPublic();
        return new Key[]{privateKey,publicKey};
    }
    private static byte[] getKeyBytes(String key){
        byte[] kbs=KEY_CACHE.get(key);
        if(kbs==null){
            synchronized(RSA.class){
                kbs=KEY_CACHE.get(key);
                if(kbs==null){
                    kbs=Base64.decodeBase64(key);
                    KEY_CACHE.put(key,kbs);
                }
            }
        }
        return kbs;
    }
    private static byte[] buffer(boolean encrypt){
        ThreadLocal<byte[]> bufCache=null;
        if(encrypt){
            bufCache=ENCRYPTION_BUF;
        }else{
            bufCache=DECRYPTION_BUF;
        }
        byte[] buf=bufCache.get();
        if(buf==null){
            buf=new byte[encrypt?117:128];
            bufCache.set(buf);
        }
        return buf;//每117字节一段完成加密，加密结果连成一个字节数组，就是完整的密文。解密也是
    }
    private static ThreadLocal<Map<CipherKey,CipherMeta>> cipherMetaData;
    private static class CipherKey{
        public String key;
        public int mode;
        public boolean privateKey;
        public int hash;
        public CipherKey(String key,int mode,boolean privateKey){
            this.key=key;
            this.mode=mode;
            this.privateKey=privateKey;
        }
        @Override
        public int hashCode(){
            if(hash==0){
                hash=new HashCodeBuilder().append(key).append(mode).append(privateKey).toHashCode();
            }
            return hash;
        }
        @Override
        public boolean equals(Object o){
            if(o instanceof CipherKey){
                CipherKey ck=(CipherKey)o;
                return ck.key.equals(this.key) && ck.mode==this.mode && ck.privateKey==this.privateKey;
            }
            return false;
        }
    }
    private static class CipherMeta{
        public final Cipher cipher;
        private byte[] buf;
        public final boolean encrypt;
        public CipherMeta(CipherKey cipherKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidKeySpecException{
            Cipher cipher=Cipher.getInstance(RSA_CRYPTO_ALGORITHMS);
            String key=cipherKey.key;
            cipher.init(cipherKey.mode, cipherKey.privateKey?createPrivateKey(key):createPublicKey(key));
            this.cipher=cipher;
            encrypt=cipherKey.mode==Cipher.ENCRYPT_MODE;
        }
        public byte[] getBuf(){
            return buf!=null?buf:(buf=RSA.buffer(encrypt));
        }
    }
    private static void initCipherMetaData(){
        if(cipherMetaData==null){
            synchronized(RSA.class){
                if(cipherMetaData==null){
                    cipherMetaData=new ThreadLocal<>();
                }
            }
        }
    }
    private static CipherMeta getCipher(String key, int mode, boolean privateKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidKeySpecException{
        initCipherMetaData();
        Map<CipherKey,CipherMeta> metaMap=cipherMetaData.get();
        if(metaMap==null){
            metaMap=Maps.newHashMap();
            cipherMetaData.set(metaMap);
        }
        CipherKey cipherKey=new CipherKey(key,mode,privateKey);
        CipherMeta meta=metaMap.get(cipherKey);
        if(meta==null){
            meta=new CipherMeta(cipherKey);
            metaMap.put(cipherKey,meta);
        }
        return meta;
    }
    /**
     * 得到私钥
     * @param key 密钥字符串（经过base64编码）
     * @throws NoSuchAlgorithmException 
     * @throws InvalidKeySpecException 
     * @throws Exception
     */
    private static PrivateKey createPrivateKey(String key) throws InvalidKeySpecException, NoSuchAlgorithmException{
        return KeyFactory.getInstance(RSA_CRYPTO_ALGORITHMS).generatePrivate(new PKCS8EncodedKeySpec(getKeyBytes(key)));
    }
    
    /**
     * 得到公钥
     * @param key
     * @return
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     */
    private static PublicKey createPublicKey(String key) throws InvalidKeySpecException, NoSuchAlgorithmException{
        return KeyFactory.getInstance(RSA_CRYPTO_ALGORITHMS).generatePublic(new X509EncodedKeySpec(getKeyBytes(key)));
    }
    
    private static byte[] cipher(CipherMeta cipherMeta, byte[] content,boolean encrypt) throws IllegalBlockSizeException, BadPaddingException, IOException{
        InputStream in = new ByteArrayInputStream(content);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        //rsa解密的字节大小最多是128，将待加/解密的内容，按128位拆分
        byte[] buf = cipherMeta.getBuf();
        int bufl;
        Cipher cipher=cipherMeta.cipher;
        while ((bufl = in.read(buf)) != -1) {
            out.write(cipher.doFinal(buf,0,bufl));
        }
        return out.toByteArray();
    }
    
    /**
     * 解密
     * @param content 密文
     * 
     * @param key 密钥
     * @return 解密后的字符串
     * @throws NoSuchAlgorithmException 
     * @throws InvalidKeySpecException 
     * @throws InvalidKeyException 
     * @throws NoSuchPaddingException 
     * @throws IOException 
     * @throws BadPaddingException 
     * @throws IllegalBlockSizeException 
     */
    public static String decryptByPublicKey(String content, String key) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException{
        return decrypt(content,key,false);
    }
    public static String decryptByPrivateKey(String content, String key) throws InvalidKeyException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, IOException{
        return decrypt(content,key,true);
    }
    public static byte[] decryptByPublicKey(byte[] content,String key) throws InvalidKeyException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, IOException{
        return decrypt(content,key,false);
    }
    public static byte[] decryptByPrivateKey(byte[] content,String key) throws InvalidKeyException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, IOException{
        return decrypt(content,key,true);
    }
    private static String decrypt(String content, String key, boolean privateKey) throws InvalidKeyException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, IOException{
        return new String(decrypt(Base64.decodeBase64(content),key,privateKey),CommonConstant.DEFAULT_CHARSET_NAME);
    }
    private static byte[] decrypt(byte[] content,String key,boolean privateKey) throws InvalidKeyException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, IOException{
        return cipher(getCipher(key,Cipher.DECRYPT_MODE,privateKey),content,false);
    }
    /**
     * 加密
     * @param content  明文
     * @param key      密钥
     * @return         加密后的字符串
     * @throws InvalidKeyException
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    public static String encryptByPrivateKey(String content, String key) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, IOException{
        return encrypt(content,key,true);
    }
    public static String encryptByPublicKey(String content, String key) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, IOException{
        return encrypt(content,key,false);
    }
    public static byte[] encryptByPrivateKey(byte[] content,String key) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, IOException{
        return encrypt(content,key,true);
    }
    public static byte[] encryptByPublicKey(byte[] content,String key) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, IOException{
        return encrypt(content,key,false);
    }
    private static String encrypt(String content,String key, boolean privateKey) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, IOException{
        return Base64.encodeBase64String(encrypt(content.getBytes(CommonConstant.DEFAULT_CHARSET_NAME),key,privateKey));
    }
    private static byte[] encrypt(byte[] content,String key,boolean privateKey) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, IOException{
        return cipher(getCipher(key,Cipher.ENCRYPT_MODE,privateKey),content,true);
    }
    
    
    public void decryptByPrivateKey(String srcPath, String dstPath, String key) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, FileNotFoundException, IOException{
        decryptByPrivateKey(new File(srcPath),new File(dstPath),key);
    }
    public void decryptByPrivateKey(File src, File dst, String key) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, FileNotFoundException, IOException{
        decryptByPrivateKey(new FileInputStream(src),new FileOutputStream(dst),key);
    }
    public void decryptByPrivateKey(InputStream src, OutputStream dst, String key) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, IOException{
        decrypt(src,dst,key,true);
    }
    public void decryptByPublicKey(String srcPath, String dstPath, String key) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, FileNotFoundException, IOException{
        decryptByPublicKey(new File(srcPath),new File(dstPath),key);
    }
    public void decryptByPublicKey(File src, File dst, String key) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, FileNotFoundException, IOException{
        decryptByPublicKey(new FileInputStream(src),new FileOutputStream(dst),key);
    }
    public void decryptByPublicKey(InputStream src, OutputStream dst, String key) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, IOException{
        decrypt(src,dst,key,false);
    }
    private void decrypt(InputStream src, OutputStream dst, String key, boolean privateKey) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, IOException{
        cipher(getCipher(key,Cipher.DECRYPT_MODE,privateKey),src,dst,false);
    }
    public static void encryptByPrivateKey(String srcPath, String dstPath, String key) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, FileNotFoundException, IOException{
        encryptByPrivateKey(new File(srcPath),new File(dstPath),key);
    }
    public static void encryptByPrivateKey(File src, File dst, String key) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, FileNotFoundException, IOException{
        encryptByPrivateKey(new FileInputStream(src),new FileOutputStream(dst),key);
    }
    public static void encryptByPrivateKey(InputStream src, OutputStream dst, String key) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, IOException{
        encrypt(src,dst,key,true);
    }
    public static void encryptByPublicKey(String srcPath, String dstPath, String key) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, FileNotFoundException, IOException{
        encryptByPublicKey(new File(srcPath),new File(dstPath),key);
    }
    public static void encryptByPublicKey(File src, File dst, String key) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, FileNotFoundException, IOException{
        encryptByPublicKey(new FileInputStream(src),new FileOutputStream(dst),key);
    }
    public static void encryptByPublicKey(InputStream src, OutputStream dst, String key) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, IOException{
        encrypt(src,dst,key,false);
    }
    private static void encrypt(InputStream src,OutputStream dst,String key, boolean privateKey) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, IOException{
        cipher(getCipher(key,Cipher.ENCRYPT_MODE,privateKey),src,dst,true);
    }
    private static void cipher(CipherMeta cipherMeta, InputStream src, OutputStream dst, boolean encrypt) throws IllegalBlockSizeException, BadPaddingException, IOException{
        byte[] buf=cipherMeta.getBuf();
        int bufl;
        Cipher cipher=cipherMeta.cipher;
        while((bufl=src.read(buf))!=-1){
            dst.write(cipher.doFinal(buf,0,bufl));
        }
    }
    
    
    
    /**
    * RSA签名
    * @param content 待签名数据
    * @param privateKey 商户私钥
    * @return 签名串
     * @throws NoSuchAlgorithmException 
     * @throws InvalidKeySpecException 
     * @throws InvalidKeyException 
     * @throws UnsupportedEncodingException 
     * @throws SignatureException 
    */
    public static String signSha1WithRSA(String content, String privateKey) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, SignatureException, UnsupportedEncodingException{
        return sign(content,privateKey,SHA1_WITH_RSA_SIGN_ALGORITHMS);
    }
    public static String signSha256WithRSA(String content, String privateKey) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, SignatureException, UnsupportedEncodingException{
        return sign(content,privateKey,SHA256_WITH_RSA_SIGN_ALGORITHMS);
    }
    public static String signSha384WithRSA(String content, String privateKey) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, SignatureException, UnsupportedEncodingException{
        return sign(content,privateKey,SHA384_WITH_RSA_SIGN_ALGORITHMS);
    }
    public static String signSha512WithRSA(String content, String privateKey) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, SignatureException, UnsupportedEncodingException{
        return sign(content,privateKey,SHA512_WITH_RSA_SIGN_ALGORITHMS);
    }
    public static String signMD2WithRSA(String content, String privateKey) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, SignatureException, UnsupportedEncodingException{
        return sign(content,privateKey,MD2_WITH_RSA_SIGN_ALGORITHMS);
    }
    public static String signMD5WithRSA(String content, String privateKey) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, SignatureException, UnsupportedEncodingException{
        return sign(content,privateKey,MD5_WITH_RSA_SIGN_ALGORITHMS);
    }
    
    public static String signSha1WithRSA(byte[] content, String privateKey) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, SignatureException, UnsupportedEncodingException{
        return sign(content,privateKey,SHA1_WITH_RSA_SIGN_ALGORITHMS);
    }
    public static String signSha256WithRSA(byte[] content, String privateKey) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, SignatureException, UnsupportedEncodingException{
        return sign(content,privateKey,SHA256_WITH_RSA_SIGN_ALGORITHMS);
    }
    public static String signSha384WithRSA(byte[] content, String privateKey) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, SignatureException, UnsupportedEncodingException{
        return sign(content,privateKey,SHA384_WITH_RSA_SIGN_ALGORITHMS);
    }
    public static String signSha512WithRSA(byte[] content, String privateKey) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, SignatureException, UnsupportedEncodingException{
        return sign(content,privateKey,SHA512_WITH_RSA_SIGN_ALGORITHMS);
    }
    public static String signMD2WithRSA(byte[] content, String privateKey) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, SignatureException, UnsupportedEncodingException{
        return sign(content,privateKey,MD2_WITH_RSA_SIGN_ALGORITHMS);
    }
    public static String signMD5WithRSA(byte[] content, String privateKey) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, SignatureException, UnsupportedEncodingException{
        return sign(content,privateKey,MD5_WITH_RSA_SIGN_ALGORITHMS);
    }
    
    public static String sign(String content,String privateKey,String algorithms) throws SignatureException, UnsupportedEncodingException, InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException{
        return sign(content.getBytes(CommonConstant.DEFAULT_CHARSET_NAME),privateKey,algorithms);
    }
    public static String sign(byte[] content,String privateKey,String algorithms) throws SignatureException, UnsupportedEncodingException, InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException{
        java.security.Signature signature = java.security.Signature.getInstance(algorithms);
        signature.initSign(createPrivateKey(privateKey));
        signature.update(content);
        return Base64.encodeBase64String(signature.sign());
    }
    
    
    /**
    * RSA验签
    * @param content 待验签字符串
    * @param sign 签名串
    * @param publicKey 公钥
    * @return 验签通过：true,不通过：false
     * @throws NoSuchAlgorithmException 
     * @throws InvalidKeySpecException 
     * @throws InvalidKeyException 
     * @throws UnsupportedEncodingException 
     * @throws SignatureException 
    */
    public static boolean verifySha1WithRSA(String content, String sign, String publicKey) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, SignatureException, UnsupportedEncodingException{
        return verify(content,sign,publicKey,SHA1_WITH_RSA_SIGN_ALGORITHMS);
    }
    public static boolean verifySha256WithRSA(String content, String sign, String publicKey) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, SignatureException, UnsupportedEncodingException{
        return verify(content,sign,publicKey,SHA256_WITH_RSA_SIGN_ALGORITHMS);
    }
    public static boolean verifySha384WithRSA(String content, String sign, String publicKey) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, SignatureException, UnsupportedEncodingException{
        return verify(content,sign,publicKey,SHA384_WITH_RSA_SIGN_ALGORITHMS);
    }
    public static boolean verifySha512WithRSA(String content, String sign, String publicKey) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, SignatureException, UnsupportedEncodingException{
        return verify(content,sign,publicKey,SHA512_WITH_RSA_SIGN_ALGORITHMS);
    }
    public static boolean verifyMD2WithRSA(String content, String sign, String publicKey) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, SignatureException, UnsupportedEncodingException{
        return verify(content,sign,publicKey,MD2_WITH_RSA_SIGN_ALGORITHMS);
    }
    public static boolean verifyMD5WithRSA(String content, String sign, String publicKey) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, SignatureException, UnsupportedEncodingException{
        return verify(content,sign,publicKey,MD5_WITH_RSA_SIGN_ALGORITHMS);
    }
    
    public static boolean verifySha1WithRSA(byte[] content, String sign, String publicKey) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, SignatureException, UnsupportedEncodingException{
        return verify(content,sign,publicKey,SHA1_WITH_RSA_SIGN_ALGORITHMS);
    }
    public static boolean verifySha256WithRSA(byte[] content, String sign, String publicKey) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, SignatureException, UnsupportedEncodingException{
        return verify(content,sign,publicKey,SHA256_WITH_RSA_SIGN_ALGORITHMS);
    }
    public static boolean verifySha384WithRSA(byte[] content, String sign, String publicKey) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, SignatureException, UnsupportedEncodingException{
        return verify(content,sign,publicKey,SHA384_WITH_RSA_SIGN_ALGORITHMS);
    }
    public static boolean verifySha512WithRSA(byte[] content, String sign, String publicKey) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, SignatureException, UnsupportedEncodingException{
        return verify(content,sign,publicKey,SHA512_WITH_RSA_SIGN_ALGORITHMS);
    }
    public static boolean verifyMD2WithRSA(byte[] content, String sign, String publicKey) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, SignatureException, UnsupportedEncodingException{
        return verify(content,sign,publicKey,MD2_WITH_RSA_SIGN_ALGORITHMS);
    }
    public static boolean verifyMD5WithRSA(byte[] content, String sign, String publicKey) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, SignatureException, UnsupportedEncodingException{
        return verify(content,sign,publicKey,MD5_WITH_RSA_SIGN_ALGORITHMS);
    }
    
    public static boolean verify(String content,String sign,String publicKey,String algorithms) throws NoSuchAlgorithmException, SignatureException, UnsupportedEncodingException, InvalidKeyException, InvalidKeySpecException{
        return verify(content.getBytes(CommonConstant.DEFAULT_CHARSET_NAME),sign,publicKey,algorithms);
    }
    public static boolean verify(byte[] content,String sign, String publicKey,String algorithms) throws SignatureException, InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException{
        java.security.Signature signature = java.security.Signature.getInstance(algorithms);
        signature.initVerify(createPublicKey(publicKey));
        signature.update(content);
        return signature.verify(Base64.decodeBase64(sign));
    }
    
    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, SignatureException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, IOException{
        Key[] keyPair=RSA.generateKeyPair(1024);
        String privateKey=Base64.encodeBase64String(keyPair[0].getEncoded());
        String publicKey=Base64.encodeBase64String(keyPair[1].getEncoded());
        String content="helloworld";
        String sign=RSA.signSha1WithRSA(content,privateKey);
        System.out.println(RSA.verifySha1WithRSA(content,sign,publicKey));
        
        String encrypted=RSA.encrypt(content,privateKey,true);
        System.out.println(RSA.decrypt(encrypted,publicKey,false));
        encrypted=RSA.encrypt(content,publicKey,false);
        System.out.println(RSA.decrypt(encrypted,privateKey,true));
    }
}
