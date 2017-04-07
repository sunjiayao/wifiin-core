package com.wifiin.multilanguage.aop.rpc.model.vo;

import java.io.Serializable;

public class MultiLangData implements Serializable{
    /**
     * 
     */
    private static final long serialVersionUID=-5160233929712568144L;
    private String app;
    private String lang;
    private String key;
    public MultiLangData(String app,String lang,String key){
        this.app=app;
        this.lang=lang;
        this.key=key;
    }
    public String getApp(){
        return app;
    }
    public String getLang(){
        return lang;
    }
    public String getKey(){
        return key;
    }
}
