package com.wifiin.util.bytes;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import com.wifiin.common.CommonConstant;

public class ThreadLocalByteArray{
    private static final ThreadLocal<byte[]> STREAMS=new ThreadLocal<>();
    public static byte[] bytes(){
        byte[] out=STREAMS.get();
        if(out==null){
            out=new byte[256];
            STREAMS.set(out);
        }
        return out;
    }
    public static String toText(){
        return toText(CommonConstant.DEFAULT_CHARSET_INSTANCE);
    }
    public static String toText(String charset){
        return toText(Charset.forName(charset));
    }
    public static String toText(Charset charset){
        return toText(bytes(),charset);
    }
    public static String toText(byte[] out){
        return toText(out,CommonConstant.DEFAULT_CHARSET_INSTANCE);
    }
    public static String toText(byte[] out,String charset) throws UnsupportedEncodingException{
        return toText(out,Charset.forName(charset));
    }
    public static String toText(byte[] out,Charset charset){
        return new String(out,charset);
    }
}
