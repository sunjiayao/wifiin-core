package com.wifiin.kv;

import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Maps;
import com.wifiin.kv.util.UUIDUtil;
import com.wifiin.util.Help;

public class ResultSinkMap{
    private static final Map<UUID ,ResultSink> RESULT_SINKS=Maps.newConcurrentMap();
    private static final ResultSink EMPTY_SINK=(r)->{};
    public static void put(byte[] uuid,ResultSink sink){
        put(UUIDUtil.parse(uuid),sink);
    }
    public static void put(UUID uuid,ResultSink sink){
        RESULT_SINKS.put(uuid,sink);
    }
    public static ResultSink get(byte[] uuid){
        return get(UUIDUtil.parse(uuid));
    }
    public static ResultSink get(UUID uuid){
        return Help.convert(RESULT_SINKS.get(uuid),EMPTY_SINK);
    }
    public static ResultSink remove(byte[] uuid){
        return remove(UUIDUtil.parse(uuid));
    }
    public static ResultSink remove(UUID uuid){
        return Help.convert(RESULT_SINKS.remove(uuid),EMPTY_SINK);
    }
}
