package com.wifiin.datasource;

import java.io.Serializable;

import com.wifiin.config.ConfigManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
/**
 * 用配置管理工具维护配置参数的DataSource，继承的@see com.zaxxer.hikari.HikariDataSource。
 * 本类从ConfigManager获取配置信息。
 * 配置key是@see DATASOURCE_CONFIG。
 * 使用下述方式向配置管理工具添加连接池配置参数：
 * com.zaxxer.hikari.HikariConfig config=new com.zaxxer.hikari.HikariConfig();
 * //添加config的属性
 * 要使用ConfigManager.getInstance().setDataOrCreate(ConfigManagedDataSource.DATASOURCE_CONFIG,config);
 * 
 * @author Running
 */
public class ConfigManagedDataSource extends HikariDataSource{
    /**
     * ConfigManager用这个key得到DataSource配置数据
     */
    public static final String DATASOURCE_CONFIG="datasource.config";
    public static class SerializableHikariConfig extends HikariConfig implements Serializable{

        /**
         * 
         */
        private static final long serialVersionUID=8577090161528694595L;
    }
    public ConfigManagedDataSource(){
        this(DATASOURCE_CONFIG);
    }
    public ConfigManagedDataSource(String key){
        super(ConfigManager.getInstance().getObject(key,SerializableHikariConfig.class,new SerializableHikariConfig()));
    }
    /**
     * watch触发后，自动重新watch
     * @param runnable
     */
    static void watch(Runnable runnable){
        ConfigManager.getInstance().watch(DATASOURCE_CONFIG,(k)->{
            watch(runnable);
            runnable.run();
        });
    }
}
