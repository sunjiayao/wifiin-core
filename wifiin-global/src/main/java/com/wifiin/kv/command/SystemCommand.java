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
    DEL(1),
    PING(2),
    SYNC(3),
    METADATA(4),
    SLOWLOG(5),
    ID(6){
        @Override
        public Result execute(Store store, byte[] key,int offset,byte... params){
            byte[] k=KVUtils.addKeyPrefix(DataType.SYSTEM.value(),this.value,key);
            byte[] id=store.get(k);
            if(Help.isEmpty(id)){
                id=UUID.randomUUID().toString().getBytes();
                store.put(k,id);
            }
            return new BytesPayLoadResult(Result.SUCCESS,id);
        }
    },
    TYPE(7),
    CMD(8),
    SEQUENCE(9);
    private static final Map<Integer,SystemCommand> cmds=Maps.newConcurrentMap();
    static{
        for(SystemCommand c:SystemCommand.values()){
            cmds.put(c.value,c);
        }
    }
    protected final int value;
    
    private SystemCommand(int value){
        this.value=value;
    }
    public SystemCommand valueOf(int value){
        return cmds.get(value);
    }
    @Override
    public Result execute(Store store, byte[] key,int offset,byte... params){
        // TODO Auto-generated method stub
        return null;
    }
    
}
