package com.wifiin.util.security;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.apache.commons.codec.binary.Base64;

/**
 * RC4加密算法
 * @author Running
 *
 */
public class RC4{
    private static final Map<String,byte[]> KEYS=new ConcurrentHashMap<>();
    private static final Function<String,byte[]> COMPUTOR_IF_ABSENT=(k)->{
        return initKey(k);
    };
    /**
     * 获取加密KEY的字节数组
     * @param key 加密KEY
     * @return
     */
    private static byte[] getKeyBytes(String key){
        return KEYS.computeIfAbsent(key,COMPUTOR_IF_ABSENT);
    }
    /**
     * 加密KEY转字节数组
     * @param aKey 加密KEY
     * @return
     */
    private static byte[] initKey(String aKey) {
        byte[] b_key = aKey.getBytes();
        byte state[] = new byte[256];

        for (int i = 0; i < 256; i++) {
            state[i] = (byte) i;
        }
        int index1 = 0;
        int index2 = 0;
        if (b_key == null || b_key.length == 0) {
            return null;
        }
        for (int i = 0; i < 256; i++) {
            index2 = ((b_key[index1] & 0xff) + (state[i] & 0xff) + index2) & 0xff;
            byte tmp = state[i];
            state[i] = state[index2];
            state[index2] = tmp;
            index1 = (index1 + 1) % b_key.length;
        }
        return state;
    }
    /**
     * RC4
     * @param input 原文
     * @param key 加密KEY
     * @return 如果input是明文，就返回密文，如果input是密文就返回明文
     */
    public static byte[] rc4 (byte [] input, String key) {
        return rc4(input,getKeyBytes(key));
    }
    /**
     * 加密
     * @param input 明文
     * @param key 加密KEY
     * @param charset 明文转字节数组采用的字符集
     * @return 密文BASE64
     * @throws UnsupportedEncodingException
     */
    public static String encryptToBase64(String input,String key,String charset) throws UnsupportedEncodingException{
        return Base64.encodeBase64String(rc4(input.getBytes(charset),key));
    }
    /**
     * 解密
     * @param input 密文
     * @param key 加密KEY
     * @param charset 明文转字符串采用的字符集
     * @return 明文
     * @throws UnsupportedEncodingException
     */
    public static String decryptFromBase64(String input,String key,String charset) throws UnsupportedEncodingException{
        return new String(rc4(Base64.decodeBase64(input),key),charset);
    }
    /**
     * RC4算法核心
     * @param input 输入，如果是明文就完成加密，如果是密文就完成解密
     * @param key 加密KEY
     */
    public static byte[] rc4(byte[] input,byte[] key){
        int x = 0;
        int y = 0;
        int xorIndex;
        byte[] result = new byte[input.length];
        for (int i = 0; i < input.length; i++) {
            x = (x + 1) & 0xff;
            y = ((key[x] & 0xff) + y) & 0xff;
            byte tmp = key[x];
            key[x] = key[y];
            key[y] = tmp;
            xorIndex = ((key[x] & 0xff) + (key[y] & 0xff)) & 0xff;
            result[i] = (byte) (input[i] ^ key[xorIndex]);
        }
        return result;
    }
    public static void main(String[] args) throws UnsupportedEncodingException{
        System.out.println(encryptToBase64("helloworld","testKEY","utf8"));
        System.out.println(decryptFromBase64("NQJTC+UiAekOSA==","testKEY","utf8"));
    }
}
