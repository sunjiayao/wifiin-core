package com.wifiin.kv.store.impl;

import java.util.function.BiFunction;

import org.rocksdb.FlushOptions;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

import com.wifiin.kv.exception.DeletionStoreException;
import com.wifiin.kv.exception.FlushStoreException;
import com.wifiin.kv.exception.GetStoreException;
import com.wifiin.kv.exception.InitStoreException;
import com.wifiin.kv.exception.IterateStoreException;
import com.wifiin.kv.exception.PutStoreException;
import com.wifiin.kv.store.Store;
import com.wifiin.kv.store.StoreIterator;
import com.wifiin.util.ShutdownHookUtil;

public class RocksDBStore implements Store{
    private Options options=new Options();
    private RocksDB db;
    public RocksDBStore(String path){
        options.setAllowMmapReads(true)
               .setAllowMmapWrites(true)
               .setCreateIfMissing(true)
               .setCreateMissingColumnFamilies(true);
        try{
            db=RocksDB.open(options,path);
            ShutdownHookUtil.addHook(()->{
                FlushOptions fo=new FlushOptions();
                fo.setWaitForFlush(true);
                try{
                    db.flush(fo);
                }catch(RocksDBException e){
                    throw new FlushStoreException(e);
                }finally{
                    db.close();
                    fo.close();
                    options.close();
                }
            });
        }catch(RocksDBException e){
            throw new InitStoreException(e);
        }
    }
    public void put(byte[] key,byte[] value){
        try{
            db.put(key,value);
        }catch(RocksDBException e){
            throw new PutStoreException(e);
        }
    }
    public byte[] put(byte[] key,byte[] value,int start,int end){
        try{
            byte[] stored=new byte[end-start];
            System.arraycopy(value,start,stored,0,stored.length);
            db.put(key,stored);
            return stored;
        }catch(RocksDBException e){
            throw new PutStoreException(e);
        }
    }
    public byte[] get(byte[] key){
        try{
            return db.get(key);
        }catch(RocksDBException e){
            throw new GetStoreException(e);
        }
    }
    public void del(byte[] key){
        try{
            db.delete(key);
        }catch(RocksDBException e){
            throw new DeletionStoreException(e);
        }
    }
    public void delAllPrefix(byte[] prefix){
        del(prefix,prefix);
    }
    public void del(byte[] min,byte[] max){
        iterate(min,max,(k,v)->{
            del(k);
            return true;
        });
    }
    /**
     * 遍历每一个key前缀是prefix的键值对。忽略从fn抛出的异常
     * @param prefix
     * @param fn
     */
    public void iterate(byte[] prefix,BiFunction<byte[],byte[],Boolean> fn){
        iterate(prefix,fn,true);
    }
    /**
     * 遍历每一个key前缀是prefix的键值对。
     * @param prefix
     * @param fn
     * @param continueOnThrown 如果从fn抛出异常是否继续
     */
    public void iterate(byte[] prefix,BiFunction<byte[],byte[],Boolean> fn,boolean continueOnThrown){
        iterate(prefix,prefix,fn,continueOnThrown);
    }
    /**
     * 遍历每一个key前缀是prefix的键值对。忽略从fn抛出的异常
     * @param min
     * @param max
     * @param fn
     */
    public void iterate(byte[] min,byte[] max,BiFunction<byte[],byte[],Boolean> fn){
        iterate(min,max,fn,true);
    }
    /**
     * 遍历每一个key前缀是prefix的键值对。前闭后闭区间
     * @param min 包含
     * @param max 包含
     * @param fn 针对遍历的每一个键值对执行的逻辑
     * @param continueOnThrown 如果从fn抛出异常是否继续
     */
    public void iterate(byte[] min,byte[] max,BiFunction<byte[],byte[],Boolean> fn,boolean continueOnThrown){
        RocksIterator iterator=db.newIterator();
        boolean toContinue=true;
        try{
            for(iterator.seek(min);toContinue && iterator.isValid();iterator.next()){
                try{
                    byte[] key=iterator.key();
                    int compare=compareBytes(key,max);
                    if(compare<=0){
                        toContinue=fn.apply(key,iterator.value());
                    }else{
                        return;
                    }
                }catch(Exception e){
                    if(!continueOnThrown){
                        throw new IterateStoreException(e);
                    }
                }
            }
        }finally{
            iterator.close();
        }
    }
    /**
     * 遍历每一个key前缀是prefix的键值对。前闭后闭区间
     * @param min 
     * @param max 
     * @param fn
     * @param continueOnThrown 如果从fn抛出异常是否继续
     */
    public void reserveIterate(byte[] min,byte[] max,BiFunction<byte[],byte[],Boolean> fn,boolean continueOnThrown){
        RocksIterator iterator=db.newIterator();
        boolean toContinue=true;
        try{
            for(iterator.seek(max);toContinue && iterator.isValid();iterator.prev()){
                try{
                    byte[] key=iterator.key();
                    int compare=compareBytes(key,min);
                    if(compare>=0){
                        toContinue=fn.apply(key,iterator.value());
                    }else{
                        return;
                    }
                }catch(Exception e){
                    if(!continueOnThrown){
                        throw new IterateStoreException(e);
                    }
                }
            }
        }finally{
            iterator.close();
        }
    }
    private int compareBytes(byte[] first,byte[] second){
        int result=0;
        for(int i=0,l1=first.length,l2=second.length;result==0 && i<l1 && i<l2; i++){
            result=first[i]-second[i];
        }
        return result;
    }
    @Override
    public StoreIterator iterator(byte[] target,boolean start){
        RocksIterator iterator=db.newIterator();
        iterator.seek(target);
        return new StoreIterator(){
            @Override
            public void next(){
                iterator.next();
            }
            @Override
            public void prev(){
                iterator.prev();
            }
            @Override
            public byte[] key(){
                return iterator.key();
            }
            @Override
            public byte[] value(){
                return iterator.value();
            }
            @Override
            public boolean valid(){
                return iterator.isValid();
            }
            @Override
            public void close(){
                iterator.close();
            }
        };
    }
}
