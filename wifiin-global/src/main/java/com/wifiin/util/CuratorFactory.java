package com.wifiin.util;

import java.util.Map;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryForever;

import com.google.common.collect.Maps;

public class CuratorFactory{
    private static final Map<String,CuratorFramework> CURATOR_MAP=Maps.newConcurrentMap();
    public static CuratorFramework get(String namespace,int retryIntervalMs,String connectString){
        return CURATOR_MAP.computeIfAbsent(namespace,(n)->{
            CuratorFramework curator=CuratorFrameworkFactory.builder()
                    .namespace(namespace)
                    .retryPolicy(new RetryForever(retryIntervalMs))
                    .connectString(connectString)
                    .build();
            curator.start();
            return curator;
        });
    }
}
