package com.wifiin.util.security;

import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomStringUtils;

import com.wifiin.common.CommonConstant;

import io.netty.util.internal.ThreadLocalRandom;
/**
 * XXTea对称加密。delta是XXTEA算法的常量值，下文不再说明
 * @author Running
 *
 */
public final class XXTea {
    /**
     * 默认DELTA
     */
    private static final int DEFAULT_DELTA = 0x9E3779B9;
    /**
     * 用指定delta, 得到XXTEA对象
     * @param delta
     * @return
     */
    public static XXTea getInstance(int delta){
        return new XXTea(delta);
    }
    /**
     * 用默认DELTA创建XXTEA
     * @return
     */
    public static XXTea getInstance(){
        return getInstance(DEFAULT_DELTA);
    }
    /**
     * 使用随机DELTA创建XXTEA
     * @return
     */
    public static XXTea randomInstance(){
        return getInstance(ThreadLocalRandom.current().nextInt());
    }
    
    private int delta;
    private XXTea(int delta){
        this.delta=delta;
    }
    /**
     * XXTEA算法核心
     * 
     */
    private int MX(int sum, int y, int z, int p, int e, int[] k) {
        return (z >>> 5 ^ y << 2) + (y >>> 3 ^ z << 4) ^ (sum ^ y) + (k[p & 3 ^ e] ^ z);
    }

    private XXTea() {}
    /**
     * 加密
     * @param data 明文
     * @param key  加密KEY
     * @return  密文
     */
    public final byte[] encrypt(byte[] data, byte[] key) {
        if (data.length == 0) {
            return data;
        }
        return toByteArray(
                encrypt(toIntArray(data, true), toIntArray(fixKey(key), false)), false);
    }
    /**
     * 加密，采用默认字符集将明文转字节数组
     * @param data 明文
     * @param key 加密KEY
     * @return 密文
     */
    public final byte[] encrypt(String data, byte[] key) {
        return encrypt(data,key,CommonConstant.DEFAULT_CHARSET_NAME);
    }
    /**
     * 加密
     * @param data 明文
     * @param key  加密KEY
     * @param charset 明文字符串加密前需要转成字节数组，这个参数用来指定转字节数组的字符集
     * @return
     */
    public final byte[] encrypt(String data, byte[] key,String charset) {
        try {
            return encrypt(data.getBytes(charset), key);
        }catch (UnsupportedEncodingException e) {
            return null;
        }
    }
    /**
     * 加密
     * @param data 明文
     * @param key 加密KEY
     * @return 密文
     */
    public final byte[] encrypt(byte[] data, String key) {
        try {
            return encrypt(data, key.getBytes(CommonConstant.DEFAULT_CHARSET_NAME));
        }catch (UnsupportedEncodingException e) {
            return null;
        }
    }
    /**
     * 加密
     * @param data 明文
     * @param key 加密KEY
     * @return 密文
     */
    public final byte[] encrypt(String data, String key) {
        return encrypt(data,key,CommonConstant.DEFAULT_CHARSET_NAME);
    }
    /**
     * 加密
     * @param data   明文
     * @param key    加密key
     * @param charset 明文转字节数组用到的字符集
     * @return
     */
    public final byte[] encrypt(String data, String key,String charset) {
        try {
            return encrypt(data.getBytes(charset), key.getBytes(CommonConstant.DEFAULT_CHARSET_NAME));
        }catch (UnsupportedEncodingException e) {
            return null;
        }
    }
    /**
     * 加密
     * @param data 明文
     * @param key 加密KEY
     * @return 密文的BASE64串
     */
    public final String encryptToBase64(byte[] data, byte[] key) {
        byte[] bytes = encrypt(data, key);
        if (bytes == null) return null;
        return Base64.encodeBase64String(bytes);
    }
    /**
     * 加密
     * @param data 明文
     * @param key 加密KEY
     * @return 密文的BASE64串
     */
    public final String encryptToBase64(String data, byte[] key) {
        byte[] bytes = encrypt(data, key);
        if (bytes == null) return null;
        return Base64.encodeBase64String(bytes);
    }
    /**
     * 加密
     * @param data  明文
     * @param key 加密KEY
     * @param charset 明文转字节数组用到的字符集
     * @return
     */
    public final String encryptToBase64(String data, byte[] key,String charset) {
        byte[] bytes = encrypt(data, key,charset);
        if (bytes == null) return null;
        return Base64.encodeBase64String(bytes);
    }
    /**
     * 加密
     * @param data 明文
     * @param key 加密KEY
     * @return 密文的BASE64串
     */
    public final String encryptToBase64(byte[] data, String key) {
        byte[] bytes = encrypt(data, key);
        if (bytes == null) return null;
        return Base64.encodeBase64String(bytes);
    }
    /**
     * 加密
     * @param data 明文
     * @param key 加密KEY
     * @return 密文的BASE64串
     */
    public final String encryptToBase64(String data, String key) {
        byte[] bytes = encrypt(data, key);
        if (bytes == null) return null;
        return Base64.encodeBase64String(bytes);
    }
    /**
     * 加密
     * @param data 明文
     * @param key 加密KEY
     * @param charset 明文转字节数组用到的字符集
     * @return
     */
    public final String encryptToBase64(String data, String key,String charset) {
        byte[] bytes = encrypt(data, key,charset);
        if (bytes == null) return null;
        return Base64.encodeBase64String(bytes);
    }
    /**
     * 解密
     * @param data 密文
     * @param key  加密KEY，由于是对称加密，加密解密使用一样的KEY
     * @return 明文
     */
    public final byte[] decrypt(byte[] data, byte[] key) {
        if (data.length == 0) {
            return data;
        }
        return toByteArray(
                decrypt(toIntArray(data, false), toIntArray(fixKey(key), false)), true);
    }
    /**
     * 解密
     * @param data 密文
     * @param key  加密KEY，由于是对称加密，加密解密使用一样的KEY
     * @return 明文
     */
    public final byte[] decrypt(byte[] data, String key) {
        try {
            return decrypt(data, key.getBytes(CommonConstant.DEFAULT_CHARSET_NAME));
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }
    /**
     * 解密
     * @param data 密文的BASE64
     * @param key  加密KEY，由于是对称加密，加密解密使用一样的KEY
     * @return 明文
     */
    public final byte[] decryptBase64(String data, byte[] key) {
        return decrypt(Base64.decodeBase64(data), key);
    }
    /**
     * 解密
     * @param data 密文的BASE64
     * @param key  加密KEY，由于是对称加密，加密解密使用一样的KEY
     * @return 明文
     */
    public final byte[] decryptBase64(String data, String key) {
        return decrypt(Base64.decodeBase64(data), key);
    }
    /**
     * 解密
     * @param data 密文
     * @param key  加密KEY，由于是对称加密，加密解密使用一样的KEY
     * @return 明文转字符串，字符集是UTF8
     */
    public final String decryptToString(byte[] data, byte[] key) {
        return decryptToString(data,key,CommonConstant.DEFAULT_CHARSET_NAME);
    }
    /**
     * 解密
     * @param data 密文
     * @param key  加密KEY，由于是对称加密，加密解密使用一样的KEY
     * @param charset 解密后的结果转字符串所用的字符集
     * @return 明文转字符串，字符集是
     */
    public final String decryptToString(byte[] data, byte[] key,String charset) {
        try {
            byte[] bytes = decrypt(data, key);
            if (bytes == null) return null;
            return new String(bytes, charset);
        } catch (UnsupportedEncodingException ex) {
            return null;
        }
    }
    /**
     * 解密
     * @param data 密文
     * @param key 加密KEY
     * @return 明文，采用UTF8
     */
    public final String decryptToString(byte[] data, String key) {
        return decryptToString(data,key,CommonConstant.DEFAULT_CHARSET_NAME);
    }
    /**
     * 解密
     * @param data 密文
     * @param key 加密KEY
     * @param charset 明文转字符串采用的字符集
     * @return 明文
     */
    public final String decryptToString(byte[] data, String key,String charset) {
        try {
            byte[] bytes = decrypt(data, key);
            if (bytes == null) return null;
            return new String(bytes, charset);
        }catch (UnsupportedEncodingException ex) {
            return null;
        }
    }
    /**
     * 解密
     * @param data 密文
     * @param key 加密KEY
     * @return 明文，采用UTF8
     */
    public final String decryptBase64ToString(String data, byte[] key) {
        return decryptBase64ToString(data,key,CommonConstant.DEFAULT_CHARSET_NAME);
    }
    public final String decryptBase64ToString(String data, byte[] key,String charset) {
        try {
            byte[] bytes = decrypt(Base64.decodeBase64(data), key);
            if (bytes == null) return null;
            return new String(bytes, charset);
        }catch (UnsupportedEncodingException ex) {
            return null;
        }
    }
    /**
     * 解密
     * @param data 密文
     * @param key 加密KEY
     * @return 明文，采用UTF8
     */
    public final String decryptBase64ToString(String data, String key) {
        try {
            byte[] bytes = decrypt(Base64.decodeBase64(data), key);
            if (bytes == null) return null;
            return new String(bytes, CommonConstant.DEFAULT_CHARSET_NAME);
        }catch (UnsupportedEncodingException ex) {
            return null;
        }
    }
    /**
     * 解密
     * @param data 密文
     * @param key 加密KEY
     * @param charset 明文转字符串采用的字符集
     * @return 明文
     */
    public final String decryptBase64ToString(String data, String key,String charset) {
        try {
            byte[] bytes = decrypt(Base64.decodeBase64(data), key);
            if (bytes == null) return null;
            return new String(bytes, charset);
        }catch (UnsupportedEncodingException ex) {
            return null;
        }
    }

    private int[] encrypt(int[] v, int[] k) {
        int n = v.length - 1;

        if (n < 1) {
            return v;
        }
        int p, q = 6 + 52 / (n + 1);
        int z = v[n], y, sum = 0, e;

        while (q-- > 0) {
            sum = sum + delta;
            e = sum >>> 2 & 3;
            for (p = 0; p < n; p++) {
                y = v[p + 1];
                z = v[p] += MX(sum, y, z, p, e, k);
            }
            y = v[0];
            z = v[n] += MX(sum, y, z, p, e, k);
        }
        return v;
    }

    private int[] decrypt(int[] v, int[] k) {
        int n = v.length - 1;

        if (n < 1) {
            return v;
        }
        int p, q = 6 + 52 / (n + 1);
        int z, y = v[0], sum = q * delta, e;

        while (sum != 0) {
            e = sum >>> 2 & 3;
            for (p = n; p > 0; p--) {
                z = v[p - 1];
                y = v[p] -= MX(sum, y, z, p, e, k);
            }
            z = v[n];
            y = v[0] -= MX(sum, y, z, p, e, k);
            sum = sum - delta;
        }
        return v;
    }

    private byte[] fixKey(byte[] key) {
        if (key.length == 16) return key;
        byte[] fixedkey = new byte[16];
        if (key.length < 16) {
            System.arraycopy(key, 0, fixedkey, 0, key.length);
        }else {
            System.arraycopy(key, 0, fixedkey, 0, 16);
        }
        return fixedkey;
    }

    private int[] toIntArray(byte[] data, boolean includeLength) {
        int n = (((data.length & 3) == 0)
                ? (data.length >>> 2)
                : ((data.length >>> 2) + 1));
        int[] result;

        if (includeLength) {
            result = new int[n + 1];
            result[n] = data.length;
        }else {
            result = new int[n];
        }
        n = data.length;
        for (int i = 0; i < n; ++i) {
            result[i >>> 2] |= (0x000000ff & data[i]) << ((i & 3) << 3);
        }
        return result;
    }

    private byte[] toByteArray(int[] data, boolean includeLength) {
        int n = data.length << 2;

        if (includeLength) {
            int m = data[data.length - 1];
            n -= 4;
            if ((m < n - 3) || (m > n)) {
                return null;
            }
            n = m;
        }
        byte[] result = new byte[n];

        for (int i = 0; i < n; ++i) {
            result[i] = (byte) (data[i >>> 2] >>> ((i & 3) << 3));
        }
        return result;
    }
    public static void main(String[] args){
        String src="helloworld";
        for(int i=0;i<10000;i++){
            String key=RandomStringUtils.random(12,true,true);
            XXTea tea=XXTea.randomInstance();
            System.out.println(tea.decryptBase64ToString(tea.encryptToBase64(src,key),key));
        }
    }
}