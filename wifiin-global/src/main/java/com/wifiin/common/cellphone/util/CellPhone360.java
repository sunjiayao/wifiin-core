package com.wifiin.common.cellphone.util;

import java.util.Map;

import com.wifiin.common.GlobalObject;

public class CellPhone360 implements CellPhoneHome{

    @Override
    public String getURL(String phone){
        return "http://cx.shouji.360.cn/phonearea.php?number="+phone;
    }

    @Override
    public String[] parseResponse(String response)throws Exception{
        Map map=GlobalObject.getJsonMapper().readValue(response,Map.class);
        map=(Map)map.get("data");
        return new String[]{getRegion(map,"province"),getRegion(map,"city")};
    }
    private String getRegion(Map map,String region){
        return (String)map.get(region);
    }
    public static void main(String[] args){
        System.out.println(java.util.Arrays.toString(new CellPhone360().query("15841949186")));
    }
}
