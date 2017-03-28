package com.wifiin.common.query.ip;

import java.util.Map;

import com.wifiin.common.GlobalObject;
import com.wifiin.common.query.RemoteQuery;

public class IpQueryTaobao implements RemoteQuery{

    @Override
    public String getURL(String ip){
        return "http://ip.taobao.com/service/getIpInfo.php?ip="+ip;
    }

    @Override
    public String[] parseResponse(String response) throws Exception{
        Map map=(Map)GlobalObject.getJsonMapper().readValue(response,Map.class).get("data");
        return new String[]{(String)map.get("country"),(String)map.get("region"),(String)map.get("city")};
    }
    
}
