package com.wifiin.multilanguage.rpc.model.vo;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import com.wifiin.common.GlobalObject;
import com.wifiin.multilanguage.aop.exception.LanguageQueryException;

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
    @SuppressWarnings("unchecked")
    public Map<String,String> getFieldValues(){
        return getFieldValues(Map.class);
    }
    public <T> T getFieldValues(Class<T> modleClass){
        try{
            return GlobalObject.getJsonMapper().readValue(value,modleClass);
        }catch(IOException e){
            throw new LanguageQueryException(e);
        }
    }
}
