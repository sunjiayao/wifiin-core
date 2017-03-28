package com.wifiin.common.query.ip;

import java.util.Map;

import com.wifiin.common.GlobalObject;
import com.wifiin.common.query.RemoteQuery;

public class IpQuerySina implements RemoteQuery{

    @Override
    public String getURL(String ip){
        return "http://int.dpool.sina.com.cn/iplookup/iplookup.php?format=json&ip="+ip;
    }

    @Override
    public String[] parseResponse(String response) throws Exception{
        Map map=(Map)GlobalObject.getJsonMapper().readValue(response,Map.class);
        return new String[]{(String)map.get("country"),(String)map.get("province"),(String)map.get("city")};
    }
    
}
