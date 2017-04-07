package com.wifiin.constant;

import com.wifiin.common.CommonConstant;
import com.wifiin.config.ConfigManager;

public class WifiinConstant {
    private static final String COMMON_CACHE_LIFE_KEY="wifiin.cache.life";
    private static final int DEFAULT_CACHE_LIFE_SECONDS=3600;
    public static int getCacheLifeSeconds(){
        return ConfigManager.getInstance().getInt(COMMON_CACHE_LIFE_KEY,DEFAULT_CACHE_LIFE_SECONDS);
    }
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
