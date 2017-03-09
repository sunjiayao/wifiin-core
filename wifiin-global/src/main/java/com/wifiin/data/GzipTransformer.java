package com.wifiin.data;

import java.io.IOException;

import com.wifiin.data.exception.DataTransformerException;
import com.wifiin.util.compress.GZIP;

public class GzipTransformer<T> implements Transformer<T,byte[],byte[]>{

    @Override
    public byte[] encode(T t,byte[] p){
        try {
            return GZIP.gzip(p);
        } catch (IOException e) {
            throw new DataTransformerException(e);
        }
    }

    @Override
    public byte[] decode(T t,byte[] r, Class<byte[]> cls){
        try {
            return GZIP.ungzip(r);
        } catch (IOException e) {
            throw new DataTransformerException(e);
        }
    }

}
