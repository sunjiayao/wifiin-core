package com.wifiin.data;

import org.apache.commons.codec.binary.Base64;

public class Base64BytesTransformer<T> implements Transformer<T,byte[],byte[]>{

    @Override
    public byte[] encode(T t,byte[] p){
        return Base64.encodeBase64(p);
    }

    @Override
    public byte[] decode(T t,byte[] r, Class<byte[]> cls){
        return Base64.decodeBase64(r);
    }

}
