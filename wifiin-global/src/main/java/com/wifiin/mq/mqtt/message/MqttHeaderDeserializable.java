package com.wifiin.mq.mqtt.message;

import com.wifiin.util.message.Input;

public interface MqttHeaderDeserializable<I extends Input>{
    public void parseLength(I in);
}
