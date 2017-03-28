package com.wifiin.common.query.cellphone;

import java.util.List;
import java.util.Map;

import com.wifiin.common.CommonConstant;
import com.wifiin.common.GlobalObject;
import com.wifiin.common.query.RemoteQuery;
import com.wifiin.util.Help;

public class CellPhoneBaidu implements RemoteQuery{

    @Override
    public String getURL(String phone){
        return "https://sp0.baidu.com/8aQDcjqpAAV3otqbppnN2DJv/api.php?resource_id=6004&ie=utf8&oe=utf8&query="+phone;
    }
    public String getCharset(){
        return CommonConstant.DEFAULT_CHARSET_NAME;
    }
    @Override
    public String[] parseResponse(String response) throws Exception{
        Map map=GlobalObject.getJsonMapper().readValue(response,Map.class);
        map=(Map)((List)map.get("data")).get(0);
        String prov=getRegion(map,"prov");
        String city=getRegion(map,"city");
        return new String[]{Help.convert(prov,city),city};
    }
    private String getRegion(Map map,String region){
        return (String)map.get(region);
    }
    public static void main(String[] args){
        System.out.println(java.util.Arrays.toString(new CellPhoneBaidu().query("15841949186")));
    }
}
