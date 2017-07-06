package com.wifiin.mq.mqtt.constant;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

public enum DupFlag{
    REDELIEVER(0x08),
    NON_REDELIEVER(0x00);
    private int value;
    private DupFlag(int value){
        this.value=value;
    }
    public int value(){
        return value;
    }
    private static final Map<Integer,DupFlag> VALUE_MAP;
    static{
        Builder<Integer,DupFlag> builder=ImmutableMap.<Integer,DupFlag>builder();
        for(DupFlag f:DupFlag.values()){
            builder.put(f.value,f);
        }
        VALUE_MAP=builder.build();
    }
    public byte format(byte value){
        return (byte)(((~0x08)&value)|this.value);
    }
    public static DupFlag valueOf(int value){
        return VALUE_MAP.get(value & REDELIEVER.value);
    }
}
