package com.wifiin.springmvc;

import java.io.Serializable;
import java.util.Map;

import com.google.common.collect.Maps;
import com.wifiin.common.CommonResponse;
import com.wifiin.common.exception.BusinessException;
import com.wifiin.common.exception.ResponseImmutableException;

public class MVCCommonResponse implements CommonResponse,Serializable{
    /**
     * 
     */
    private static final long serialVersionUID=8043439439467793629L;
    private static final Map<Integer,MVCCommonResponse> RESPONSE_MAP=Maps.newConcurrentMap();
    public static final MVCCommonResponse get(int status){
        return RESPONSE_MAP.computeIfAbsent(status,(s)->{
            return new ImmutableMVCCommonResponse(s);
        });
    }
    public static final MVCCommonResponse SUCCESS=get(1);
    public static final MVCCommonResponse FAILURE=get(0);
    private final static class ImmutableMVCCommonResponse extends MVCCommonResponse implements Serializable{
        /**
         * 
         */
        private static final long serialVersionUID=7050735796140223391L;
        public ImmutableMVCCommonResponse(int status){
            super(status);
        }
        public void setStatus(int status){
            throw new ResponseImmutableException("value of status in current instance must not be changed");
        }
    }
    public static MVCCommonResponse get(BusinessException ex){
        return get(ex.getStatus());
    }
    private int status;
    protected MVCCommonResponse(){}
    protected MVCCommonResponse(int status){
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
