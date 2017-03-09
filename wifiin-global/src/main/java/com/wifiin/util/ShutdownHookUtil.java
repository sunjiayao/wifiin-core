package com.wifiin.util;

import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Queues;

public class ShutdownHookUtil{
    private static final Logger log=LoggerFactory.getLogger(ShutdownHookUtil.class);
    private static final LinkedBlockingQueue<Runnable> HOOKS=Queues.newLinkedBlockingQueue();
    static{
        Runtime.getRuntime().addShutdownHook(new Thread(){
            public void run(){
                HOOKS.parallelStream().forEach((r)->{
                    try{
                        r.run();
                    }catch(Exception e){
                        log.warn("ShutdownHook:",e);
                    }
                });
            }
        });
    }
    public static void addHook(Runnable hook){
        HOOKS.add(hook);
    }
}
