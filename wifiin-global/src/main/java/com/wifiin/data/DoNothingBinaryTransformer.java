package com.wifiin.data;

public class DoNothingBinaryTransformer<T>  implements Transformer<T,byte[],byte[]>{

    @Override
    public byte[] encode(T t, byte[] p){
        return p;
    }

    @Override
    public byte[] decode(T t, byte[] r, Class<byte[]> cls){
        return r;
    }

}
