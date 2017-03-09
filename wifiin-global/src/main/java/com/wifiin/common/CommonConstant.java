package com.wifiin.common;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.wifiin.exception.ConstantLoadingException;
import com.wifiin.util.Help;

/**
 * 在工程类路径的根下查找文件名以constant.properties结尾的文件<br/>
 * 加载其中的properties名值对作为常量定义保存在名为PROPERTIES的私有静态属性内<br/>
 * 可通过getProperties()方法获取<br/>
 * 
 * constant.prefix.properties文件中储存key是constant.prefix的常量，这个常量的值决定加载哪个properties常量文件里的值。
 * 如果constant.prefix=product，加载的就是product.constant.properties
 * 
 * properties.dev.reload.period 定义重新载入常量定义文件的周期，默认是60000ms，最好跟properties.dev.reload.period.timeunit搭配使用，否则可能导致周期过短或过长
 * properties.dev.reload.period.timeunit 定义重新载入周期的时间单位，默认是TimeUnit.MILLISECONDS
 * properties.dev.reloadable 如果这个键的值是"1"，就会重新定时重新加载常量定义文件，如果没有定义或者是其它值就不会
 * */
public final class CommonConstant {
	private static final Logger log=LoggerFactory.getLogger(CommonConstant.class);
	/**
	 * 常量属性文件后缀，所有在资源目录中以此结束的文件名都作为常量文件名加载。
	 * 常量key相同的以最后一次出现的为准
	 */
	private static String CONSTANT_PROPERTIES="current.constant.properties";
	private static Map<String,String> CONSTANTS=Maps.newConcurrentMap();
	private static String getConstantProperties(){
	    return CONSTANT_PROPERTIES;
	}
	public static void setConstantProperties(String props){
	    CONSTANT_PROPERTIES=props;
	}
	/**
	 * 以此为名在cookie中保存登录用户id
	 */
	public static final String USER_ID_HEADER="User-Id";
	/**
	 * 以此为名在cookie中保存登录用户名
	 */
	public static final String LOGGED_USERNAME="LOGGED-USERNAME";
	/**
	 * 未找到登录用户名的标志
	 */
	public static final long LOGIN_USERNAME_UNEXISTANCE=-1;
	/**
	 * 未找到登录用户密码的标志
	 */
	public static final long LOGIN_INCORRECT_PASSWORD=-2;
	/**
	 * 默认字符集的名称
	 */
	public static final String DEFAULT_CHARSET_NAME="UTF-8";
	/**
	 * 默认字符集的名称，已过时，使用CommonConstant.DEFAULT_CHARSET_NAME代替
	 */
	@Deprecated
	public static final String DEFAULT_CHARSET=DEFAULT_CHARSET_NAME;
	/**
	 * 默认字符集
	 */
	public static final Charset DEFAULT_CHARSET_INSTANCE=Charset.forName(DEFAULT_CHARSET_NAME);
	public static final String PASSWORD = "password";
	
	private static String[] EMPTY_STRING_ARRAY;
	public static String[] getEmptyStringArray(){
	    if(EMPTY_STRING_ARRAY==null){
	        synchronized(CommonConstant.class){
	            if(EMPTY_STRING_ARRAY==null){
	                EMPTY_STRING_ARRAY=new String[0];
	            }
	        }
	    }
	    return EMPTY_STRING_ARRAY;
	}

	/**
	 * 返回值作为向浏览器推送的javascript函数名
	 * properties键名：properties.dev.project.iframefn
	 * @return String
	 */
	public static String getIFRAMEFN(){
		return CONSTANTS.get("properties.dev.project.iframefn");
	}

	/**
	 * 决定log4j配置是否可在运行期重加载
	 * properties键名：properties.dev.project.log4j.reloadable
	 */
	public static String getLOG4J_CONF_RELOADABLE(){
		return CONSTANTS.get("properties.dev.project.log4j.reloadable");
	}
	/**
	 * 决定log4j日志的文件路径
	 * properties键名：properties.dev.project.log4j.path
	 */
	public static String getLOG4J_PATH(){
		return CONSTANTS.get("properties.dev.project.log4j.path");
	}
	/**
	 * 决定重加载log4j配置的周期
	 * properties键名：properties.dev.project.log4j.reload.period
	 * @return String
	 */
	public static String getLOG4J_RELOAD_PERIOD(){
		return CONSTANTS.get("properties.dev.project.log4j.reload.period");
	}
	/**
	 * 确定svn协议
	 * properties键名：properties.dev.project.svn.protocol
	 * @return String
	 */
	public static String getSVN_PROTOCOL(){
		return CONSTANTS.get("properties.dev.project.svn.protocol");
	}
	/**
	 * 确定svn用户名
	 * properties键名：properties.dev.project.svn.auth.user
	 * @return String
	 */
	public static String getSVN_AUTH_USER(){
		return CONSTANTS.get("properties.dev.project.svn.auth.user");
	}
	/**
	 * 确定svn密码
	 * properties键名：properties.dev.project.svn.auth.pass
	 * @return String
	 */
	public static String getSVN_AUTH_PASS(){
		return CONSTANTS.get("properties.dev.project.svn.auth.pass");
	}
	/**
	 * 如果应用需要一个统一的线程池完成系统级任务可指定此线程池
	 * properties键名：properties.dev.project.threadpool.size
	 * @return int 系统线程池大小
	 * @see
	 */
	public static int getTHREAD_POOL_SIZE(){
		String size=CONSTANTS.get("properties.dev.project.threadpool.size");
		int count=Help.isNotEmpty(size)?Integer.parseInt(size):0;
		return count>0?count*Runtime.getRuntime().availableProcessors():1;
	}
	public static int getIntConstant(String key, int defaultVal){
		String val=CONSTANTS.get(key);
		if(Help.isEmpty(val)){
			return defaultVal;
		}else{
			return Integer.parseInt(val);
		}
	}
	public static long getLongConstant(String key, long defaultVal){
		String val=CONSTANTS.get(key);
		if(Help.isEmpty(val)){
			return defaultVal;
		}else{
			return Long.parseLong(val);
		}
	}
	public static boolean getBooleanConstant(String key, boolean defaultVal){
		String val=CONSTANTS.get(key);
		if(Help.isEmpty(val)){
			return defaultVal;
		}else{
			return Boolean.parseBoolean(val);
		}
	}
	public static String getStringConstant(String key, String defaultVal){
		String val=CONSTANTS.get(key);
		if(Help.isEmpty(val)){
			return defaultVal;
		}else{
			return val;
		}
	}
	public static <E extends Enum> E getEnumConstant(String key, Class enumType, Enum<?> defaultVal){
		String val=CONSTANTS.get(key);
		if(Help.isEmpty(val)){
			return (E)defaultVal;
		}else{
			return (E)Enum.valueOf(enumType, val);
		}
	}
	public static void loadConstant(Class constantClass) throws IllegalArgumentException, IllegalAccessException{
		clearBufferedValue(constantClass);
		loadConstant();
	}
	public static void clearBufferedValue(Class constantClass) throws IllegalArgumentException, IllegalAccessException{
		Field[] fs=constantClass.getDeclaredFields();
		for(int i=0,l=fs.length;i<l;i++){
			Field f=fs[i];
			Class ft=f.getType();
			if((f.getModifiers()&Modifier.FINAL)>0){
				continue;
			}
			if(ft.isPrimitive()){
				if(ft.equals(Boolean.TYPE)){
					f.setBoolean(null, false);
				}else if(ft.equals(Byte.TYPE)){
					f.set(null,(byte)0);
				}else if(ft.equals(Short.TYPE)){
					f.set(null,(short)0);
				}else{
					f.set(null, 0);
				}
			}else{
				f.set(null,null);
			}
		}
	}
    public static void loadConstant(){
		Properties properties=new Properties();
		try {
			String constantFileName=getConstantProperties();
			URL url=CommonConstant.class.getResource("/");
			if(url.getProtocol().equalsIgnoreCase("jar")){
			    try(JarFile jar=new JarFile(new File(url.getFile().replaceFirst("^file:/+", "/").replaceFirst("!/", "")))){
			        Enumeration<JarEntry> entries=jar.entries();
			        while(entries.hasMoreElements()){
			            JarEntry entry=entries.nextElement();
			            if(!entry.isDirectory() && entry.getName().endsWith(constantFileName)){
			                try(InputStream jarIn=jar.getInputStream(entry)){
			                    properties.putAll(Help.loadProperties(jarIn,DEFAULT_CHARSET_NAME));
			                }
			            }
			        }
			    }
			}else{
			    for(File props:new File(url.toURI()).listFiles()){
	                if(props.isFile() && props.getName().endsWith(constantFileName)){
	                    properties.putAll(Help.loadProperties(props, DEFAULT_CHARSET_NAME));
	                }
	            }
			}
			properties.putAll(System.getProperties());//把System.properties填到加载的properties对象
			setProperties(properties);
		}catch(Exception e) {
			throw new ConstantLoadingException(e);
		}
		scheduledLoad();
	}
	private static void scheduledLoad(){
		if("1".equals(CONSTANTS.get("properties.dev.reloadable"))){
		    String periodDef=CONSTANTS.get("properties.dev.reload.period");
		    String timeUnit=CONSTANTS.get("properties.dev.reload.period.timeunit");
		    long delay=Help.isEmpty(periodDef)?60000:Long.parseLong(periodDef);
			final ScheduledExecutorService ses=Executors.newScheduledThreadPool(1);
			ses.scheduleWithFixedDelay(()->{
				try{
					loadConstant();
				}catch(ConstantLoadingException e){
					log.error("CommonConstant.loadConstant:scheduled",e);
				}finally{
				    ses.shutdownNow();
				}
			},delay,delay,Help.isEmpty(timeUnit)?TimeUnit.MILLISECONDS:TimeUnit.valueOf(timeUnit));
		}
	}
	@SuppressWarnings("rawtypes")
    public static void setProperties(Properties props){
	    for(Map.Entry entry:props.entrySet()){
            CONSTANTS.put((String)entry.getKey(),(String)entry.getValue());
        }
	}
	static{
		loadConstant();
	}
}