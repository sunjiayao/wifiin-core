package com.wifiin.mq.mqtt.message.serder;

import java.io.Serializable;

public interface SerDer{
    public byte[] serialize(Serializable serializable);
    public Object deserialize(byte[] bytes);
    public <T> T deserialize(byte[] bytes,Class<T> cls);
}
