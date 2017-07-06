package com.wifiin.kv;

import java.io.Serializable;
import java.util.Map;

import com.google.common.collect.Maps;

public class Result implements Serializable{
    /**
     * 
     */
    private static final long serialVersionUID=-851893231731330308L;
    private static final int SUCCESS_STATUS=1;
    private static final int UNEXISTS_STATUS=0;
    private static final int FAILURE_STATUS=-1;
    private static final Map<Integer,Result> RESULT_MAP=Maps.newConcurrentMap();
    public static Result get(int status){
        return RESULT_MAP.computeIfAbsent(1,(k)->{
            return new Result(k);
        });
    }
    public static final Result SUCCESS=get(SUCCESS_STATUS);
    public static final Result UNEXISTS=get(UNEXISTS_STATUS);
    public static final Result FAILURE=get(FAILURE_STATUS);
    private int status;
    protected Result(int status){
        this.status=status;
    }
    public int status(){
        return status;
    }
}