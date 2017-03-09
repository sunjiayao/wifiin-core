package com.wifiin.pool;

import java.util.Set;

public class KeyedPoolConfig<K,T> extends PoolConfig<T>{
    /**
     * 
     */
    private static final long serialVersionUID=1918922860275026347L;
    private Set<K> keys;
    public KeyedPoolConfig(){
        super();
    }

    public KeyedPoolConfig(Set<K> keys,long maxBlockMillisOnExhausted,boolean createOnNoAvailableIfNotFull,boolean testOnTrieve,
            boolean testOnCreate,int retrieveOnInvalidCount,boolean testOnReturn,long testWhenIdleMoreThan,
            long idleTestPeriodMillis,int minIdle,int maxIdle,int maxTotal,
            Class<PooledObjectFactory<T>> pooledObjectFactoryClass)
            throws InstantiationException,IllegalAccessException{
        super(maxBlockMillisOnExhausted,createOnNoAvailableIfNotFull,testOnTrieve,testOnCreate,retrieveOnInvalidCount,
                testOnReturn,testWhenIdleMoreThan,idleTestPeriodMillis,minIdle,maxIdle,maxTotal,pooledObjectFactoryClass);
        this.keys=keys;
    }

    public KeyedPoolConfig(Set<K> keys,long maxBlockMillisOnExhausted,boolean createOnNoAvailableIfNotFull,boolean testOnTrieve,
            boolean testOnCreate,int retrieveOnInvalidCount,boolean testOnReturn,long testWhenIdleMoreThan,
            long idleTestPeriodMillis,int minIdle,int maxIdle,int maxTotal,String pooledObjectFactoryClassName)
            throws InstantiationException,IllegalAccessException{
        super(maxBlockMillisOnExhausted,createOnNoAvailableIfNotFull,testOnTrieve,testOnCreate,retrieveOnInvalidCount,
                testOnReturn,testWhenIdleMoreThan,idleTestPeriodMillis,minIdle,maxIdle,maxTotal,pooledObjectFactoryClassName);
        this.keys=keys;
    }
    public Set<K> getKeys(){
        return keys;
    }
    public void setKeys(Set<K> keys){
        this.keys=keys;
    }
}
