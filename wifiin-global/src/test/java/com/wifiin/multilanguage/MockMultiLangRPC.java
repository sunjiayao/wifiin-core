package com.wifiin.multilanguage;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Maps;
import com.wifiin.common.GlobalObject;
import com.wifiin.multilanguage.exception.LanguageQueryException;
import com.wifiin.multilanguage.rpc.MultiLangRPC;
import com.wifiin.multilanguage.rpc.model.vo.MultiLangData;
import com.wifiin.multilanguage.rpc.model.vo.MultiLangResponse;

@Component
public class MockMultiLangRPC implements MultiLangRPC{

    @Override
    public MultiLangResponse queryLang(MultiLangData data){
        Map map=Maps.newHashMap();
        map.put("test","test");
        try{
            return new MultiLangResponse(1,GlobalObject.getJsonMapper().writeValueAsString(map));
        }catch(JsonProcessingException e){
            throw new LanguageQueryException(e);
        }
    }
    
}
