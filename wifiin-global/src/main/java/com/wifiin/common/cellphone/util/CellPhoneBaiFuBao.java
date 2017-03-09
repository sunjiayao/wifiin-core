package com.wifiin.common.cellphone.util;

import java.util.Map;

import com.wifiin.common.GlobalObject;

public class CellPhoneBaiFuBao implements CellPhoneHome{
    @Override
    public String getURL(String phone){
        return "https://www.baifubao.com/callback?cmd=1059&callback=iteblog&phone="+phone;
    }
    @Override
    public String[] parseResponse(String response)throws Exception{
        Map map=GlobalObject.getJsonMapper().readValue(response.trim().substring(response.indexOf('(')+1,response.length()-1).trim(),Map.class);
        return new String[]{(String)((Map)map.get("data")).get("area"),""};
    }
    public static void main(String[] args){
        System.out.println(java.util.Arrays.toString(new CellPhoneBaiFuBao().query("15841949186")));
    }
}
