package com.wifiin.pool.redis;

import java.util.Set;

import com.wifiin.pool.KeyedPoolConfig;
import com.wifiin.pool.PooledObjectFactory;

public class ShardedRedisPoolConfig<T> extends KeyedPoolConfig<RedisPoolKey,T>{
    /**
     * 
     */
    private static final long serialVersionUID=4064980659278684927L;
    private int connectionTimeout;
    private int soTimeout;
    private String password;
    public ShardedRedisPoolConfig(){}
    public ShardedRedisPoolConfig(Set<RedisPoolKey> keys,long maxBlockMillisOnExhausted,
            boolean createOnNoAvailableIfNotFull,boolean testOnTrieve,boolean testOnCreate,int retrieveOnInvalidCount,
            boolean testOnReturn,long testWhenIdleMoreThan,long idleTestPeriodMillis,int minIdle,int maxIdle,
            int maxTotal,int connectionTimeout,int soTimeout,String password,
            Class<PooledObjectFactory<T>> pooledObjectFactoryClass)
            throws InstantiationException,IllegalAccessException{
        super(keys,maxBlockMillisOnExhausted,createOnNoAvailableIfNotFull,testOnTrieve,testOnCreate,retrieveOnInvalidCount,
                testOnReturn,testWhenIdleMoreThan,idleTestPeriodMillis,minIdle,maxIdle,maxTotal,pooledObjectFactoryClass);
        setConnectionTimeout(connectionTimeout);
        setSoTimeout(soTimeout);
        setPassword(password);
    }
    public ShardedRedisPoolConfig(Set<RedisPoolKey> keys,long maxBlockMillisOnExhausted,
            boolean createOnNoAvailableIfNotFull,boolean testOnTrieve,boolean testOnCreate,int retrieveOnInvalidCount,
            boolean testOnReturn,long testWhenIdleMoreThan,long idleTestPeriodMillis,int minIdle,int maxIdle,
            int maxTotal,int connectionTimeout,int soTimeout,String password,
            String pooledObjectFactoryClassName) throws InstantiationException,IllegalAccessException{
        super(keys,maxBlockMillisOnExhausted,createOnNoAvailableIfNotFull,testOnTrieve,testOnCreate,retrieveOnInvalidCount,
                testOnReturn,testWhenIdleMoreThan,idleTestPeriodMillis,minIdle,maxIdle,maxTotal,pooledObjectFactoryClassName);
        setConnectionTimeout(connectionTimeout);
        setSoTimeout(soTimeout);
        setPassword(password);
    }
    public int getConnectionTimeout(){
        return connectionTimeout;
    }
    public void setConnectionTimeout(int connectionTimeout){
        this.connectionTimeout=connectionTimeout;
    }
    public int getSoTimeout(){
        return soTimeout;
    }
    public void setSoTimeout(int soTimeout){
        this.soTimeout=soTimeout;
    }
    public String getPassword(){
        return password;
    }
    public void setPassword(String password){
        this.password=password;
    }
}
