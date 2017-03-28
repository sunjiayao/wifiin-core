package com.wifiin.common.query.cellphone;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.wifiin.common.query.RemoteQuery;

public class CellPhoneTenPay implements RemoteQuery{

    @Override
    public String getURL(String phone){
        return "http://life.tenpay.com/cgi-bin/mobile/MobileQueryAttribution.cgi?chgmobile="+phone;
    }

    @Override
    public String[] parseResponse(String response)throws Exception{
        Document xml=DocumentHelper.parseText(response);
        Element root=xml.getRootElement();
        return new String[]{root.element("province").getTextTrim(),root.element("city").getTextTrim()};
    }
    public static void main(String[] args){
        System.out.println(java.util.Arrays.toString(new CellPhoneTenPay().query("15841949186")));
    }
}
