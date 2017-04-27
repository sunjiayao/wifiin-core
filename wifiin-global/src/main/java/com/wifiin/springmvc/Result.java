package com.wifiin.springmvc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wifiin.multilanguage.rpc.MultiLangRPC;
import com.wifiin.multilanguage.rpc.model.vo.MultiLangData;
import com.wifiin.multilanguage.rpc.model.vo.MultiLangResponse;
import com.wifiin.spring.ApplicationContextHolder;
import com.wifiin.springmvc.exception.MsgCodeNotFoundException;
import com.wifiin.util.WifiinUtil;

public class Result {
    private int status;
    private String msg;
    private Object fields;
    @JsonIgnore
    private transient Object log;
    public Result(){}
    public Result(int status){
        this.status=status;
    }
    public Result(int status,String msg){
        this.status=status;
        this.msg=msg;
    }
    public Result(int status,String msg,Object fields){
        this.status=status;
        this.msg=msg;
        this.fields=fields;
    }
    public Result(int status,String msg,Object fields,Object log){
        this.status=status;
        this.msg=msg;
        this.fields=fields;
        this.log=log;
    }
    
    public Result(int status,String app,String code){
        this.status=status;
        this.msg=msg(app,WifiinUtil.language(),code);
    }
    public Result(int status,String app,String code,Object fields){
        this.status=status;
        this.msg=msg(app,WifiinUtil.language(),code);
        this.fields=fields;
    }
    public Result(int status,String app,String code,Object fields,Object log){
        this.status=status;
        this.msg=msg(app,WifiinUtil.language(),code);
        this.fields=fields;
        this.log=log;
    }
    
    public Result(int status,String app,String lang,String code){
        this.status=status;
        this.msg=msg(app,lang,code);
    }
    public Result(int status,String app,String lang,String code,Object fields){
        this.status=status;
        this.msg=msg(app,lang,code);
        this.fields=fields;
    }
    public Result(int status,String app,String lang,String code,Object fields,Object log){
        this.status=status;
        this.msg=msg(app,lang,code);
        this.fields=fields;
        this.log=log;
    }
    
    public Result(int status,Object fields){
        this.status=status;
        this.fields=fields;
    }
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
    public String getMsg(){
        return msg;
    }
    public void setMsg(String msg){
        this.msg=msg;
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
    public static String msg(String app,String code){
        return msg(app,WifiinUtil.language(),code);
    }
    public static String msg(String app,String lang,String code){
        MultiLangResponse response=null;
        try{
            response=multiLangRPC().queryLang(new MultiLangData(app,lang,code));
        }catch(Exception e){
            throw new MsgCodeNotFoundException("app:"+app+"; lang:"+lang+"; code:"+code,e);
        }
        if(response==null || response.getStatus()<=0){
            throw new MsgCodeNotFoundException("app:"+app+"; lang:"+lang+"; code:"+code);
        }
        return response.getValue();
    }
    private static MultiLangRPC multiLangRPC(){
        return ApplicationContextHolder.getInstance().getBean(MultiLangRPC.class);
    }
}
