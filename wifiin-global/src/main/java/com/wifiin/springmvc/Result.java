package com.wifiin.springmvc;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Result {
    private int status;
    private Object fields;
    @JsonIgnore
    private transient Object log;
    public Result(){}
    public Result(int status,Object fields,Object log){
        this.status=status;
        this.fields=fields;
        this.log=log;
    }
    public int getStatus(){
        return status;
    }
    public void setStatus(int status){
        this.status = status;
    }
    public Object getFields(){
        return fields;
    }
    public void setFields(Object fields){
        this.fields = fields;
    }
    @JsonIgnore
    public Object getLog(){
        return log;
    }
    public void setLog(Object log){
        this.log = log;
    }
    
}
