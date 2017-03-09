package com.wifiin.springboot;

import org.springframework.context.ApplicationContext;
/**
 * 维持spring的ApplicationContext实例
 * @author Running
 *
 */
public class ApplicationContextHolder{
    private static ApplicationContext appContext;
    static void setApplicationContext(ApplicationContext appContext){
        ApplicationContextHolder.appContext=appContext;
    }
    public static ApplicationContext getApplicationContext(){
        return appContext;
    }
}
