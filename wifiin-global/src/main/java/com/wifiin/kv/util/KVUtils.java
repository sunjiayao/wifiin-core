package com.wifiin.kv.util;

import com.wifiin.kv.DataType;
import com.wifiin.util.Help;
import com.wifiin.util.message.IntMessageCodec;
import com.wifiin.util.message.Output;

public class KVUtils{
    public static byte[] addKeyPrefix(DataType dataType,int cmd,long millis,byte[] uuid,byte[] key){
        return addKeyPrefix(dataType.value(),cmd,millis,uuid,key);
    }
    public static byte[] addKeyPrefix(int type,int cmd,long millis,byte[] uuid,byte[] key){
        return addKeyPrefix((byte)type,(byte)cmd,millis,uuid,key);
    }
    public static byte[] addKeyPrefix(byte type,byte cmd,long millis,byte[] uuid,byte[] key){
        byte[] k=new byte[2+(millis>0?9:0)+(uuid==null?0:uuid.length)+key.length];
        k[0]=type;
        k[1]=cmd;
        int offset=2;
        if(millis>0){
            int len=IntMessageCodec.encode(millis,new Output(){
                private int offset=3;
                private int mark=3;
                @Override
                public int writerIndex(){
                    return offset;
                }

                @Override
                public void writerIndex(int index){
                    this.offset=index;
                }

                @Override
                public void setByte(int index,int b){
                    k[index]=(byte)b;
                    this.offset=index+1;
                }

                @Override
                public void markWriterIndex(){
                    mark=offset;
                }

                @Override
                public void writeByte(int value){
                    k[offset++]=(byte)value;
                }

                @Override
                public void writeBytes(byte[] buf){
                    for(int i=0,l=buf.length;i<l;i++){
                        writeByte(buf[i]);
                    }
                }

                @Override
                public void resetWriterIndex(){
                    offset=mark;
                }});
            k[offset]=(byte)len;
            offset+=len+1;
            for(int i=3,j=i+len-1;i<j;i++,j--){
                if(k[i]!=k[j]){
                    k[i]^=k[j];
                    k[j]^=k[i];
                    k[i]^=k[j];
                }
            }
        }
        if(Help.isNotEmpty(uuid)){
            System.arraycopy(uuid,0,uuid,offset,uuid.length);
        }
        offset+=uuid.length;
        System.arraycopy(key,0,k,offset,key.length);
        offset+=key.length;
        byte[] result=k;
        if(offset<k.length){
            result=new byte[offset];
            System.arraycopy(k,0,result,0,offset);
        }
        return result;
    }
}
