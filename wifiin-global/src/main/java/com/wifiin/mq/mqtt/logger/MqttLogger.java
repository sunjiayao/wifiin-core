package com.wifiin.mq.mqtt.logger;

import org.slf4j.Logger;

import com.wifiin.log.LoggerFactory;
import com.wifiin.mq.mqtt.constant.MqttConstant;

public class MqttLogger{
    private static final Logger log=LoggerFactory.getLogger(MqttConstant.MQTT_LOG_NAME);
    public static void debug(String template,Object... args){
        if(log.isDebugEnabled()){
            log.debug(template,args);
        }
    }
}
