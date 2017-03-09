package com.wifiin.springmvc;

public class ResultWithMsg extends Result{
    private String msg;
    public ResultWithMsg(){}
    public ResultWithMsg(int status,String msg,Object fields,Object log){
        super(status,fields,log);
        this.msg=msg;
    }
    public String getMsg(){
        return msg;
    }
    public void setMsg(String msg){
        this.msg = msg;
    }
}
