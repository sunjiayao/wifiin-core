package com.wifiin.constant;

import com.wifiin.common.CommonConstant;

public class WifiinConstant {
    public static final String TOKEN_TIME_FORMAT = "yyyyMMddHHmmssSSS";
    public static int getVERIFY_CODE_RANDOM_START(){
        return CommonConstant.getIntConstant("wifiin.verify.code.random.start", 10);
    }
    public static int getTOKEN_LIFE(){
        return CommonConstant.getIntConstant("wifiin.token.life", 1800);
    }
    public static String getHttpProxiesSrc(){
        return CommonConstant.getStringConstant("wifiin.http.proxies.src","");
    }
    public static String getHttpProxyFilterUrl(){
        return CommonConstant.getStringConstant("wifiin.http.proxies.filter.url","");
    }
}
