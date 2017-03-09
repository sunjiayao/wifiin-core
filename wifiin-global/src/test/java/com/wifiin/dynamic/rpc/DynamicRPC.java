package com.wifiin.dynamic.rpc;

import com.alibaba.dubbo.config.annotation.Reference;

public interface DynamicRPC{
    public int execute(String arg);
}
