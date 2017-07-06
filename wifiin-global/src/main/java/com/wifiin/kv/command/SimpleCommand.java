package com.wifiin.kv.command;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import com.google.common.collect.Maps;
import com.wifiin.cache.HeapCache;
import com.wifiin.kv.BytesPayLoadResult;
import com.wifiin.kv.Command;
import com.wifiin.kv.DataType;
import com.wifiin.kv.Result;
import com.wifiin.kv.buf.ThreadLocalByteBufIntput;
import com.wifiin.kv.buf.ThreadLocalByteBufOutput;
import com.wifiin.kv.buf.ThreadLocalByteBufOutput.ByteBufOutput;
import com.wifiin.kv.constant.KVConstant;
import com.wifiin.kv.store.Store;
import com.wifiin.kv.util.KVUtils;
import com.wifiin.util.bytes.ThreadLocalByteArray;
import com.wifiin.util.message.Input;
import com.wifiin.util.message.IntMessageCodec;
import com.wifiin.util.message.Output;

/**
1 INCR 2 自增1
2 INCRBY 2 自增指定数
3 DECR 2 自减1
4 DECRBY 2 自减指定数
5 SET 2 设为指定值
6 SETEX 2 设为指定值且指定秒数后过期
7 SETEXAT 2 设为指定值且指定时间戳后过期，单位是秒
8 SETNX 2 如果不存在则设为指定值
9 SETENX 2 如果不存在则设为指定值且指定秒数后过期
10 SETENXAT 2 如果不存在则设为指定值且指定时间戳后过期，单位是秒
11 SETPX 2 如果存在则设为指定值
12 SETEPX 2 如果存在则设为指定值且指定秒数后过期
13 SETEPXAT 2 如果存在则设为指定值且指定时间戳后过期，单位是秒
14 GET 2 得到值

 * @author Running
 *
 */
public enum SimpleCommand implements Command<Result>{
    GET(1) {
        @Override
        public Result execute(Store store, byte[] key,int offset,byte... params){
            return HeapCache.<byte[],Result>getDefaultInstance(KVConstant.HEAP_NAME).get(key,()->{
                byte[] payload=store.get(prependKeyPrefix(key));
                if(payload==null){
                    return Result.UNEXISTS;
                }
                return new BytesPayLoadResult(Result.SUCCESS,payload);
            });
        }
    },INCR(2) {
        @Override
        public Result execute(Store store, byte[] key,int offset,byte... params){
            return INCRBY.execute(store,key,offset,one);
        }
    },INCRBY(3) {
        @Override
        public Result execute(Store store, byte[] key,int offset,byte... params){
            return incrBy(store,key,decodeDelta(params));
        }
    },DECR(4) {
        @Override
        public Result execute(Store store, byte[] key,int offset,byte... params){
            return DECRBY.execute(store,key,offset,one);
        }
    },DECRBY(5) {
        @Override
        public Result execute(Store store, byte[] key,int offset,byte... params){
            return incrBy(store,key,-decodeDelta(params));
        }
    },SET(6) {
        @Override
        public Result execute(Store store, byte[] key,int offset,byte... params){
            HeapCache.<byte[],Result>getDefaultInstance(KVConstant.HEAP_NAME).remove(key);
            store.put(prependKeyPrefix(key),params);
            return Result.SUCCESS;
        }
    },SETEX(7) {
        @Override
        public Result execute(Store store, byte[] key,int offset,byte... params){
            return null;
        }
    },SETEXAT(8) {
        @Override
        public Result execute(Store store, byte[] key,int offset,byte... params){
            // TODO Auto-generated method stub
            return null;
        }
    },SETNX(9) {
        @Override
        public Result execute(Store store, byte[] key,int offset,byte... params){
            // TODO Auto-generated method stub
            return null;
        }
    },SETENX(10) {
        @Override
        public Result execute(Store store, byte[] key,int offset,byte... params){
            // TODO Auto-generated method stub
            return null;
        }
    },SETENXAT(11) {
        @Override
        public Result execute(Store store, byte[] key,int offset,byte... params){
            // TODO Auto-generated method stub
            return null;
        }
    },SETPX(12) {
        @Override
        public Result execute(Store store, byte[] key,int offset,byte... params){
            // TODO Auto-generated method stub
            return null;
        }
    },SETEPX(13) {
        @Override
        public Result execute(Store store, byte[] key,int offset,byte... params){
            // TODO Auto-generated method stub
            return null;
        }
    },SETEPXAT(14) {
        @Override
        public Result execute(Store store, byte[] key,int offset,byte... params){
            // TODO Auto-generated method stub
            return null;
        }
    };
    private static final Map<Integer,SimpleCommand> cmds=Maps.newHashMap();
    static{
        for(SimpleCommand cmd:SimpleCommand.values()){
            cmds.put(cmd.value,cmd);
        }
    }
    public static SimpleCommand valueOf(int value){
        return cmds.get(value);
    }
    protected final int value;
    private SimpleCommand(int value){
        this.value=value;
    }
    private final static byte[] one=toBytes(1);
    private static byte[] toBytes(int value){
        ByteBufOutput output=ThreadLocalByteBufOutput.output();
        IntMessageCodec.encode(value,output);
        return output.bytes();
    }
    private static long decodeDelta(byte[] delta){
        return IntMessageCodec.decode(ThreadLocalByteBufIntput.input(delta));
    }
    private static byte[] prependKeyPrefix(byte[] key){
        return KVUtils.addKeyPrefix(DataType.SIMPLE,0,key);
    }
    private static Result incrBy(Store store,byte[] key,long delta){
        byte[] k=prependKeyPrefix(key);
        AtomicLong atomic=HeapCache.<byte[],AtomicLong>getDefaultInstance(KVConstant.HEAP_NAME).get(k,()->{
            byte[] value=store.get(k);
            if(value==null){
                return new AtomicLong(0);
            }
            return new AtomicLong(IntMessageCodec.decode(new Input(){
                private int idx=0;
                @Override
                public short readUnsignedByte(){
                    return (short)(value[idx++] & 0xff);
                }
            }));
        });
        synchronized(atomic){
            long value=atomic.addAndGet(delta);
            byte[] result=ThreadLocalByteArray.bytes();
            Output output=ThreadLocalByteBufOutput.output();
            IntMessageCodec.encode(value,output);
            int idx=output.writerIndex();
            result[0]=(byte)(idx-1);
            store.put(k,result,1,idx);
            return new BytesPayLoadResult(Result.SUCCESS,result,0,idx);
        }
    }
    public abstract Result execute(Store store, byte[] key,int offset,byte... params);
}
