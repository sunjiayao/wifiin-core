package com.wifiin.mq.mqtt.message.serder;

import java.io.Serializable;

import com.wifiin.mq.mqtt.exception.UnSupportedDeserializationMethodAccessException;
import com.wifiin.util.kryo.ThreadLocalKryo;

public class KryoWithClassSerDer implements SerDer{

    @Override
    public byte[] serialize(Serializable serializable){
        return ThreadLocalKryo.kryo().writeClassAndObject(serializable);
    }

    @Override
    public Object deserialize(byte[] bytes){
        return ThreadLocalKryo.kryo().read(bytes);
    }

    @Override
    public <T> T deserialize(byte[] bytes,Class<T> cls){
        throw new UnSupportedDeserializationMethodAccessException();
    }
    
}
