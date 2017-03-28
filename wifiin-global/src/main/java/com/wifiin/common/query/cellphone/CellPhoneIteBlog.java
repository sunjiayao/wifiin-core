package com.wifiin.common.query.cellphone;

import java.util.Map;

import com.wifiin.common.GlobalObject;
import com.wifiin.common.query.RemoteQuery;

public class CellPhoneIteBlog implements RemoteQuery{

    @Override
    public String getURL(String phone){
        return "https://www.iteblog.com/api/mobile.php?mobile="+phone;
    }

    @SuppressWarnings("unchecked")
    @Override
    public String[] parseResponse(String response)throws Exception{
        Map<String,String> map=GlobalObject.getJsonMapper().readValue(response,Map.class);
        return new String[]{map.get("province"),map.get("city")};
    }
    public static void main(String[] args){
        System.out.println(java.util.Arrays.toString(new CellPhoneIteBlog().query("15841949186")));
    }
}
