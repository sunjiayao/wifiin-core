package com.wifiin.mq.mqtt.message.executors;

import com.wifiin.mq.mqtt.message.MqttMessage;
import com.wifiin.nio.OutputObject;

public interface MqttMessageExecutor{
    public static final MqttMessageExecutor RESERVED_0X00_EXECUTOR=new Reserved0x00Executor();
    public static final MqttMessageExecutor CONNECT_EXECUTOR=new ConnectExecutor();
    public static final MqttMessageExecutor CONN_ACK_EXECUTOR=new ConnAckExecutor();
    public static final MqttMessageExecutor PUBLISH_EXECUTOR=new PublishExecutor();
    public static final MqttMessageExecutor PUB_ACK_EXECUTOR=new PubAckExecutor();
    public static final MqttMessageExecutor PUB_REC_EXECUTOR=new PubRecExecutor();
    public static final MqttMessageExecutor PUB_REL_EXECUTOR=new PubRelExecutor();
    public static final MqttMessageExecutor PUB_COMP_EXECUTOR=new PubCompExecutor();
    public static final MqttMessageExecutor SUBSCRIBE_EXECUTOR=new SubscribeExecutor();
    public static final MqttMessageExecutor SUB_ACK_EXECUTOR=new SubAckExecutor();
    public static final MqttMessageExecutor UNSUBSCRIBE_EXECUTOR=new UnsubscribeExecutor();
    public static final MqttMessageExecutor UNSUB_ACK_EXECUTOR=new UnSubAckExecutor();
    public static final MqttMessageExecutor PING_REQ_EXECUTOR=new PingReqExecutor();
    public static final MqttMessageExecutor PING_RESP_EXECUTOR=new PingRespExecutor();
    public static final MqttMessageExecutor DISCONNECT_EXECUTOR=new DisconnectExecutor();
    public static final MqttMessageExecutor RESERVED_0XF0_EXECUTOR=new Reserved0xf0Executor();
    @SuppressWarnings("rawtypes")
    public OutputObject execute(MqttMessage mm);
}
