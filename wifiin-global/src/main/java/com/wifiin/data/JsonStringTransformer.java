package com.wifiin.data;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wifiin.common.GlobalObject;
import com.wifiin.data.exception.DataTransformerException;

public class JsonStringTransformer<T> implements Transformer<T,Object,String>{

    @Override
    public String encode(T t,Object p){
        try {
            return GlobalObject.getJsonMapper().writeValueAsString(p);
        } catch (JsonProcessingException e) {
            throw new DataTransformerException(e);
        }
    }

    @Override
    public Object decode(T t,String r,Class<Object> cls){
        try {
            return GlobalObject.getJsonMapper().readValue(r, cls);
        } catch (IOException e) {
            throw new DataTransformerException(e);
        }
    }

}
