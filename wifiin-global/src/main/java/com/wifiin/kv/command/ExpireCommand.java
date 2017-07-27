package com.wifiin.kv.command;

import java.util.Map;

import com.google.common.collect.Maps;
import com.wifiin.kv.BytesPayLoadResult;
import com.wifiin.kv.Command;
import com.wifiin.kv.DataType;
import com.wifiin.kv.Result;
import com.wifiin.kv.store.Store;
import com.wifiin.kv.util.KVUtils;
import com.wifiin.util.message.ArrayByteBufOutput;
import com.wifiin.util.message.ByteArrayInput;
import com.wifiin.util.message.IntMessageCodec;

public enum ExpireCommand implements Command<Result>{
    EXPIRE(1){
        @Override
        public Result execute(byte[] uuid,Store store, byte[] key,byte... params){
            long expireAt=decode(params)+System.currentTimeMillis()/1000;
            return execute(uuid,store,key,expireAt);
        }
    },
    EXPIREAT(2){
        @Override
        public Result execute(byte[] uuid,Store store, byte[] key,byte... params){
            return execute(uuid,store,key,decode(params));
        }
    };
    private static final Map<Integer,ExpireCommand> cmds=Maps.newHashMap();
    static{
        for(ExpireCommand cmd:ExpireCommand.values()){
            cmds.put(cmd.value,cmd);
        }
    }
    public static ExpireCommand valueOf(int value){
        return cmds.get(value);
    }
    private int value;
    private ExpireCommand(int value){
        this.value=value;
    }
    @Override
    public int value(){
        return value;
    }
    protected Result execute(byte[] uuid,Store store, byte[] key,long expireAt){
        byte[] k=KVUtils.addKeyPrefix(DataType.EXPIRE.value(),0,0,null,key);
        ArrayByteBufOutput output=new ArrayByteBufOutput();
        IntMessageCodec.encode(expireAt,output);
        store.put(k,output.byteArray());
        return new BytesPayLoadResult(Result.SUCCESS,uuid,null);
    }
    protected long decode(byte[] params){
        return IntMessageCodec.decode(new ByteArrayInput(params));
    }
}
