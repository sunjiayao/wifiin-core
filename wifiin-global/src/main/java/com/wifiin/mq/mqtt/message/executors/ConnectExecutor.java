package com.wifiin.mq.mqtt.message.executors;

import com.wifiin.mq.mqtt.constant.MessageType;
import com.wifiin.mq.mqtt.logger.MqttLogger;
import com.wifiin.mq.mqtt.message.MqttMessage;
import com.wifiin.mq.mqtt.netty.message.ByteBufMqttMessage;
import com.wifiin.nio.OutputObject;

public class ConnectExecutor implements MqttMessageExecutor{
    ConnectExecutor(){}
    @Override
    public OutputObject execute(MqttMessage mm){
        if(mm==null){
            mm = new ByteBufMqttMessage().messageType(MessageType.CONNECT);
        }else{
            mm = new ByteBufMqttMessage().messageType(MessageType.CONNACK);
        }
        MqttLogger.debug("MqttMessageExecutor:{}:{}",MessageType.CONNECT,mm.header().messageType());
        return mm;
    }
    
}
