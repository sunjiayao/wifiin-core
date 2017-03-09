package com.wifiin.redis;

import java.io.Serializable;
import java.util.Set;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.FactoryBean;

import com.wifiin.config.ConfigManager;

/**
 * 使用ConfigManager管理的redis连接池。
 * 依赖jedis
 * @author Running
 */
public class ConfigManagedRedisConnectionPoolFactoryBean implements FactoryBean<RedisConnection>{
    /**
     * redis的配置参数在ConfigManager的key
     */
    public static final String SENTINEL_JEDIS_POOL_CONFIG="sentinel.jedis.pool.config";
    public static class SerializableGenericObjectPoolConfig extends GenericObjectPoolConfig implements Serializable{

        /**
         * 
         */
        private static final long serialVersionUID=1226420657667399986L;
    }
    /**
     * redis配置数据
     * sentinel，一个字符串一个sentinel主机，格式是ip:port
     * timeout, 连接超时
     * poolConfig, 对象池配置参数
     * @author Running
     *
     */
    public static class RedisSentinelConfig implements Serializable{
        /**
         * 
         */
        private static final long serialVersionUID=-2842437569186962482L;
        private Set<String> sentinel;
        private String password;
        private int timeout;
        private SerializableGenericObjectPoolConfig poolConfig;
        public Set<String> getSentinel(){
            return sentinel;
        }
        public void setSentinel(Set<String> sentinel){
            this.sentinel=sentinel;
        }
        public String getPassword(){
            return password;
        }
        public void setPassword(String password){
            this.password=password;
        }
        public int getTimeout(){
            return timeout;
        }
        public void setTimeout(int timeout){
            this.timeout=timeout;
        }
        public GenericObjectPoolConfig getPoolConfig(){
            return poolConfig;
        }
        public void setPoolConfig(SerializableGenericObjectPoolConfig poolConfig){
            this.poolConfig=poolConfig;
        }
        
    }
    /**
     * 返回redis连接池实例
     * @return
     */
    @Override
    public RedisConnection getObject() throws Exception{
        RedisSentinelConfig conf=ConfigManager.getInstance().getObject(SENTINEL_JEDIS_POOL_CONFIG,RedisSentinelConfig.class,new RedisSentinelConfig());
        return new JedisConnection(new RedisSentinelPool(conf.sentinel,conf.password,conf.timeout,conf.poolConfig));
    }
    @Override
    public Class<RedisConnection> getObjectType(){
        return RedisConnection.class;
    }
    @Override
    public boolean isSingleton(){
        return true;
    }
    
}
