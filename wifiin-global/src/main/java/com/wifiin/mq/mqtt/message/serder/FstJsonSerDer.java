package com.wifiin.mq.mqtt.message.serder;

import java.io.Serializable;

import org.nustaq.serialization.FSTConfiguration;

import com.wifiin.mq.mqtt.exception.UnSupportedDeserializationMethodAccessException;

public class FstJsonSerDer implements SerDer{
    private static final FSTConfiguration fst=FSTConfiguration.createJsonConfiguration(false,false);
    @Override
    public byte[] serialize(Serializable serializable){
        return fst.asByteArray(serializable);
    }

    @Override
    public Object deserialize(byte[] bytes){
        return fst.asObject(bytes);
    }

    @Override
    public <T> T deserialize(byte[] bytes,Class<T> cls){
        throw new UnSupportedDeserializationMethodAccessException();
    }
    
}
