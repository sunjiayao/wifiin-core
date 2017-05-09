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
                        if(r instanceof NamedRunnabled){
                            log.info("ShutdownHookTask {} finished",((NamedRunnabled)r).name());
                        }
                    }catch(Throwable e){
                        if(r instanceof NamedRunnabled){
                            log.warn("ShutdownHookTask:"+((NamedRunnabled)r).name(),e);
                        }else{
                            log.warn("ShutdownHookTask:",e);
                        }
                    }
                });
            }
        });
    }
    public static void addHook(Runnable hook){
        HOOKS.add(hook);
    }
    public static void addHook(NamedRunnabled hook){
        HOOKS.add((Runnable)hook);
    }
    public static interface NamedRunnabled extends Runnable{
        public String name();
    }
}
