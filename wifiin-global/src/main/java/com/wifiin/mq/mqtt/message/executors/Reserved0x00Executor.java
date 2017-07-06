package com.wifiin.mq.mqtt.message.executors;

import com.wifiin.mq.mqtt.message.MqttMessage;
import com.wifiin.nio.OutputObject;

public class Reserved0x00Executor implements MqttMessageExecutor{
    Reserved0x00Executor(){}
    @Override
    public OutputObject execute(MqttMessage mm){
        throw new IllegalAccessError("message type 0x00 is reserved");
    }
    
}
