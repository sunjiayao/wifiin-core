package com.wifiin.mq.mqtt.message.serder;

import java.io.Serializable;

import com.wifiin.common.GlobalObject;
import com.wifiin.mq.mqtt.exception.UnSupportedDeserializationMethodAccessException;

public class FstBinarySerDer implements SerDer{

    @Override
    public byte[] serialize(Serializable serializable){
        return GlobalObject.getFSTConfiguration().asByteArray(serializable);
    }

    @Override
    public Object deserialize(byte[] bytes){
        return GlobalObject.getFSTConfiguration().asObject(bytes);
    }

    @Override
    public <T> T deserialize(byte[] bytes,Class<T> cls){
        throw new UnSupportedDeserializationMethodAccessException();
    }
    
}
