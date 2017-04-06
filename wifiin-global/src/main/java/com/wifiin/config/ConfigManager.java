package com.wifiin.config;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.wifiin.common.CommonConstant;
import com.wifiin.common.GlobalObject;
import com.wifiin.exception.ConfigException;
import com.wifiin.util.CuratorFactory;
import com.wifiin.util.Help;
import com.wifiin.util.ShutdownHookUtil;
/**
 * 用zookeeper管理的配置工具。
 * 如果利用本工具向zookeeper存储的数据对象为自定义的复合对象，
 * 需要确保各个使用这些对象的应用拥有这些定义对象的类，且保持版本一致，否则不能完成从zookeeper读取数据的工作；
 * 如果不能保证这一点，请使用jdk自带集合类型代替复杂的自定义复合类型。
 * 如果zookeeper保存的数据已包含类信息，用本类读取数据时Class参数务必要传null，否则传相应的Class对象。
 * 如果没有注释的特别说明，所有数据都用kryo序列化和反序列化。
 * json和字符串类型单独实现了存储和获取逻辑，
 * 对于Date Instant Calendar等时间类型，填充值时一律使用当前时间毫秒数，不要传入时间对象
 * @see setDataOrCreateInJson(String,Object)， @see setDataOrCreateInJsonToGlobal(String,Object) @see getDataOrCreateFromJson(String,Class) 
 * @see setStringDataOrCreate(String,String), @see getString(String)
 * @author Running
 *
 */
public class ConfigManager{
    private static final Logger log=LoggerFactory.getLogger(ConfigManager.class);
    private static final String PATH_SEPERATOR="/";
    private static final String CONFIG_MANAGER_NAMESPACE="wifiin.config.manager.namespace";
    private static final String DEFAULT_CONFIG_MANAGER_NAMESPACE="wifiin";
    private static final String GLOBAL_NAMESPACE="global";
    private static final String CONFIG_MANAGER_CONNECT_STRING="wifiin.config.manager.connect";
    private static final String DEFAULT_CONFIG_MANAGER_CONNECT_STRING="127.0.0.1:2181";
    private static final String CONFIG_MANAGER_RETRY_INTERVAL_MS="wifiin.config.manager.retry.interval.ms";
    private static final int DEFAULT_CONFIG_MANAGER_RETRY_INTERVAL_MS=3000;
    private static ConfigManager instance=new ConfigManager();
    private CuratorFramework curator;
    private CuratorFramework globalCurator; 
    private volatile Map<String,Object> constants;
    private ConfigManager(){
        String namespace=CommonConstant.getStringConstant(CONFIG_MANAGER_NAMESPACE,DEFAULT_CONFIG_MANAGER_NAMESPACE);
        String connectString=CommonConstant.getStringConstant(CONFIG_MANAGER_CONNECT_STRING,DEFAULT_CONFIG_MANAGER_CONNECT_STRING);
        int retryIntervalMs=CommonConstant.getIntConstant(CONFIG_MANAGER_RETRY_INTERVAL_MS,DEFAULT_CONFIG_MANAGER_RETRY_INTERVAL_MS);
        curator=initCurator(namespace,connectString,retryIntervalMs);
        globalCurator=initCurator(GLOBAL_NAMESPACE,connectString,retryIntervalMs);
        populateConstants();
        addShutdownHook();
    }
    public ConfigManager(String namespace,String connectString,int retryIntervalMs){
        curator=initCurator(namespace,connectString,retryIntervalMs);
    }
    public CuratorFramework curator(){
        return curator;
    }
    /**
     * 初始化curator
     * @param namespace      应用标识
     * @param connectString  连接zookeeper的主机和端口号，如果有多个，就用英文逗号分隔，形如：ip1:port1,ip2:port2
     * @param retryIntervalMs 如果连不上zookeeper，重试连接的周期，只要jvm不停会一直重试下去
     */
    private CuratorFramework initCurator(String namespace,String connectString,int retryIntervalMs){
        return CuratorFactory.get(namespace,retryIntervalMs,connectString);
    }
    /**
     * 填充常量map
     */
    private void populateConstants(){
        Map<String,Object> map=Maps.newConcurrentMap();
        map.putAll(System.getenv());
        Properties props=System.getProperties();
        for(Map.Entry<Object,Object> entry:props.entrySet()){
            map.put((String)entry.getKey(),entry.getValue());
        }
        constants=map;
    }
    /**
     * 添加shutdownhook，jvm退出时关闭curator
     */
    private void addShutdownHook(){
        ShutdownHookUtil.addHook(()->{
            closeCurator(curator);
            closeCurator(globalCurator);
        });
    }
    /**
     * 关闭指定curator
     * @param curator
     */
    private void closeCurator(CuratorFramework curator){
        if(curator!=null){
            curator.close();
        }
    }
    /**
     * 返回ConfigManager
     * @return
     */
    public static ConfigManager getInstance(){
        return instance;
    }
    /**
     * 构造zookeeper路径
     * @param key
     * @return
     */
    private String generatePath(String key){
        return PATH_SEPERATOR+(key!=null?key:"");
    }
    /**
     * 监听zookeeper
     * @param key 要监听的key
     * @param curator 监听的客户端
     */
    private void watch(CuratorFramework curator,String key){
        watch(curator,key,null);
    }
    /**
     * 监听zookeepeer
     * @param curator 监听的客户端
     * @param key 要监听的key
     * @param runnable key的值发生变化时要执行的任务，可以传null
     */
    private void watch(CuratorFramework curator,String key,Consumer<String> watchConsumer){
        try{
            curator.getData().watched().inBackground((client,event)->{
                constants.remove(key);
                if(watchConsumer!=null){
                    watchConsumer.accept(key);
                }
            }).forPath(generatePath(key));
        }catch(Exception e){
            log.error("ConfigManager.watch:",e);
        }
    }
    /**
     * 监听zookeeper
     * @param key 要监听的key
     * @param runnable key的值发生变化时要执行的任务
     */
    public void watch(String key,Consumer<String> consumer){
        watch(curator,key,consumer);
    }
    /**
     * 删除指定key
     * @param key
     */
    public void delete(String key){
        delete(key,curator);
    }
    /**
     * 从全局对象删除指定key
     */
    public void deleteFromGlobal(String key) {
        delete(key,globalCurator);
    }
    /**
     * 从指定curator删除指定key
     * @param key
     * @param curator
     */
    private void delete(String key,CuratorFramework curator) {
        try{
            curator.delete().deletingChildrenIfNeeded().forPath(generatePath(key));
        }catch(Exception e){
            throw new ConfigException(e);
        }
    }
    /**
     * 用json格式储存数据,只能用getDataFromJson(String, Class)获取
     * @param key
     * @param value
     */
    public void setDataOrCreateInJson(String key,Object value){
        setDataOrCreateInJson(key,value,curator);
    }
    /**
     * 用json格式储存全局配置数据，只能用getDataFromJson(String, Class)获取
     * @param key
     * @param value
     */
    public void setDataOrCreateInJsonToGlobal(String key,Object value) {
        setDataOrCreateInJson(key,value,globalCurator);
    }
    /**
     * 向指定curator填充指定key-value
     * @param key
     * @param value
     * @param curator
     */
    private void setDataOrCreateInJson(String key,Object value,CuratorFramework curator){
        setDataOrCreate(key,value,curator,(v)->{
            try{
                return GlobalObject.getJsonMapper().writeValueAsBytes(v);
            }catch(JsonProcessingException e){
                throw new ConfigException(e);
            }
        });
    }
    /**
     * 得到格式是json的数据，只能获取setDataOrCreateJson*(String,Object)保存的数据
     * @param key
     * @param cls
     * @return
     */
    public <E> E getDataFromJson(String key,Class<E> cls) {
        E r=getDataFromJson(key,cls,curator);
        if(Help.isEmpty(r)){
            r=getDataFromJson(key,cls,globalCurator);
        }
        return r;
    }
    /**
     * 从指定curator得到指定key的数据，数据是json格式转换为cls的对象
     * @param key
     * @param cls
     * @param curator
     * @return
     */
    private <E> E getDataFromJson(String key,Class<E> cls,CuratorFramework curator) {
        return getDataByCurator(key,(data)->{
            try{
                return GlobalObject.getJsonMapper().readValue(data,cls);
            }catch(IOException e){
                throw new ConfigException(e);
            }
        },curator);
    }
    /**
     * 将value保存到zookeeper，键是key，如果key不存在就创建一个，如果已存在就覆盖已存在的值。写入的结果不包含类信息。
     * 数据将用kryo序列化
     * @param key
     * @param value
     */
    public void setDataOrCreate(String key,Object value) {
        setDataOrCreate(key,value,curator);
    }
    /**
     * 将value保存到zookeeper,namespace是global，键是key，如果key不存在就创建一个，如果已存在就覆盖已存在的值。写入的结果不包含类信息
     * @see setDataOrCreate(String,Object)
     * @param key
     * @param value
     */
    public void setDataOrCreateToGlobal(String key,Object value) {
        setDataOrCreate(key,value,globalCurator);
    }
    /**
     * 向指定curator填充指定key-value
     * @param key
     * @param value
     * @param curator
     */
    private void setDataOrCreate(String key,Object value,CuratorFramework curator){
        setDataOrCreate(key,value,curator,GlobalObject.getFSTConfiguration()::asByteArray);
    }
    /**
     * 将value保存到zookeeper，键是key，如果key不存在就创建一个，如果已存在就覆盖已存在的值
     * @param key
     * @param value 
     * @param curator 要保存到的curator对象，本类实例维护两个curator对象，一个是全局对象一个不是
     * @param writer
     */
    private void setDataOrCreate(String key,Object value, CuratorFramework curator,Function<Object,byte[]> writer){
        String path=generatePath(key);
        try{
            if(curator.checkExists().forPath(path)!=null){
                curator.setData().forPath(path,writer.apply(value));
            }else{
                curator.create().creatingParentsIfNeeded().forPath(path,writer.apply(value));
            }
        }catch(Exception e){
            throw new ConfigException(e);
        }
    }
    /**
     * 用curator从zookeeper得到指定key的数据，数据类型是cls,如果key变化了执行watchConsumer
     * @param key
     * @param cls
     * @param watcherConsumer
     * @return
     */
    private <E> E getDataByCurator(String key,Class<E> cls,Consumer<E> watcherConsumer){
        E e=getDataByCurator(key,cls,watcherConsumer,curator);
        if(Help.isEmpty(e)){
            e=getDataByCurator(key,cls,watcherConsumer,globalCurator);//此处不能形成递归，否则在值实际不存在时会造成无穷递归
        }
        return e;
    }
    /**
     * 从指定curator对象获取数据
     * @param key
     * @param cls
     * @param curator
     * @return
     */
    private <E> E getDataByCurator(String key,Class<E> cls,CuratorFramework curator) {
        return getDataByCurator(key,cls,null,curator);
    }
    /**
     * 从指定curator得到key的值，值类型是cls，值变化了执行watchConsumer
     * @param key
     * @param cls
     * @param watchConsumer accept的参数是新的值
     * @param curator
     * @return
     */
    private <E> E getDataByCurator(String key,Class<E> cls,Consumer<E> watchConsumer,CuratorFramework curator){
        return getDataByCurator(key,(data)->{
            return convertBytes(data,cls);
        },watchConsumer,curator);
    }
    /**
     * 从指定curator对象获取数据，用指定的dataConverter将得到的数据转化为要返回的对象
     * @param key
     * @param dataConverter
     * @param curator
     * @return
     */
    private <E> E getDataByCurator(String key,Function<byte[],E> dataConverter,CuratorFramework curator){
        return getDataByCurator(key,dataConverter,null,curator);
    }
    /**
     * 从指定curator获取数据，用指定dataConverter将得到的数据转化为要返回的对象，并监听指定key的变化，key发生变化了执行consumer
     * @param key
     * @param dataConverter
     * @param consumer
     * @param curator
     * @return
     */
    private <E> E getDataByCurator(String key,Function<byte[],E> dataConverter,Consumer<E> consumer,CuratorFramework curator){
        String path=generatePath(key);
        byte[] data=null;
        if(curator!=null){
            try{
                data=curator.getData().forPath(path);
                if(consumer==null){
                    watch(curator,key,null);
                }else{
                    watch(curator,key,(k)->{
                        E e=getDataByCurator(key,dataConverter, consumer,curator);
                        consumer.accept(e);
                    });
                }
            }catch(NoNodeException e){
                log.warn("ConfigManager.getDataByCurator:"+e.getMessage());
                return null;
            }catch(Exception e){
                throw new ConfigException(e);
            }
        }else{
            return null;
        }
        return dataConverter.apply(data);
    }
    /**
     * 把data转成指定cls类型的对象
     * @param data kryo序列化字节数组
     * @param cls
     * @return
     */
    @SuppressWarnings("unchecked")
    private <E> E convertBytes(byte[] data,Class<E> cls){
        if(Help.isEmpty(data)){
            return null;
        }
        return (E)GlobalObject.getFSTConfiguration().asObject(data);
    }
    /**
     * 将字符串存储到配置管理
     */
    public void setStringDataOrCreate(String key,String value){
        setStringDataOrCreate(key,value,curator);
    }
    /**
     * 将字符串作为全局配置存储到配置管理
     * @param key
     * @param value
     */
    public void setStringDataOrCreateToGlobal(String key,String value){
        setStringDataOrCreate(key,value,globalCurator);
    }
    /**
     * 将字符串key-value填充到指定curator
     * @param key
     * @param value
     * @param curator
     */
    private void setStringDataOrCreate(String key,String value,CuratorFramework curator){
        setDataOrCreate(key,value,curator,(data)->{
            try{
                return ((String)data).getBytes(CommonConstant.DEFAULT_CHARSET_NAME);
            }catch(UnsupportedEncodingException e){
                throw new ConfigException(e);
            }
        });
    }
    /**
     * 得到指定key的值
     * @param key
     * @param defaultValue
     * @return
     */
    public String getString(String key,String defaultValue){
        return getStringAndWatch(key,defaultValue,(Consumer<String>)null);
    }
    /**
     * 得到指定key的值，并在key的值变化时指定watchConsumer
     * @param key
     * @param defaultValue
     * @param watchConsumer
     * @return
     */
    public String getStringAndWatch(String key,String defaultValue,Consumer<String> watchConsumer){
        return getValue(key,String.class,defaultValue,(v)->{
            try{
                return new String(v,CommonConstant.DEFAULT_CHARSET_NAME);
            }catch(UnsupportedEncodingException e){
                throw new ConfigException(e);
            }
        },(v)->{
            return v;
        },watchConsumer);
    }
    /**
     * 得到指定key的值，值类型是String，默认值是空字符串
     * @param key
     * @return
     */
    public String getString(String key){
        return getString(key,"");
    }
    /**
     * 得到指定key的值，值类型是String，默认值是空字符串，在key变化时执行wathConsumer
     * @param key
     * @param watchConsumer accept的参数是新的字符串
     * @return
     */
    public String getStringAndWatch(String key,Consumer<String> watchConsumer){
        return getStringAndWatch(key,"",watchConsumer);
    }
    /**
     * 得到默认值是false的boolean
     */
    public boolean getBoolean(String key){
        return getBoolean(key,false);
    }
    /**
     * 得到key对应的boolean
     * @param key
     * @param defaultValue
     * @return
     */
    public boolean getBoolean(String key,boolean defaultValue){
        return getBooleanAndWatch(key,defaultValue,null);
    }
    /**
     * 得到key对应的boolean,并在key的值变化时执行watchConsumer
     * @param key
     * @param defaultValue
     * @param watchConsumer，accept接受新的值
     * @return
     */
    public boolean getBooleanAndWatch(String key,boolean defaultValue,Consumer<Boolean> watchConsumer){
        return getValue(key,Boolean.class,defaultValue,(v)->{
            return convertBytes(v,Boolean.class);
        },(v)->{
           return Boolean.parseBoolean((String)v); 
        },watchConsumer);
    }
    /**
     * 得到key对应的boolean,并在key的值变化时执行watchConsumer，默认值是false
     * @param key
     * @param watchConsumer
     * @return
     */
    public boolean getBooleanAndWatch(String key,Consumer<Boolean> watchConsumer){
        return getBooleanAndWatch(key,false,watchConsumer);
    }
    /**
     * 得到指定key的值，值类型是int
     * @param key
     * @param defaultValue
     * @return
     */
    public int getInt(String key,int defaultValue){
        return getIntAndWatch(key,defaultValue,null);
    }
    /**
     * 得到指定key的值，值类型是int，默认值是0
     * @param key
     * @return
     */
    public int getInt(String key){
        return getInt(key,0);
    }
    /**
     * 得到指定key的int值，默认是0，如果值变化了执行watchConsumer
     * @param key
     * @param watchConsumer
     * @return
     */
    public int getIntAndWatch(String key,Consumer<Integer> watchConsumer){
        return getIntAndWatch(key,0,watchConsumer);
    }
    /**
     * 得到指定key的值，值类型是int，默认值是defaultValue,如果值变化了执行watchConsumer
     * @param key
     * @param defaultValue
     * @param watchConsumer
     * @return
     */
    public int getIntAndWatch(String key,int defaultValue,Consumer<Integer> watchConsumer){
        return getValue(key,Integer.class,defaultValue,(v)->{
            return convertBytes(v,Integer.class);
        },(v)->{
            return Integer.valueOf((String)v);
        },watchConsumer);
    }
    /**
     * 得到指定key的值，值类型是long
     * @param key
     * @param defaultValue
     * @return
     */
    public long getLong(String key,long defaultValue){
        return getLongAndWatch(key,defaultValue,null);
    }
    /**
     * 得到指定key的值，值类型是long，默认值是0
     * @param key
     * @return
     */
    public long getLong(String key){
        return getLong(key,0);
    }
    /**
     * 得到指定key的long值，默认是defaultValue，如果值变化了执行watchConsumer
     * @param key
     * @param defaultValue
     * @param watchConsumer
     * @return
     */
    public long getLongAndWatch(String key,long defaultValue,Consumer<Long> watchConsumer){
        return getValue(key,Long.class,defaultValue,(v)->{
            return (Long)convertBytes(v,Long.class);
        },(v)->{
            return Long.valueOf(v);
        },watchConsumer);
    }
    /**
     * 得到指定key的long值，默认是0，如果值变化了执行watchConsumer
     * @param key
     * @param watchConsumer
     * @return
     */
    public long getLongAndWatch(String key,Consumer<Long> watchConsumer){
        return getLongAndWatch(key,0,watchConsumer);
    }
    /**
     * 得到指定key的值，值类型是double
     * @param key
     * @param defaultValue
     * @return
     */
    public double getDouble(String key,double defaultValue){
        return getDouble(key,defaultValue,null);
    }
    /**
     * 得到指定key的值，值类型是double，默认值是0
     * @param key
     * @return
     */
    public double getDouble(String key){
        return getDouble(key,0);
    }
    /**
     * 得到指定key的值，值类型是double,默认是defaultValue,值变化了执行watchConsumer
     * @param key
     * @param defaultValue
     * @param watchConsumer accept的参数是新的double
     * @return
     */
    public double getDouble(String key,double defaultValue,Consumer<Double> watchConsumer){
        return getValue(key,Double.class,defaultValue,(v)->{
            return convertBytes(v,Double.class);
        },(v)->{
            return Double.valueOf(v);
        },watchConsumer);
    }
    /**
     * 得到指定key的值，值类型是double,默认是0，值变化了执行watchConsumer
     * @param key
     * @param watchConsumer accept的参数是新的double
     * @return
     */
    public double getDouble(String key,Consumer<Double> watchConsumer){
        return getDouble(key,0,watchConsumer);
    }
    /**
     * 得到指定key的值，值类型是java.util.Date
     * @param key
     * @param defaultValue
     * @return
     */
    public Date getDate(String key,Date defaultValue){
        return getDate(key,defaultValue,null);
    }
    /**
     * 得到指定key的值，值类型是java.util.Date，默认值是new Date()
     * @param key
     * @return
     */
    public Date getDate(String key){
        return getDate(key,new Date());
    }
    /**
     * 得到指定key的值，值类型是java.util.Date,默认是defaultValue,值变化了执行watchConsumer,
     * @param key
     * @param defaultValue
     * @param watchConsumer accept的参数是新的值
     * @return
     */
    public Date getDate(String key,Date defaultValue,Consumer<Date> watchConsumer){
        return getValue(key,Date.class,defaultValue,(v)->{
            return new Date(convertBytes(v,Long.class));
        },(v)->{
            return new Date(Long.valueOf(v));
        },watchConsumer);
    }
    /**
     * 得到指定key的值，值类型是java.util.Date，默认是new Date()，值变化了执行watchConsumer
     * @param key
     * @param watchConsumer accept的参数是新的值
     * @return
     */
    public Date getDate(String key,Consumer<Date> watchConsumer){
        return getDate(key,new Date(),watchConsumer);
    }
    /**
     * 得到指定key的值，值类型是Calendar
     * @param key
     * @param defaultValue
     * @return
     */
    public Calendar getCalendar(String key,Calendar defaultValue){
        return getCalendar(key,defaultValue,null);
    }
    /**
     * 得到指定key的值，值的类型是Calendar,默认值是当前时间
     * @param key
     * @return
     */
    public Calendar getCalendar(String key){
        return getCalendar(key,Calendar.getInstance());
    }
    /**
     * 得到指定key的值，值类型是Calendar，默认值是defaultValue,值变化了执行watchConsumer
     * @param key
     * @param defaultValue
     * @param watchConsumer accept参数是新的值
     * @return
     */
    public Calendar getCalendar(String key,Calendar defaultValue,Consumer<Calendar> watchConsumer){
        return getValue(key,Calendar.class,defaultValue,(v)->{
            Calendar calendar=Calendar.getInstance();
            calendar.setTimeInMillis(convertBytes(v,Long.class));
            return calendar;
        },(v)->{
            Calendar calendar=Calendar.getInstance();
            calendar.setTimeInMillis(Long.valueOf((String)v));
            return calendar;
        },watchConsumer);
    }
    /**
     * 得到指定key的值，默认是当前时间，值变化了执行watchConsumer
     * @param key
     * @param watchConsumer
     * @return
     */
    public Calendar getCalendar(String key,Consumer<Calendar> watchConsumer){
        return getCalendar(key,Calendar.getInstance(),watchConsumer);
    }
    /**
     * 得到指定key的值，值类型是Instant
     * @param key
     * @param defaultValue
     * @return
     */
    public Instant getInstant(String key, Instant defaultValue){
        return getInstant(key,defaultValue,null);
    }
    /**
     * 得到指定key的值，值类型是Instant，默认值是Instant.now()
     * @param key
     * @return
     */
    public Instant getInstant(String key){
        return getInstant(key,Instant.now());
    }
    /**
     * 得到指定key的值，值类型是Instant，默认值是defaultValue,值变化了执行watchConsumer
     * @param key
     * @param defaultValue
     * @param watchConsumer accept参数是新的值
     * @return
     */
    public Instant getInstant(String key,Instant defaultValue,Consumer<Instant> watchConsumer){
        return getValue(key,Instant.class,defaultValue,(v)->{
            return Instant.ofEpochMilli(convertBytes(v,long.class));
        },(v)->{
            try{
                return Instant.parse((String)v);
            }catch(Exception e){
                try{
                    return Instant.ofEpochMilli(Long.parseLong(v));
                }catch(Exception e1){
                    ConfigException ce=new ConfigException(e);
                    ce.addSuppressed(e1);
                    throw ce;
                }
            }
        },watchConsumer);
    }
    /**
     * 得到指定key的值，值类型是Instant，默认是Instant.now()，值变化了执行watchConsumer
     * @param key
     * @param watchConsumer
     * @return
     */
    public Instant getInstant(String key,Consumer<Instant> watchConsumer){
        return getInstant(key,Instant.now(),watchConsumer);
    }
    /**
     * 得到指定key的值，值类型是枚举型
     * @param key
     * @param defaultValue 
     * @param cls
     * @return
     */
    public <E extends Enum<E> > E getEnum(String key,E defaultValue,Class<E> cls){
        return getEnum(key,defaultValue,cls,null);
    }
    /**
     * 得到指定key的值，值类型是枚举型，默认值是defaultValue,值变化了执行watchConsumer
     * @param key
     * @param defaultValue
     * @param cls
     * @param watchConsumer accept的参数是新的枚举值
     * @return
     */
    public <E extends Enum<E>> E getEnum(String key,E defaultValue,Class<E> cls,Consumer<E> watchConsumer){
        return getValue(key,cls,defaultValue,(v)->{
            return Enum.valueOf(cls,convertBytes(v,String.class));
        },(v)->{
            return Enum.valueOf(cls,(String)v);
        },watchConsumer);
    }
    
    /**
     * 得到指定key的值，值类型是BigInteger
     * @param key
     * @param defaultValue
     * @return
     */
    public BigInteger getBigInteger(String key,BigInteger defaultValue){
        return getBigInteger(key,defaultValue,null);
    }
    /**
     * 得到指定key的值，值类型是BigInteger，默认值是BigInteger.ZERO
     * @param key
     * @return
     */
    public BigInteger getBigInteger(String key){
        return getBigInteger(key,BigInteger.ZERO);
    }
    /**
     * 得到指定key的值，值类型是BigInteger,默认值是new BigInteger(defaultValue)
     * @param key
     * @param defaultValue
     * @return
     */
    public BigInteger getBigInteger(String key,String defaultValue){
        return getBigInteger(key,new BigInteger(defaultValue));
    }
    /**
     * 得到指定key的值，值类型是BigInteger,默认值是new BigInteger(defaultValue)，值变化了执行watchConsumer
     * @param key
     * @param defaultValue
     * @param watchConsumer accept参数是新的值
     * @return
     */
    public BigInteger getBigInteger(String key,String defaultValue,Consumer<BigInteger> watchConsumer){
        return getBigInteger(key,new BigInteger(defaultValue),watchConsumer);
    }
    /**
     * 得到指定key的值，值类型是BigInteger,默认是BigInteger.ZERO,值变化了执行watchConsumer
     * @param key
     * @param watchConsumer accept参数是新的值
     * @return
     */
    public BigInteger getBigInteger(String key,Consumer<BigInteger> watchConsumer){
        return getBigInteger(key,BigInteger.ZERO,watchConsumer);
    }
    /**
     * 得到指定key的值，值类型是BigInteger，默认是defaultValue,值变化了执行watchConsumer
     * @param key
     * @param defaultValue
     * @param watchConsumer accept的参数是新的值
     * @return
     */
    public BigInteger getBigInteger(String key,BigInteger defaultValue,Consumer<BigInteger> watchConsumer){
        return getValue(key,BigInteger.class,defaultValue,(v)->{
            return convertBytes(v,BigInteger.class);
        },(v)->{
            return new BigInteger(v);
        },watchConsumer);
    }
    
    /**
     * 得到指定key的值，值类型是BigDecimal
     * @param key
     * @param defaultValue
     * @return
     */
    public BigDecimal getBigDecimal(String key,BigDecimal defaultValue){
        return getBigDecimal(key,defaultValue,null);
    }
    /**
     * 得到指定key的值，值类型是BigDecimal，默认值是BigDecimal.ZERO
     * @param key
     * @return
     */
    public BigDecimal getBigDecimal(String key){
        return getBigDecimal(key,BigDecimal.ZERO);
    }
    /**
     * 得到指定key的值，值类型是BigDecimal,默认值是new BigDecimal(defaultValue)
     * @param key
     * @param defaultValue
     * @return
     */
    public BigDecimal getBigDecimal(String key,String defaultValue){
        return getBigDecimal(key,new BigDecimal(defaultValue));
    }
    /**
     * 得到指定key的值，值类型是BigDecimal,默认值是new BigDecimal(defaultValue)，值变化了执行watchConsumer
     * @param key
     * @param defaultValue
     * @param watchConsumer accept参数是新的值
     * @return
     */
    public BigDecimal getBigDecimal(String key,String defaultValue,Consumer<BigDecimal> watchConsumer){
        return getBigDecimal(key,new BigDecimal(defaultValue),watchConsumer);
    }
    /**
     * 得到指定key的值，值类型是BigDecimal,默认是BigDecimal.ZERO,值变化了执行watchConsumer
     * @param key
     * @param watchConsumer accept参数是新的值
     * @return
     */
    public BigDecimal getBigDecimal(String key,Consumer<BigDecimal> watchConsumer){
        return getBigDecimal(key,BigDecimal.ZERO,watchConsumer);
    }
    /**
     * 得到指定key的值，值类型是BigDecimal，默认是defaultValue,值变化了执行watchConsumer
     * @param key
     * @param defaultValue
     * @param watchConsumer accept的参数是新的值
     * @return
     */
    public BigDecimal getBigDecimal(String key,BigDecimal defaultValue,Consumer<BigDecimal> watchConsumer){
        return getValue(key,BigDecimal.class,defaultValue,(v)->{
            return convertBytes(v,BigDecimal.class);
        },(v)->{
            return new BigDecimal(v);
        },watchConsumer);
    }
    /**
     * 得到指定key的值，值类型是HashMap。
     * 默认值是空Map
     * @param key
     * @return
     */
    public <K,V> HashMap<K,V> getHashMap(String key){
        return getHashMap(key,null);
    }
    /**
     * 得到指定key的值，值类型是HashMap，值变化了执行watchConsumer
     * @param key
     * @param watchConsumer accept的参数是新的值
     * @return
     */
    @SuppressWarnings({"unchecked","rawtypes"})
    public <K,V> HashMap<K,V> getHashMap(String key,Consumer<HashMap> watchConsumer){
        return (HashMap<K,V>)getObject(key,HashMap.class,Maps.newHashMap(),watchConsumer);
    }
    /**
     * 得到指定key的值，值类型是HashMap。
     * 默认值是空HashMap
     * @param key
     * @return
     */
    @SuppressWarnings("unchecked")
    public <K,V> HashMap<K,V> mergeHashMap(String... keys){
        return (HashMap<K,V>)mergeMap(HashMap.class,HashMap.class,keys);
    }
    /**
     * 得到指定key的值，值类型是HashMap，默认是空HashMap，值变化了重新执行合并
     * @param watchConsumer accept的参数是新的值
     * @return
     */
    public <K,V> HashMap<K,V> mergeHashMap(Consumer<HashMap<K,V>> watchConsumer, String... keys){
        return (HashMap<K,V>)mergeMap(HashMap.class,HashMap.class,watchConsumer,keys);
    }
    /**
     * 得到指定key的值，值类型是ConcurrentHashMap。
     * 默认值是空ConcurrentHashMap
     * @param key
     * @return
     */
    public <K,V> ConcurrentHashMap<K,V> getConcurrentHashMap(String key){
        return getConcurrentHashMap(key,null);
    }
    /**
     * 得到指定key的值，值类型是ConcurrentHashMap，默认是空的ConcurrentHashMap，值变化了执行watchConsumer
     * @param key
     * @param watchConsumer accept参数是新的ConcurrentHashMap
     * @return
     */
    @SuppressWarnings({"unchecked","rawtypes"})
    public <K,V> ConcurrentHashMap<K,V> getConcurrentHashMap(String key,Consumer<ConcurrentHashMap> watchConsumer){
        return (ConcurrentHashMap<K,V>)getObject(key,ConcurrentHashMap.class,new ConcurrentHashMap(),watchConsumer);
    }
    /**
     * 得到指定key的值，值类型是ConcurrentHashMap。
     * 默认值是空ConcurrentHashMap
     * @param key
     * @return
     */
    public <K,V> ConcurrentHashMap<K,V> mergeConcurrentHashMap(String... keys){
        return mergeConcurrentHashMap(null,keys);
    }
    /**
     * 得到指定key的值，值类型是ConcurrentHashMap,默认是空ConcurrentHashMap,值变化了执行watchConsumer
     * @param watchConsumer accept的参数是新的ConcurrentHashMap
     * @param keys
     * @return
     */
    public <K,V> ConcurrentHashMap<K,V> mergeConcurrentHashMap(Consumer<ConcurrentHashMap<K,V>> watchConsumer,String... keys){
        return (ConcurrentHashMap<K,V>)mergeMap(ConcurrentHashMap.class,ConcurrentHashMap.class,watchConsumer,keys);
    }
    /**
     * 得到指定key的值，值类型是TreeMap。
     * 默认值是空TreeMap
     * @param key
     * @return
     */
    public <K,V> TreeMap<K,V> getTreeMap(String key){
        return getTreeMap(key,null);
    }
    /**
     * 得到指定key的值，值类型是TreeMap，默认是空TreeMap，值变化了执行watchConsumer
     * @param key
     * @param watchConsumer accept的参数是新的TreeMap
     * @return
     */
    @SuppressWarnings({"unchecked","rawtypes"})
    public <K,V> TreeMap<K,V> getTreeMap(String key, Consumer<TreeMap> watchConsumer){
        return (TreeMap<K,V>)getObject(key,TreeMap.class,Maps.newTreeMap(),watchConsumer);
    }
    /**
     * 得到指定key的值，值类型是TreeMap。
     * 默认值是空TreeMap
     * @param key
     * @return
     */
    public <K,V> TreeMap<K,V> mergeTreeMap(String... keys){
        return mergeTreeMap(null,keys);
    }
    /**
     * 得到指定key的值，值类型是TreeMap，默认是空TreeMap
     * @param watchConsumer 任意一个key的值变化了会重新合并全部key的值，accept的参数是新的TreeMap
     * @param keys
     * @return
     */
    public <K,V> TreeMap<K,V> mergeTreeMap(Consumer<TreeMap<K,V>> watchConsumer,String... keys){
        return (TreeMap<K,V>)mergeMap(TreeMap.class,TreeMap.class,watchConsumer,keys);
    }
    /**
     * 得到指定key的值，值类型是Properties。
     * 默认值是System.getProperties()
     * @param key
     * @return
     */
    public Properties getProperties(String key){
        return getObject(key,Properties.class,System.getProperties());
    }
    /**
     * 得到指定key的值，值类型是Propeerties，默认是System.getProperties()，key的值变化了执行watchConsumer，accept的参数是新的Properties
     * @param key 
     * @param watchConsumer
     * @return
     */
    public Properties getProperties(String key,Consumer<Properties> watchConsumer){
        return getObject(key,Properties.class,System.getProperties(),watchConsumer);
    }
    /**
     * 合并指定key的值，值类型是Properties。
     * 默认值是System.getProperties()
     * @param key
     * @return
     */
    public Properties mergeProperties(String... keys){
        return mergeProperties(null,keys);
    }
    /**
     * 合并指定key的值，值类型
     * @param watchConsumer
     * @param keys
     * @return
     */
    public Properties mergeProperties(Consumer<Properties> watchConsumer,String... keys){
        return mergeMap(Properties.class,Properties.class,watchConsumer,keys);
    }
    /**
     * 合并指定key的集合元素，将合并后的集合作为第一个key的值，后面key的元素会覆盖前面的
     * @param map 
     * @param originMapClass 原始数据类型
     * @param keys 要合并的key
     * @return
     */
    private <K,V> Map<K,V> mergeMap(Class<?> destMapClass,Class<?> originMapClass,String... keys){
        return mergeMap(destMapClass,originMapClass,null,keys);
    }
    @SuppressWarnings("unchecked")
    private <K,V,M extends Map<K,V>> M mergeMap(Class<?> destMapClass,Class<?> originMapClass,Consumer<M> watchConsumer,String... keys){
        return (M)constants.computeIfAbsent(keys[0],(v)->{
            Map<K,V> map=null;
            try{
                map=(Map<K,V>)destMapClass.newInstance();
                for(int i=0,l=keys.length;i<l;i++){
                    String key=keys[i];
                    try{
                        Consumer<Map<K,V>> consumer=(Map<K,V> newMap)->{
                            M mergedNewMap=mergeMap(destMapClass,originMapClass,watchConsumer,keys);
                            if(watchConsumer!=null){
                                watchConsumer.accept(mergedNewMap);
                            }
                        };
                        map.putAll(Help.convert(getDataByCurator(key,(Class<Map<K,V>>)originMapClass,consumer,globalCurator),Collections.emptyMap()));
                        map.putAll(Help.convert(getDataByCurator(key,(Class<Map<K,V>>)originMapClass,consumer,curator),Collections.emptyMap()));
                    }catch(Exception e){
                        throw new ConfigException(e); 
                    }
                }
                return map;
            }catch(InstantiationException | IllegalAccessException e1){
                throw new ConfigException(e1);
            }
        });
    }
    /**
     * 得到指定key的值，值类型是ArrayList。
     * 默认值是空ArrayList
     * @param key
     * @return
     */
    public <T> ArrayList<T> getArrayList(String key){
        return getArrayList(key,null);
    }
    /**
     * 得到指定key的值，值类型是ArrayList,值变化了执行watchConsumer，accept的参数是新的ArrayList
     * @param key
     * @param watchConsumer
     * @return
     */
    @SuppressWarnings({"unchecked","rawtypes"})
    public <T> ArrayList<T> getArrayList(String key,Consumer<ArrayList> watchConsumer){
        return getObject(key,ArrayList.class,Lists.newArrayList(),watchConsumer);
    }
    /**
     * 合并指定key的集合元素，将合并后的集合作为第一个key的值，后面key的元素会覆盖前面的。
     * 返回值类型是@see java.util.ArrayList
     * @param keys
     * @return
     */
    public <V> ArrayList<V> mergeArrayList(String... keys){
        return mergeArrayList(null,keys);
    }
    /**
     * 合并指定key的集合元素，将合并后的集合作为第一个key的值，后面key的元素会覆盖前面的。返回的值类型是@see java.util.ArrayList。
     * 任意key的值变化了会执行watchConsumer，accept的参数是新的合并后的ArrayList
     * @param watchConsumer
     * @param keys
     * @return
     */
    public <V> ArrayList<V> mergeArrayList(Consumer<ArrayList<V>> watchConsumer,String... keys){
        return (ArrayList<V>)mergeCollection(ArrayList.class,ArrayList.class,watchConsumer,keys);
    }
    /**
     * 得到指定key的值，值类型是ArrayList。
     * 默认值是空ArrayList
     * @param key
     * @return
     */
    public <T> LinkedList<T> getLinkedList(String key){
        return getLinkedList(key,null);
    }
    /**
     * 得到指定key的值，值类型是LinkedList，值变化了执行watchConsumer，accept的参数是新的LinkedList
     * @param key
     * @param watchConsumer
     * @return
     */
    @SuppressWarnings({"unchecked","rawtypes"})
    public <T> LinkedList<T> getLinkedList(String key,Consumer<LinkedList> watchConsumer){
        return getObject(key,LinkedList.class,Lists.newLinkedList(),watchConsumer);
    }
    /**
     * 合并指定key的集合元素，将合并后的集合作为第一个key的值，前面key的元素会覆盖后面的。
     * 返回值类型是@see java.util.LinkedList
     * @param keys
     * @return
     */
    public <V> LinkedList<V> mergeLinkedList(String... keys){
        return mergeLinkedList(null,keys);
    }
    /**
     * 合并指定key的集合元素，合并后的集合作为第一个key的值，后面的key元素会覆盖前面的
     * @param watchConsumer
     * @param keys
     * @return
     */
    @SuppressWarnings({"unchecked","rawtypes"})
    public <V> LinkedList<V> mergeLinkedList(Consumer<LinkedList> watchConsumer,String... keys){
        return mergeCollection(LinkedList.class,LinkedList.class,watchConsumer,keys);
    }
    /**
     * 得到指定key的值，值类型是HashSet。
     * 默认值是空HashSet
     * @param key
     * @return
     */
    public <T> HashSet<T> getHashSet(String key){
        return getHashSet(key,null);
    }
    /**
     * 得到指定key的值，值类型是HashSet，值变化了执行watchConsumer，accept的参数是新的HashSet
     * @param key
     * @param watchConsumer
     * @return
     */
    @SuppressWarnings({"unchecked","rawtypes"})
    public <T> HashSet<T> getHashSet(String key,Consumer<HashSet> watchConsumer){
        return (HashSet<T>)getObject(key,HashSet.class,Sets.newHashSet(),watchConsumer);
    }
    /**
     * 合并指定key的集合元素，将合并后的集合作为第一个key的值，后面key的元素会覆盖前面的。
     * 返回值类型是@see java.util.HashSet
     * @param keys
     * @return
     */
    public <V> HashSet<V> mergeHashSet(String... keys){
        return mergeHashSet(null,keys);
    }
    /**
     * 指定key的值合并为一个存到keys[0]，值类型是HashSet，后面的值会覆盖前面的，任意一个key变化了的执行watchConsumer，accept的参数是新的合并后的HashSet
     * @param watchConsumer
     * @param keys
     * @return
     */
    @SuppressWarnings({"unchecked","rawtypes"})
    public <V> HashSet<V> mergeHashSet(Consumer<HashSet> watchConsumer,String... keys){
        return (HashSet<V>)mergeCollection(HashSet.class,HashSet.class,watchConsumer,keys);
    }
    /**
     * 得到指定key的值，值类型是TreeSet。
     * 默认值是空TreeSet
     * @param key
     * @return
     */
    public <T> TreeSet<T> getTreeSet(String key){
        return getTreeSet(key,null);
    }
    /**
     * 得到指定key的值，值类型是TreeSet，值变化了执行watchConsumer，accept的参数是新的TreeSet
     * @param key
     * @param watchConsumer
     * @return
     */
    @SuppressWarnings({"unchecked","rawtypes"})
    public <T> TreeSet<T> getTreeSet(String key,Consumer<TreeSet> watchConsumer){
        return getObject(key,TreeSet.class,Sets.newTreeSet(),watchConsumer);
    }
    /**
     * 合并指定key的集合元素，将合并后的集合作为第一个key的值，后面key的元素会覆盖前面的。
     * 返回值类型是@see java.util.TreeSet
     * @param keys
     * @return
     */
    public <V> TreeSet<V> mergeTreeSet(String... keys){
        return mergeTreeSet(null,keys);
    }
    /**
     * 合并指定key的集合元素，将合并后的集合作为第一个key的值，后面key的元素会覆盖前面的。任意一个key变化了执行watchConsumer，accept的元素是新的合并后的TreeSet
     * @param watchConsumer
     * @param keys
     * @return
     */
    @SuppressWarnings({"unchecked","rawtypes"})
    public <V> TreeSet<V> mergeTreeSet(Consumer<TreeSet> watchConsumer,String... keys){
        return (TreeSet<V>)mergeCollection(TreeSet.class,TreeSet.class,watchConsumer,keys);
    }
    /**
     * 合并指定key的集合元素，将合并后的集合作为第一个key的值，后面key的元素会覆盖前面的
     * @param collection 
     * @param originCollectionClass 原始数据类型
     * @param keys 要合并的key
     * @return
     */
    public <V,C extends Collection<V>> C mergeCollection(Class<C> destCollectionClass,Class<C> originCollectionClass,String... keys){
        return mergeCollection(destCollectionClass,originCollectionClass,null,keys);
    }
    /**
     * 合并指定keys的值，合并结果存在keys[0]，任意一个key的值变化了执行watchConsuemr，accept的参数是新的合并后的Collection
     * @param destCollectionClass   合并后的集合类型
     * @param originCollectionClass 合并前各个key的集合类型
     * @param watchConsumer
     * @param keys
     * @return
     */
    @SuppressWarnings("unchecked")
    public <V,C extends Collection<V>> C mergeCollection(Class<?> destCollectionClass,Class<?> originCollectionClass,Consumer<C> watchConsumer,String... keys){
        return (C)constants.computeIfAbsent(keys[0],(v)->{
            C collection;
            try{
                collection=(C)destCollectionClass.newInstance();
                for(int i=0,l=keys.length;i<l;i++){
                    String key=keys[i];
                    try{
                        collection.addAll(Help.convert(getDataByCurator(key,(Class<C>)originCollectionClass,watchConsumer,globalCurator),Collections.emptyList()));
                        collection.addAll(Help.convert(getDataByCurator(key,(Class<C>)originCollectionClass,watchConsumer,curator),Collections.emptySet()));
                    }catch(Exception e){
                        throw new ConfigException(e); 
                    }
                }
                return collection;
            }catch(InstantiationException | IllegalAccessException e1){
                throw new ConfigException(e1);
            }
        });
    }
    /**
     * 得到指定key的对象，这个对象就是需要的常量值
     * @param key 
     * @param cls 返回对象的类型
     * @param defaultValue 默认值
     * @return
     */
    public <E> E getObject(String key,Class<E> cls,E defaultValue){
        return getObject(key,cls,defaultValue,null);
    }
    /**
     * 得到指定key的对象，这个对象就是需要的常量值
     * @param key
     * @param cls
     * @param defaultValue
     * @param watchConsumer 值变化了执行watchConsumer，accept的参数是新的值
     * @return
     */
    public <E> E getObject(String key,Class<E> cls,E defaultValue,Consumer<E> watchConsumer){
        return getValue(key,cls,defaultValue,(v)->{
            return convertBytes(v,cls);
        },(v)->{
            try{
                return GlobalObject.getJsonMapper().readValue(v,cls);
            }catch(IOException e){
                throw new ConfigException(e);
            }
        },watchConsumer);
    }
    /**
     * 得到指定key的值
     * @param key
     * @param cls 值的类型
     * @param defaultvalue 默认值
     * @param bytesFn 如果从常量Map得到byte[]就用这个对象转化成指定类型的对象
     * @param stringFn 如果从常量Map得到String就用这个对象转化成指定类型的对象
     * @param last 对数据做最后的处理，可以是null
     * @return 需要的值，除非defaultValue是null，否则一定会返回一个值
     */
    public <E> E getValue(String key,Class<E> cls,E defaultValue,Function<byte[],E> bytesFn,Function<String,E> stringFn){
        return getValue(key,cls,defaultValue,bytesFn,stringFn,null);
    }
    /**
     * 得到指定key的数据，默认是defaultValue,数据类型是cls, 
     * @param key
     * @param cls
     * @param defaultvalue
     * @param bytesFn 如果从常量Map得到byte[]就用这个对象转化成指定类型的对象
     * @param stringFn 如果从常量Map得到String就用这个对象转化成指定类型的对象
     * @param consumer consumer的accept参数就是新的值
     * @return key对应的数据
     */
    @SuppressWarnings("unchecked")
    private <E> E getValue(String key,Class<E> cls,E defaultValue,Function<byte[],E> bytesFn,Function<String,E> stringFn,Consumer<E> consumer){
        Object value=constants.computeIfAbsent(key,(k)->{
            try{
                return Help.convert(getDataByCurator(key,cls,consumer),defaultValue);
            }catch(Exception e){
                throw new ConfigException(e);
            }
        });
        if(value==null){
            return null;
        }
        if(cls.isInstance(value)){
            return (E)value;
        }
        return (E)constants.compute(key,(k,v)->{
            if(cls.isInstance(v)){
                return v;
            }else if(value instanceof byte[] && bytesFn!=null){
                return bytesFn.apply((byte[])value);
            }else if(value instanceof String && stringFn!=null){
                return stringFn.apply((String)value);
            }else{
                throw new ConfigException("there is not right value, expected "+cls+", but value type is "+value.getClass()+", and value is "+value);
            }
        });
    }
    
    public static void main(String[] args) throws Exception{
        ConfigManager cm=ConfigManager.getInstance();
        cm.setDataOrCreate("a.b.c",1);
        cm.setDataOrCreate("a.b.d",2);
        cm.watch(cm.curator,"a.b.c");
        cm.watch(cm.curator,"a.b.d");
        synchronized(cm){
            cm.wait();
        }
    }
}
