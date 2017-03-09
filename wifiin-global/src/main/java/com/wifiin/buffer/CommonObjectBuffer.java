package com.wifiin.buffer;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.wifiin.exception.CommonObjectBufferException;

/**
 * @author wujingrun
 *         技术：guava缓存
 *         用途：缓存任意对象列表
 *         很多时候会需要暂时把数据缓存起来，过段时间再使用，比如接收文件，导入数据，先缓存起来，用户做一些判断或处理再决定下一步
 */
public class CommonObjectBuffer<O>{
    private static final Logger logger=LoggerFactory.getLogger(CommonObjectBuffer.class);
    private static final Cache<String,CommonObjectBuffer<?>> CACHE=CacheBuilder.newBuilder().expireAfterAccess(5,TimeUnit.MINUTES).build();// 有效时长5分钟(缓存内没有操作后)
    private static final String CACHE_NAME="cache";
    private static final Callable<CommonObjectBuffer<?>> BUFFER_CALLABLE=CommonObjectBuffer::new;
    private LinkedBlockingQueue<O> buffer;
    
    @SuppressWarnings("unchecked")
    public static <O> CommonObjectBuffer<O> getInstance(String tag){
        try{
            return (CommonObjectBuffer<O>)CACHE.get(tag,BUFFER_CALLABLE);
        }catch(ExecutionException e){
            throw new CommonObjectBufferException(e);
        }
    }
    
    private CommonObjectBuffer(){
        buffer=new LinkedBlockingQueue<O>();
    }
    
    /**
     * 缓存开始前清空缓存
     */
    public void start() throws ExecutionException{
        buffer.clear();
    }
    
    /**
     * 缓存结束后清空缓存
     */
    public void end() throws ExecutionException{
        buffer.clear();
    }
    
    public boolean isEmpty(long adminLogId) throws ExecutionException{
        return buffer.isEmpty();
    }
    
    public boolean isNotEmpty(long adminLogId) throws ExecutionException{
        return !isEmpty(adminLogId);
    }
    
    /**
     * 存数据到缓存
     */
    public void push(O o) throws ExecutionException{
        buffer.offer(o);
    }
    
    /**
     * 获取缓存中的数据并删除
     */
    public O pop() throws ExecutionException{
        return buffer.poll();
    }
    
    /**
     * 遍历缓存中的数据
     */
    public void each(Consumer<O> action) throws ExecutionException{
        buffer.forEach(action);
    }
    
    /**
     * 遍历缓存中的数据并清空缓存,串行执行
     */
    public void eachThenEnd(Consumer<O> action) throws ExecutionException{
        try{
            buffer.forEach(action);
        }finally{
            end();
        }
    }
    
    /**
     * 遍历缓存中的数据,并行执行
     */
    public void parallelEach(Consumer<O> consumer) throws ExecutionException,InterruptedException{
        int bufferSize=bufferSize();
        CountDownLatch latch=new CountDownLatch(bufferSize);
        buffer.parallelStream().forEach((t)->{
            try{
                consumer.accept(t);
            }finally{
                latch.countDown();
            }
        });
        latch.await();
    }
    
    /**
     * 遍历缓存中的数据并清空缓存,并行执行
     */
    public void parallelEachThenEnd(Consumer<O> consumer) throws ExecutionException,InterruptedException{
        try{
            parallelEach(consumer);
        }finally{
            end();
        }
    }
    
    /**
     * 缓存长度
     */
    public int bufferSize() throws ExecutionException{
        return buffer.size();
    }
    
}
