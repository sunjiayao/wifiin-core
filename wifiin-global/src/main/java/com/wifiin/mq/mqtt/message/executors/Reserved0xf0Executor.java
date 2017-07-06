package com.wifiin.mq.mqtt.message.executors;

import com.wifiin.mq.mqtt.message.MqttMessage;
import com.wifiin.nio.OutputObject;

public class Reserved0xf0Executor implements MqttMessageExecutor{
    Reserved0xf0Executor(){}
    @Override
    public OutputObject execute(MqttMessage mm){
        throw new IllegalAccessError("message type 0xf0 is reserved");
    }
    
}
