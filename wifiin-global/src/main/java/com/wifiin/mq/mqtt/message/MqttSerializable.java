package com.wifiin.mq.mqtt.message;

import com.wifiin.util.message.Output;

public interface MqttSerializable<T,O extends Output>{
    public O wrap(T t);
    public O wrap();
    public default O serialize(T t){
        return serialize(wrap(t));
    }
    public O serialize(O buf);
}
