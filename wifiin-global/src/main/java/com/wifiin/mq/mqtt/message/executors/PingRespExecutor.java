package com.wifiin.mq.mqtt.message.executors;

import com.wifiin.mq.mqtt.constant.MessageType;
import com.wifiin.mq.mqtt.logger.MqttLogger;
import com.wifiin.mq.mqtt.message.MqttMessage;
import com.wifiin.nio.OutputObject;

public class PingRespExecutor implements MqttMessageExecutor{
    PingRespExecutor(){}
    @Override
    public OutputObject execute(MqttMessage mm){
        MqttLogger.debug("MqttMessageExecutor:{}:{}",MessageType.PINGRESP,mm.header().messageType());
        return OutputObject.ACCOMPLISHED;
    }
    
}
