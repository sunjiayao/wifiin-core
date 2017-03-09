package com.wifiin.struts.interceptor;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import com.wifiin.common.Decider;
import com.wifiin.common.GlobalObject;
import com.wifiin.struts.action.AbstractBaseAction;
import com.wifiin.util.Help;
import com.wifiin.util.io.IOUtil;

/**
 * 计算每次请求的耗时，并把耗时和请求参数、action类名、url记录到info日志
 */
public class TimeConsumeAndExceptionForJsonRequestInterceptor extends AbstractInterceptor {
	/**
	 * 
	 */
	private static final long serialVersionUID = 300759252325913128L;
	private static final Logger logger=LoggerFactory.getLogger(TimeConsumeAndExceptionForJsonRequestInterceptor.class);
	private Logger actualLogger;
	private String defaultCharset;
	private char logSplitor='|';
	private boolean debug;
	private Decider logDecider;
	private List<String> paramsLogFilter;
	private String loggerName;
	private boolean mustLog=true;
	public String getDefaultCharset() {
		return defaultCharset;
	}
	public void setDefaultCharset(String defaultCharset) {
		this.defaultCharset = defaultCharset;
	}
	public char getLogSplitor() {
		return logSplitor;
	}
	public void setLogSplitor(char logSplitor) {
		this.logSplitor = logSplitor;
	}
	public boolean isDebug() {
		return debug;
	}
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	public Decider getLogDecider() {
		return logDecider;
	}
	public void setLogDecider(Decider logDecider) {
		this.logDecider = logDecider;
	}
	public List<String> getParamsLogFilter() {
		return paramsLogFilter;
	}
	public void setParamsLogFilter(List<String> paramsLogFilter) {
		this.paramsLogFilter = paramsLogFilter;
	}
	public String getLoggerName() {
		return loggerName;
	}
	public void setLoggerName(String loggerName) {
		this.loggerName = loggerName;
	}
	public boolean getMustLog() {
		return mustLog;
	}
	public void setMustLog(boolean mustLog) {
		this.mustLog = mustLog;
	}
	private Logger getLogger(){
		if(actualLogger==null){
			synchronized(this){
				if(actualLogger==null){
					actualLogger=Help.isEmpty(loggerName)?logger:LoggerFactory.getLogger(loggerName);
				}
			}
		}
		return actualLogger;
	}
	private String getParams(AbstractBaseAction action) throws IOException{
		String params=IOUtil.readString(action.getRequest().getInputStream(), defaultCharset);
		getLogger().debug("TimeConsumeAndExceptionForJsonRequestInterceptor.getParams:"+params);
		if(Help.isNotEmpty(params)){
			params=beforeTransformJson(params);
		}
		return params;
	}
	
	private boolean decideLog(String url){
		return logDecider==null || logDecider.decide(url);
	}
	private void log(AbstractBaseAction action,Map paramValues,Object resultContent,long start) throws JsonProcessingException{
		if(mustLog){
			String url=ServletActionContext.getRequest().getServletPath();
			Map log=new HashMap();
			log.put("ip",action.getIp());
			log.put("url", url);
			try{
				log.put("params", filterParams(paramValues));
			}catch(Exception e){
				getLogger().error("TimeConsumeAndExceptionForJsonRequestInterceptor.log:"+paramValues,e);
			}
			if(decideLog(url)){
				log.put("result", resultContent);
			}
			log.put("consumed", System.currentTimeMillis()-start);
			getLogger().info(GlobalObject.getJsonMapper().writeValueAsString(log));
		}
	}
	private void log(AbstractBaseAction action,Object paramValues,Object resultContent,Throwable t) throws JsonProcessingException{
		if(mustLog){
			String url=ServletActionContext.getRequest().getServletPath();
			Map log=new HashMap();
			log.put("ip",action.getIp());
			log.put("url", url);
			log.put("result", resultContent);
			if(paramValues instanceof String){
				getLogger().error(((String)paramValues)+'|'+GlobalObject.getJsonMapper().writeValueAsString(log),t);
			}else{
				try{
					log.put("params", filterParams((Map)paramValues));
				}catch(Exception e){
					getLogger().error("TimeConsumeAndExceptionForJsonRequestInterceptor.log:"+paramValues,e);
				}
				log.put("throwable", t.getClass().getName());
				getLogger().error(GlobalObject.getJsonMapper().writeValueAsString(log),t);
			}
		}
	}
	@Override
	public String intercept(ActionInvocation ai) throws Exception {
		AbstractBaseAction action=(AbstractBaseAction)ai.getAction();
		String params=null;
		Object resultContent=null;
		Map paramValues=null;
		try{
			long start=System.currentTimeMillis();
			params=getParams(action);
			if(Help.isNotEmpty(params)){
				paramValues=beforePopulateParams(GlobalObject.getJsonMapper().readValue(params, Map.class));
				action.getRequest().setAttribute("params", paramValues);
//				Help.populate(action,paramValues,false);
				BeanUtils.copyProperties(action, paramValues);
			}
			String result=ai.invoke();
			resultContent=action.getRequest().getAttribute("result");
			if(Help.isEmpty(resultContent)){
				resultContent=action.getResult();
			}
			log(action,paramValues,resultContent,start);
			return result;
		}catch(Throwable t){
			log(action,paramValues==null?params:paramValues, resultContent, t);
			
			if(Help.isNotEmpty(resultContent)){
				return ActionSupport.SUCCESS;
			}else{
				return ActionSupport.ERROR;
			}
		}
	}
	public String beforeTransformJson(String params){
		return params;
	}
	public Map<String,Object> beforePopulateParams(Map<String,Object> params){
		return params;
	}
	public void populateResultOnError(Throwable t){
	    
	}
	public Map filterParams(Map params){
		if(Help.isNotEmpty(paramsLogFilter) && Help.isNotEmpty(params)){
			for(String paramName:paramsLogFilter){
				params.remove(paramName);
			}
		}
		return params;
	}
}