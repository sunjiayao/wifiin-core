package com.wifiin.rpc;

import java.io.Serializable;
import java.util.Map;

import com.google.common.collect.Maps;
import com.wifiin.common.exception.BusinessException;
import com.wifiin.common.exception.ResponseImmutableException;

public class RPCCommonResponse implements RPCResponse,Serializable{
    /**
     * 
     */
    private static final long serialVersionUID=-2514403064998062582L;
    private static final Map<Integer,RPCCommonResponse> RESPONSE_MAP=Maps.newConcurrentMap();
    public static final RPCCommonResponse get(int status){
        return RESPONSE_MAP.computeIfAbsent(status,(s)->{
            return new ImmutableRPCCommonResponse(s);
        });
    }
    public static final RPCCommonResponse SUCCESS=get(1);
    public static final RPCCommonResponse FAILURE=get(0);
    private final static class ImmutableRPCCommonResponse extends RPCCommonResponse implements Serializable{
        /**
         * 
         */
        private static final long serialVersionUID=7050735796140223391L;
        public ImmutableRPCCommonResponse(int status){
            super(status);
        }
        public void setStatus(int status){
            throw new ResponseImmutableException("value of status in current instance must not be changed");
        }
    }
    public static RPCCommonResponse get(BusinessException ex){
        return get(ex.getStatus());
    }
    private int status;
    protected RPCCommonResponse(){}
    protected RPCCommonResponse(int status){
        this.status=status;
    }
    @Override
    public void setStatus(int status){
        this.status=status;
    }

    @Override
    public int getStatus(){
        return status;
    }
    
}
