package com.wifiin.util;

import java.util.concurrent.atomic.AtomicBoolean;
/**
 * 应用于无限循环的判断条件，在jvm进程终止时自动退出循环
 * @author Running
 *
 */
public class EndlessLoop{
    private static final AtomicBoolean CONTINUOUS=new AtomicBoolean(true);
    
    static{
        ShutdownHookUtil.addHook(()->{
            CONTINUOUS.set(false);
        });
    }
    public static boolean continuous(){
        return CONTINUOUS.get();
    }
}
