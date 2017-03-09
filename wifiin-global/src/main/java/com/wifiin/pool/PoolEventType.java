package com.wifiin.pool;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Maps;
import com.wifiin.util.ShutdownHookUtil;
/**
 * 池事件，每个枚举值一个事件线程，池事件处理逻辑在Pool类
 * @author Running
 *
 */
public enum PoolEventType{
    /**
     * 池对象获取事件，目前只做一些获取池对象的统计
     */
    TRIEVED,
    /**
     * 池对象返回事件，目前只做一些返回池对象的统计
     */
    RETURNED,
    /**
     * 池对象太少事件，完成向空闲队列添加池对象，直到maxIdle
     */
    TOO_FEW,
    /**
     * 池对象退役事件，退役线程池任意时刻只接收一个任务，更多任务将被丢弃
     */
    RETIRE{
        @Override
        protected ExecutorService create(){
            return new ThreadPoolExecutor(1, 1,
                    0L, TimeUnit.MILLISECONDS,
                    new SynchronousQueue<Runnable>(),
                    new ThreadPoolExecutor.DiscardPolicy());
        }
    },
    ;
    private static final Map<PoolEventType,ExecutorService> executors=Maps.newEnumMap(PoolEventType.class);
    static{
        for(PoolEventType et:PoolEventType.values()){
            executors.put(et,et.create());
        }
        ShutdownHookUtil.addHook(()->{
            executors.values().forEach((c)->{
                c.shutdownNow();
            });
        });
    }
    void execute(Runnable runnable){
        executors.get(this).submit(runnable);
    }
    protected ExecutorService create(){
        return Executors.newSingleThreadExecutor();
    }
}
