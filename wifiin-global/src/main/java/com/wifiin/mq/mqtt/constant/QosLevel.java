package com.wifiin.mq.mqtt.constant;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.wifiin.mq.mqtt.netty.exception.IllegalHeaderException;

public enum QosLevel{
    AT_MOST_ONCE(0x00),
    AT_LIST_ONCE(0x02),
    EXACTLY_ONCE(0x04),
    RESERVED(0x06){
        public int value(){
            throw new IllegalHeaderException("QosLevel 0x06 is reserved");
        }
    };
    private int value;
    private QosLevel(int value){
        this.value=value;
    }
    public int value(){
        return value;
    }
    private static final Map<Integer,QosLevel> VALUE_MAP;
    static{
        Builder<Integer,QosLevel> builder=ImmutableMap.<Integer,QosLevel>builder();
        for(QosLevel l:QosLevel.values()){
            builder.put(l.value,l);
        }
        VALUE_MAP=builder.build();
    }
    public byte format(byte value){
        return (byte)(((~0x06)&value)|this.value);
    }
    public static QosLevel valueOf(int value){
        QosLevel level=VALUE_MAP.get(value & RESERVED.value);
        level.value();
        return level;
    }
}
