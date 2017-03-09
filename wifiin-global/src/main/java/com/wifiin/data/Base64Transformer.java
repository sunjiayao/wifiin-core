package com.wifiin.data;

import org.apache.commons.codec.binary.Base64;

public class Base64Transformer<T> implements Transformer<T,byte[],String>{

    @Override
    public String encode(T t,byte[] p){
        return Base64.encodeBase64String(p);
    }

    @Override
    public byte[] decode(T t,String r, Class<byte[]> cls){
        return Base64.decodeBase64(r);
    }

}
