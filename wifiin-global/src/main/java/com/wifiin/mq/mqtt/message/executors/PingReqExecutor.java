package com.wifiin.mq.mqtt.message.executors;

import com.wifiin.mq.mqtt.constant.MessageType;
import com.wifiin.mq.mqtt.logger.MqttLogger;
import com.wifiin.mq.mqtt.message.MqttMessage;
import com.wifiin.mq.mqtt.netty.message.ByteBufMqttMessage;
import com.wifiin.nio.OutputObject;

public class PingReqExecutor implements MqttMessageExecutor{
    PingReqExecutor(){}
    @Override
    public OutputObject execute(MqttMessage mm){
        if(mm==null){
            mm=new ByteBufMqttMessage().messageType(MessageType.PINGREQ);
        }else{
            mm=new ByteBufMqttMessage().messageType(MessageType.PINGRESP);
        }
        MqttLogger.debug("MqttMessageExecutor:{}:{}",MessageType.PINGREQ,mm.header().messageType());
        return mm;
    }
    
}
