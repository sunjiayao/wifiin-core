package com.wifiin.mq.mqtt.constant;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

public enum Retain{
    RETAIN(1),
    NO_RETAIN(0);
    private int value;
    private Retain(int value){
        this.value=value;
    }
    public int value(){
        return value;
    }
    private static final Map<Integer,Retain> VALUE_MAP;
    static{
        Builder<Integer,Retain> builder=ImmutableMap.<Integer,Retain>builder();
        for(Retain r:Retain.values()){
            builder.put(r.value,r);
        }
        VALUE_MAP=builder.build();
    }
    public byte format(byte value){
        return (byte)(((~1)&value)|this.value);
    }
    public static Retain valueOf(int value){
        return VALUE_MAP.get(value & RETAIN.value);
    }
}
