package com.wifiin.cron;

import java.util.Date;

import org.slf4j.Logger;

import com.wifiin.log.LoggerFactory;
import com.wifiin.util.Help;

/**
 * 定时任务对象，包含定时任务名、执行周期表达式、是否可并发执行等
 * @author Running
 *
 */
public interface CronTask extends Runnable{
    static final Logger log=LoggerFactory.getLogger(CronTask.class);
    static final ThreadLocal<String> cronExpr=new ThreadLocal<>();
    /**
     * 定时任务名
     * @return
     */
    public String name();
    /**
     * 当前任务是否正在执行，可以重写此方法实现分布式定时器,默认是false
     * @return
     */
    public default boolean executing(){
        return false;
    }
    /**
     * 本任务是否可以执行
     * @return
     */
    public default boolean executable(){
        return concurrentable() || !executing();
    }
    /**
     * 本次任务执行结束执行一些清理逻辑。如果canExecute()返回false，就不执行本方法
     */
    public default void clean(){}
    /**
     * 是否允许并发执行，默认是true
     * @return
     */
    public default boolean concurrentable(){
        return true;
    }
    /**
     * 执行定时任务的业务逻辑
     */
    public void execute();
    /**
     * 记录最后执行时间
     * @param executedTime
     */
    public default void executedAt(Date executedTime){
        log.info("CronTask.executed:{};{}",name(),cron0());
    }
    /**
     * 类CronTaskTrigger会用这个方法得到cron表达式
     * @return
     */
    public String cron();
    /**
     * 不要覆盖本方法
     * @return
     */
    public default String cron0(){
        String expr=cronExpr.get();
        if(Help.isEmpty(expr)){
            expr=cron();
            cronExpr.set(expr);
        }
        return expr;
    }
    @Override
    public default void run(){
        try{
            if(executable()){
                try{
                    execute();
                    executedAt(new Date());
                }finally{
                    clean();
                }
            }
        }catch(Exception e){
            log.warn("CronTask.run:{},{}",name(),cron0(),e);
        }
    }
}
