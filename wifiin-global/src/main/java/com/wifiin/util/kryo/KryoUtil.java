package com.wifiin.util.kryo;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoPool;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

public class KryoUtil {
    private static final KryoUtil kryo=new KryoUtil();
    private KryoUtil(){}
    public static KryoUtil getInstance(){
        return kryo;
    }
    private KryoPool kryos=new KryoPool.Builder(()->{
        return new Kryo();
    }).build();
    
    public ByteBuffer write(Object src,ByteBuffer buffer){
        Kryo kryo=null;
        try{
            kryo=kryos.borrow();
            kryo.writeObject(new ByteBufferOutput(buffer), src);
            return buffer;
        }finally{
            if(kryo!=null){
                kryos.release(kryo);
            }
        }
    }
    public <T> T read(ByteBuffer buffer,Class<T> cls){
        Kryo kryo=null;
        try{
            kryo=kryos.borrow();
            return kryo.readObject(new ByteBufferInput(buffer), cls);
        }finally{
            if(kryo!=null){
                kryos.release(kryo);
            }
        }
    }
    public ByteBuf writeByteBuf(Object src){
        return write(src,PooledByteBufAllocator.DEFAULT.buffer());
    }
    public ByteBuf writeByteBuf(Object src, int initBufSize){
        return write(src,PooledByteBufAllocator.DEFAULT.buffer(initBufSize,Integer.MAX_VALUE));
    }
    public ByteBuf write(Object src, ByteBuf buf){
        return write(src,new NettyByteBufOutput(buf));
    }
    public ByteBuf write(Object src,NettyByteBufOutput out){
        Kryo kryo=null;
        try{
            kryo=kryos.borrow();
            kryo.writeObject(out,src);
            return out.getByteBuf();
        }finally{
            if(kryo!=null){
                kryos.release(kryo);
            }
        }
    }
    public <T> T read(ByteBuf buf,Class<T> cls){
        return read(buf.nioBuffer(),cls);
    }
    
    public void write(Object src, Output out){
        Kryo kryo=null;
        try{
            kryo=kryos.borrow();
            kryo.writeObject(out,src);
            out.flush();
        }finally{
            if(kryo!=null){
                kryos.release(kryo);
            }
        }
    }
    public byte[] write(Object src,int initBufSize){
        Output out=new Output(initBufSize,Integer.MAX_VALUE);
        write(src,out);
        return out.toBytes();
    }
    public byte[] write(Object src){
        return write(src,256);
    }
    public void write(Object src,OutputStream out){
        write(src,new Output(out));
    }
    public byte[] write(Object src, byte[] buf){
        Output output=new Output(buf,Integer.MAX_VALUE);
        write(src,output);
        return output.toBytes();
    }
    public <T> T read(Input in,Class<T> cls){
        Kryo kryo=null;
        try{
            kryo=kryos.borrow();
            return kryo.readObject(in, cls);
        }finally{
            if(kryo!=null){
                kryos.release(kryo);
            }
        }
    }
    public <T> T read(InputStream in,Class<T> cls){
        return read(new Input(in),cls);
    }
    public <T> T read(byte[] buf,Class<T> cls){
        return read(new Input(buf),cls);
    }
}