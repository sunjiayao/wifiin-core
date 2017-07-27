package com.wifiin.kv.util;

import java.util.UUID;

public class UUIDUtil{
    private static final byte[] EMPTY_BYTES=new byte[0];
    public static UUID parse(byte[] bytes){
        if(bytes==null){
            bytes=EMPTY_BYTES;
        }
        if(bytes.length!=16){
            return UUID.nameUUIDFromBytes(bytes);
        }
        long most=bytes2long(bytes,0);
        long least=bytes2long(bytes,8);
        return new UUID(most,least);
    }
    
    private static long bytes2long(byte[] buf,int idx){
        long value=0;
        for(int i=0;i<8 && idx<buf.length;i++){
            value=(value<<8) | (buf[idx++] & 0xff);
        }
        return value;
    }
    
    public static byte[] uuid2bytes(UUID uuid){
        byte[] buf=new byte[16];
        int i=long2bytes(uuid.getLeastSignificantBits(),buf,15);
        long2bytes(uuid.getMostSignificantBits(),buf,i);
        return buf;
    }
    private static int long2bytes(long value,byte[] buf,int idx){
        for(int c=0;c<8;c++){
            buf[idx--]=(byte)value;
            value>>>=8;
        }
        return idx;
    }
}
