package com.wifiin.mq.mqtt.message.executors;

import com.wifiin.mq.mqtt.constant.MessageType;
import com.wifiin.mq.mqtt.logger.MqttLogger;
import com.wifiin.mq.mqtt.message.MqttMessage;
import com.wifiin.nio.OutputObject;

public class DisconnectExecutor implements MqttMessageExecutor{
    DisconnectExecutor(){}
    @Override
    public OutputObject execute(MqttMessage mm){
        MqttLogger.debug("MqttMessageExecutor:{}:{}",MessageType.DISCONNECT,mm.header().messageType());
        return OutputObject.CLOSE_CHANNEL;
    }
    
}
