package com.wifiin.datasource;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import javax.sql.DataSource;
/**
 * DataSource代理，配置管理的DataSource配置参数变化时自动创建新的DataSource并销毁旧的
 * @author Running
 *
 */
public class WatchableConfigManagedDataSource implements DataSource, Closeable{
    private ScheduledExecutorService prevCloser;
    private ConfigManagedDataSource datasource;
    private AtomicBoolean open=new AtomicBoolean(true);
    public WatchableConfigManagedDataSource(){
        buildDataSource();
        watch();
    }
    /**
     * 监视数据源配置的变化
     */
    private void watch(){
        ConfigManagedDataSource.watch(()->{
            this.buildDataSource();
        });
    }
    /**
     * 得到定时任务线程
     * @return
     */
    private ScheduledExecutorService getPrevCloser(){
        if(prevCloser!=null){
            return prevCloser;
        }
        return prevCloser=Executors.newScheduledThreadPool(1);
    }
    /**
     * 关闭定时任务线程
     */
    private void closePrevCloser(){
        prevCloser.shutdown();
        this.prevCloser=null;
    }
    /**
     * 关闭旧的源。可能有线程安全问题，不过这个方法不常触发。只有在频繁修改数据源配置才可能发生线程安全问题。
     * @param prev
     */
    private void closePrev(ConfigManagedDataSource prev){
        ScheduledExecutorService prevCloser=getPrevCloser();
        prevCloser.schedule(()->{
            try{
                if(prev!=null && !prev.isClosed()){
                    prev.close();
                }
            }finally{
                closePrevCloser();
            }
        },1,TimeUnit.SECONDS);
    }
    /**
     * 建造数据源
     */
    private synchronized void buildDataSource(){
        if(open.get()){
            ConfigManagedDataSource prev=this.datasource;
            this.datasource=new ConfigManagedDataSource();
            closePrev(prev);
        }
    }
    @Override
    public PrintWriter getLogWriter() throws SQLException{
        return datasource.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException{
        datasource.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException{
        datasource.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException{
        return datasource.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException{
        return datasource.getParentLogger();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException{
        return datasource.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException{
        return datasource.isWrapperFor(iface);
    }

    @Override
    public synchronized void close() throws IOException{
        open.set(false);
        datasource.close();
    }

    @Override
    public Connection getConnection() throws SQLException{
        return datasource.getConnection();
    }

    @Override
    public Connection getConnection(String username,String password) throws SQLException{
        return datasource.getConnection(username,password);
    }
    
}
