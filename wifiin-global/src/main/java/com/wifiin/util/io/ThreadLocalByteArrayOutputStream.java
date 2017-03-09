package com.wifiin.util.io;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import com.wifiin.common.CommonConstant;

public class ThreadLocalByteArrayOutputStream{
    private static final ThreadLocal<ByteArrayOutputStream> STREAMS=new ThreadLocal<>();
    public static ByteArrayOutputStream stream(){
        return stream(true);
    }
    public static ByteArrayOutputStream steamWithoutReset(){
        return stream(false);
    }
    private static ByteArrayOutputStream stream(boolean reset){
        ByteArrayOutputStream out=STREAMS.get();
        if(out==null){
            out=new ByteArrayOutputStream();
            STREAMS.set(out);
        }else if(reset){
            out.reset();
        }
        return out;
    }
    public static void reset(){
        reset(stream());
    }
    public static void reset(ByteArrayOutputStream out){
        out.reset();
    }
    public static byte[] toByteArray(){
        return stream().toByteArray();
    }
    public static String toText(){
        return toText(CommonConstant.DEFAULT_CHARSET_INSTANCE);
    }
    public static String toText(String charset){
        return toText(Charset.forName(charset));
    }
    public static String toText(Charset charset){
        return toText(stream(),charset);
    }
    public static String toText(ByteArrayOutputStream out){
        return toText(out,CommonConstant.DEFAULT_CHARSET_INSTANCE);
    }
    public static String toText(ByteArrayOutputStream out,String charset) throws UnsupportedEncodingException{
        return toText(out,Charset.forName(charset));
    }
    public static String toText(ByteArrayOutputStream out,Charset charset){
        return new String(out.toByteArray(),charset);
    }
}
