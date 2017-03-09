package com.wifiin.util.context.log4j.listener;

import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;

import com.wifiin.util.Help;
/**
 * 创建这个类的对象并调用init方法，可以按照指定delay的毫秒数定时重新加载应用classpath下的log4j配置文件
 * 一个应用只需要创建这个类的一个对象即可
 * 类路径下的properties文件由log4jPropsPath指定，路径要符合posix系统路径规则
 * @author wujingrun
 *
 */
public class Log4jConfigureLoader{
	private long delay;
	private String log4jPropsPath;
	public String getLog4jPropsPath() {
		return log4jPropsPath;
	}

	public void setLog4jPropsPath(String log4jPropsPath) {
		this.log4jPropsPath = log4jPropsPath;
	}

	public long getDelay() {
		return delay;
	}

	public void setDelay(long delay) {
		this.delay = delay;
	}

	public Log4jConfigureLoader(){}
	
	public void init(){
		if(Help.isEmpty(log4jPropsPath)){
			this.log4jPropsPath="log4j.properties";
		}
		if(!this.log4jPropsPath.startsWith("/")){
			this.log4jPropsPath='/'+this.log4jPropsPath;
		}
		PropertyConfigurator.configureAndWatch(Log4jConfigureLoader.class.getResource(this.log4jPropsPath).getPath(), delay==0?60000:delay);
		Runtime.getRuntime().addShutdownHook(new Thread(){
			public void run(){
				LogManager.shutdown();
			}
		});
	}
}
