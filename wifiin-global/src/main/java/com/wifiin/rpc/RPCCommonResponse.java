package com.wifiin.rpc;

import java.io.Serializable;

import com.wifiin.rpc.exception.RPCResponseImmutableException;

public class RPCCommonResponse implements RPCResponse,Serializable{
    /**
     * 
     */
    private static final long serialVersionUID=-2514403064998062582L;
    public static final RPCCommonResponse SUCCESS=new ImmutableRPCCommonResponse(1);
    public static final RPCCommonResponse FAILURE=new ImmutableRPCCommonResponse(0);
    private static class ImmutableRPCCommonResponse extends RPCCommonResponse implements Serializable{
        /**
         * 
         */
        private static final long serialVersionUID=7050735796140223391L;
        public ImmutableRPCCommonResponse(int status){
            super(status);
        }
        public void setStatus(int status){
            throw new RPCResponseImmutableException("value of status in current instance must not be changed");
        }
    }
    private int status;
    public RPCCommonResponse(){}
    public RPCCommonResponse(int status){
        setStatus(status);
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
