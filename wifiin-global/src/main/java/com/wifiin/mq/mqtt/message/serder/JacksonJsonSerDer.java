package com.wifiin.mq.mqtt.message.serder;

import java.io.IOException;
import java.io.Serializable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wifiin.common.GlobalObject;
import com.wifiin.mq.mqtt.exception.SerDerException;
import com.wifiin.mq.mqtt.exception.UnSupportedDeserializationMethodAccessException;

public class JacksonJsonSerDer implements SerDer{

    @Override
    public byte[] serialize(Serializable serializable){
        try{
            return GlobalObject.getJsonMapper().writeValueAsBytes(serializable);
        }catch(JsonProcessingException e){
            throw new SerDerException(e);
        }
    }

    @Override
    public Object deserialize(byte[] bytes){
        throw new UnSupportedDeserializationMethodAccessException();
    }

    @Override
    public <T> T deserialize(byte[] bytes,Class<T> cls){
        try{
            return GlobalObject.getJsonMapper().readValue(bytes,cls);
        }catch(IOException e){
            throw new SerDerException(e);
        }
    }
    
}
