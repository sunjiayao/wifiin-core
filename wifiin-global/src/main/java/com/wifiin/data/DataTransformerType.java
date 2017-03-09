package com.wifiin.data;

import java.lang.reflect.InvocationTargetException;

import com.wifiin.data.exception.DataTransformerException;

public enum DataTransformerType {
    BYTES_2_STRING(0,Bytes2StringType.class),
    ENCRYPTION(1,EncryptionType.class),
    COMPRESS(2,CompressType.class),
    SERIALIZATION(3,SerializationType.class);
    private int type;
    private Class<? extends Enum<?>> transformerType;
    private DataTransformerType(int type,Class<? extends Enum<?>> transformerType){
        this.type=type;
        this.transformerType=transformerType;
    }
    public static String evalDataTransformerName(int type,int transformerType){
        DataTransformerType[] dtts=DataTransformerType.values();
        for(int i=0,l=dtts.length;i<l;i++){
            DataTransformerType dtt=dtts[i];
            if(dtt.type==type){
                Class tt=dtt.transformerType;
                try {
                    return (String) tt.getMethod("eval", int.class).invoke(null, transformerType);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                        | NoSuchMethodException | SecurityException e) {
                    throw new DataTransformerException(e);
                }
            }
        }
        throw new DataTransformerException(type);
    }
}
