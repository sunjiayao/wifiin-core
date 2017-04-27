package com.wifiin.util.kryo;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.lang3.RandomStringUtils;
import org.nustaq.serialization.FSTConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.wifiin.common.CommonConstant;
import com.wifiin.util.Help;

/**
 * 对kryo的薄封装，确保一个线程一个kryo实例，没有线程安全问题也不会反复创建kryo对象
 * @author Running
 *
 */
public class ThreadLocalKryo {
    private Logger log=LoggerFactory.getLogger(ThreadLocalKryo.class);
    private static final ThreadLocalKryo instance=new ThreadLocalKryo();
    private static final byte[] EMPTY_BUFFER=CommonConstant.EMPTY_BYTE_ARRAY;
    public static ThreadLocalKryo kryo(){
        return instance;
    }
    
    private ThreadLocalKryo(){}
    
    private ThreadLocal<KryoHelper> kryo=new ThreadLocal<>();
    /**
     * kryo帮助类
     * @return
     */
    private KryoHelper getHelper(){
        KryoHelper helper=kryo.get();
        if(helper==null){
            helper=new KryoHelper();
            kryo.set(helper);
        }
        return helper;
    }
    /**
     * 写操作收尾，完成flush、清除缓冲区、重置等操作
     * @param helper
     * @return 把对象写入到字节数组的结果
     */
    private byte[] finishWrite(KryoHelper helper){
        helper.out.flush();
        byte[] buf=helper.out.toBytes();
        helper.out.clear();
        helper.kryo.reset();
        return buf;
    }
    /**
     * 把指定对象o写到一个字节数组，返回值就是写入结果
     * @param helper
     * @param o 要序列化的对象
     * @return
     */
    private byte[] write(KryoHelper helper,Object o){
        helper.kryo.writeObject(helper.out, o);
        return finishWrite(helper);
    }
    /**
     * 把指定对象带类信息写入一个字节数组
     * @param helper
     * @param o 要序列化的对象
     * @return 序列化的结果
     */
    private byte[] writeClassAndObject(KryoHelper helper,Object o){
        helper.kryo.writeClassAndObject(helper.out,o);
        return finishWrite(helper);
    }
    /**
     * 把对象o序列化为字节数组
     * @param o 要序列化的对象
     * @param fn 序列化的具体行为，目前有两种行为，带类信息序列化和不带类信息序列化
     * @return 序列化结果
     */
    private byte[] write(Object o,BiFunction<KryoHelper,Object,byte[]> fn){
        KryoHelper helper=getHelper();
        for(;;){
            try{
                return fn.apply(helper,o);
            }catch(KryoException e){
                log.warn("ThreadLocakKryo.write:",e);
                helper.out.setBuffer(new byte[helper.out.getBuffer().length*2]);
            }
        }
    }
    /**
     * 把对象o序列化为字节数组，序列化结果不带类信息
     * @param o
     * @return
     */
    public byte[] write(Object o){
        return write(o,this::write);
    }
    /**
     * 把对象o序列化为字节数组，序列化结果带类信息
     * @param o
     * @return
     */
    public byte[] writeClassAndObject(Object o){
        return write(o,this::writeClassAndObject);
    }
    /**
     * 把序列化的结果写到output
     * @param buf 序列化结果
     * @param output
     * @throws IOException
     */
    private void finishWrite(byte[] buf,ObjectOutput output) throws IOException{
        output.writeInt(buf.length);
        output.write(buf);
        output.flush();
    }
    /**
     * 把对象o序列化到ouptut，不带类信息
     * @param o
     * @param output
     * @throws IOException
     */
    public void write(Object o,ObjectOutput output) throws IOException{
        byte[] buf=write(o);
        finishWrite(buf,output);
    }
    /**
     * 把对象o序列化到output，带类信息
     * @param o
     * @param output
     * @throws IOException
     */
    public void writeClassAndObject(Object o,ObjectOutput output) throws IOException{
        byte[] buf=writeClassAndObject(o);
        finishWrite(buf,output);
    }
    /**
     * 将指定对象o以kryo格式写出到out
     * @param o
     * @param out
     */
    public void write(Object o,OutputStream out){
        writeInternal(out,(h)->{
            h.kryo.writeObject(h.out,o);
            return null;
        });
    }
    /**
     * 将指定对象o以kryo格式写出到out
     * @param o
     * @param out
     */
    public void writeClassAndObject(Object o,OutputStream out){
        writeInternal(out,(h)->{
            h.kryo.writeClassAndObject(h.out,o);
            return null;
        });
    }
    /**
     * 将指定对象o以kryo格式写出到out
     * @param out
     * @param fn
     */
    private void writeInternal(OutputStream out,Function<KryoHelper,Void> fn){
        KryoHelper helper=getHelper();
        try{
            helper.out.setOutputStream(out);
            fn.apply(helper);
        }finally{
            helper.out.flush();
            helper.out.setOutputStream(null);
        }
    }
    /**
     * 把字节数组反序列化为指定Class的对象，具体行为由fn控制，目前有两种反序列化行为，分别是字节数组包含类信息或不包含类信息
     * @param buf
     * @param cls
     * @param fn
     * @return
     */
    private <T> T readInternal(byte[] buf,Class<T> cls,Function<KryoHelper,T> fn){
        KryoHelper helper=getHelper();
        try{
            helper.in.setBuffer(buf);
            return (T)fn.apply(helper);
        }finally{
            helper.in.setBuffer(EMPTY_BUFFER);
        }
    }
    /**
     * 把字节数组反序列化为对象，buf包含类信息
     * @param buf
     * @return 反序列化的对象
     */
    @SuppressWarnings("unchecked")
    public <T> T read(byte[] buf){
        return readInternal(buf,null,(h)->{
            return (T)h.kryo.readClassAndObject(h.in);
        });
    }
    /**
     * 把字节数组反序列化为对象，buf不包含类信息
     * @param buf
     * @param cls 反序列化的类型
     * @return
     */
    public <T> T read(byte[] buf,Class<T> cls){
        return readInternal(buf,null,(h)->{
            return (T)h.kryo.readObject(h.in,cls);
        });
    }
    /**
     * 反序列化前的工作
     * @param input 包含对象数据的源
     * @return 对象数组
     * @throws IOException
     */
    private byte[] aheadRead(ObjectInput input) throws IOException{
        int bufLen=input.readInt();
        byte[] buf=new byte[bufLen];
        input.read(buf);
        return buf;
    }
    /**
     * 把input反序列化成对象，源包含类信息
     * @param input 反序列化的源
     * @return 反序列化的结果
     * @throws IOException
     */
    public <T> T read(ObjectInput input) throws IOException{
        return read(aheadRead(input));
    }
    /**
     * 把input反序列化成指定Class的对象，源不包含类信息
     * @param input
     * @param cls
     * @return 反序列化的结果
     * @throws IOException
     */
    public <T> T read(ObjectInput input,Class<T> cls) throws IOException{
        return read(aheadRead(input),cls);
    }
    /**
     * 把从in读到的内容反序列化成指定Class的对象，源不包含类信息
     * @param in
     * @param cls
     * @return
     */
    public <T> T read(InputStream in,Class<T> cls){
        return (T)readInternal(in,(h)->{
            return h.kryo.readObject(h.in,cls);
        });
    }
    /**
     * 把从in读到的内容反序列化成指定Class的对象，源包含类信息
     * @param in
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T read(InputStream in){
        return (T)readInternal(in,(h)->{
            return h.kryo.readClassAndObject(h.in);
        });
    }
    /**
     * 把从in读到的内容反序列化成指定Class对象，源是否包含类信息由调用它的方法决定
     * @param in
     * @param fn
     * @return
     */
    private <T> T readInternal(InputStream in,Function<KryoHelper,T> fn){
        KryoHelper helper=getHelper();
        try{
            helper.in.setInputStream(in);
            return fn.apply(helper);
        }finally{
            helper.in.setBuffer(EMPTY_BUFFER);
            helper.in.setInputStream(null);
        }
    }
    
    private class KryoHelper{
        public Output out=new Output(new byte[256],Integer.MAX_VALUE);
        public Input in=new Input();
        public Kryo kryo=new Kryo();
    }
}
