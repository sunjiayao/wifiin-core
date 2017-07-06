package com.wifiin.kv.util;

import com.wifiin.kv.DataType;

public class KVUtils{
    public static byte[] addKeyPrefix(DataType dataType,int cmd,byte[] key){
        return addKeyPrefix(dataType.value(),cmd,key);
    }
    public static byte[] addKeyPrefix(int type,int cmd,byte[] key){
        return addKeyPrefix((byte)type,(byte)cmd,key);
    }
    public static byte[] addKeyPrefix(byte type,byte cmd,byte[] key){
        byte[] k=new byte[2+key.length];
        k[0]=type;
        k[1]=cmd;
        System.arraycopy(key,0,k,2,key.length);
        return k;
    }
}
