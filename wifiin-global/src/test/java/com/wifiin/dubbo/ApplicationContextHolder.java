package com.wifiin.dubbo;

import org.springframework.context.ApplicationContext;

public class ApplicationContextHolder{
    private static ApplicationContext context;
    static void setApplicationContext(ApplicationContext context){
        ApplicationContextHolder.context=context;
    }
    public static ApplicationContext getApplicationContext(){
        return context;
    }
}
