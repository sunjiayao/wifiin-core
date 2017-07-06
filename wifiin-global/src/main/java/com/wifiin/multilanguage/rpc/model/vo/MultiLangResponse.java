package com.wifiin.multilanguage.rpc.model.vo;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wifiin.common.JSON;
import com.wifiin.multilanguage.aop.exception.LanguageQueryException;
import com.wifiin.util.Help;

public class MultiLangResponse implements Serializable{
    /**
     * 
     */
    private static final long serialVersionUID=-7770786641745854262L;
    private int status;
    private String value;
    public MultiLangResponse(int status,String value){
        this.status=status;
        this.value=value;
    }
    public int getStatus(){
        return status;
    }
    public String getValue(){
        return value;
    }
    @JsonIgnore
    @SuppressWarnings("unchecked")
    public Map<String,String> getFieldValues(){
        Map map = getFieldValues(Map.class);
        if(map==null){
            return Collections.EMPTY_MAP;
        }
        return map;
    }
    public <T> T getFieldValues(Class<T> modleClass){
        if(Help.isEmpty(value)){
            return null;
        }
        try{
            return JSON.parse(value,modleClass);
        }catch(Exception e){
            throw new LanguageQueryException(e);
        }
    }
}
