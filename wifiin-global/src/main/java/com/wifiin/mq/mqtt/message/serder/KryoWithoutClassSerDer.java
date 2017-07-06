package com.wifiin.mq.mqtt.message.serder;

import java.io.Serializable;

import com.wifiin.mq.mqtt.exception.UnSupportedDeserializationMethodAccessException;
import com.wifiin.util.kryo.ThreadLocalKryo;

public class KryoWithoutClassSerDer implements SerDer{

    @Override
    public byte[] serialize(Serializable serializable){
        return ThreadLocalKryo.kryo().write(serializable);
    }

    @Override
    public Object deserialize(byte[] bytes){
        throw new UnSupportedDeserializationMethodAccessException();
    }

    @Override
    public <T> T deserialize(byte[] bytes,Class<T> cls){
        return ThreadLocalKryo.kryo().read(bytes,cls);
    }
    
}
