package com.wifiin.kv;

import java.util.Map;

import com.google.common.collect.Maps;
import com.wifiin.kv.store.Store;
import com.wifiin.kv.store.impl.RocksDBStore;

public class KV<R extends Result>{
    private static final Map<String,KV<?>> kvs=Maps.newConcurrentMap();
    @SuppressWarnings("unchecked")
    public static <R extends Result> KV<R> getInstance(String path){
        return (KV<R>)kvs.computeIfAbsent(path,(k)->{
            return new KV<R>(path);
        });
    }
    private Store store;
    private KV(String path){
        store=new RocksDBStore(path);
    }
    public R execute(Command<R> command,byte[] key,int offset,byte... params){
        return command.execute(store,key,offset,params);
    }
    public R execute(DataType dt,byte command,byte[] key,int offset,byte... params){
        return execute(dt.command(command),key,offset,params);
    }
    public R execute(byte dataType,byte command,byte[] key,int offset,byte... params){
        return execute(DataType.valueOf(dataType),command,key,offset,params);
    }
}
