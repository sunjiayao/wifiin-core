package com.wifiin.dynamic.rpc;

import com.wifiin.dynamic.rpc.test.DummyDynamicRPC;

public class DynamicRPCImpl implements DummyDynamicRPC{

    @Override
    public int execute(String arg){
        try{
            return Integer.parseInt(arg);
        }catch(Exception e){
            return 0;
        }
    }
    
}
