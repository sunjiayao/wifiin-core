package com.wifiin.kv.command;

import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Maps;
import com.wifiin.kv.BytesPayLoadResult;
import com.wifiin.kv.Command;
import com.wifiin.kv.DataType;
import com.wifiin.kv.Result;
import com.wifiin.kv.store.Store;
import com.wifiin.kv.util.KVUtils;
import com.wifiin.util.Help;

public enum SystemCommand implements Command<Result>{
    DEL(1){
        @Override
        public Result execute(byte[] uuid,Store store, byte[] key,byte... params){
            if(Help.isNotEmpty(params)){
                key=KVUtils.addKeyPrefix(params[0],params[1],0,null,key);
            }else{
                key=KVUtils.addKeyPrefix((byte)DataType.SYSTEM.value(),(byte)TYPE.value(),0,null,key);
                params=store.get(key);
                if(Help.isNotEmpty(params)){
                    key=KVUtils.addKeyPrefix(params[0],params[1],0,null,key);
                }
            }
            store.del(key);
            return new BytesPayLoadResult(Result.SUCCESS,uuid,null);
        }
    },
    PING(2),
    SYNC(3),
    METADATA(4),
    SLOWLOG(5),
    ID(6){
        @Override
        public Result execute(byte[] uuid,Store store, byte[] key,byte... params){
            byte[] k=KVUtils.addKeyPrefix(DataType.SYSTEM.value(),this.value,0,null,key);
            byte[] id=store.get(k);
            if(Help.isEmpty(id)){
                id=UUID.randomUUID().toString().getBytes();
                store.put(k,id);
            }
            return new BytesPayLoadResult(Result.SUCCESS,uuid,id);
        }
    },
    TYPE(7){
        @Override
        public Result execute(byte[] uuid,Store store, byte[] key,byte... params){
            store.put(KVUtils.addKeyPrefix(DataType.SYSTEM.value(),this.value,0,null,key),params);
            return new BytesPayLoadResult(Result.SUCCESS,uuid,null);
        }
    },
    CMD(8){
        @Override
        public Result execute(byte[] uuid,Store store, byte[] key,byte... params){
            //millis uuid key 
            byte[] k=KVUtils.addKeyPrefix(DataType.SYSTEM.value(),this.value,System.currentTimeMillis(),uuid,key);
            store.put(k,params);
            return new BytesPayLoadResult(Result.SUCCESS,uuid,null);
        }
    },
    SEQUENCE(9);
    private static final Map<Integer,SystemCommand> cmds=Maps.newConcurrentMap();
    static{
        for(SystemCommand c:SystemCommand.values()){
            cmds.put(c.value,c);
        }
    }
    protected final int value;
    public int value(){
        return value;
    }
    private SystemCommand(int value){
        this.value=value;
    }
    public static SystemCommand valueOf(int value){
        return cmds.get(value);
    }
    
    @Override
    public Result execute(byte[] uuid,Store store, byte[] key,byte... params){
        return null;
    }
    
}
