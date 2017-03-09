package com.wifiin.struts.interceptor;

import java.util.Arrays;
import java.util.Map;

import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import com.wifiin.common.CommonConstant;
import com.wifiin.struts.action.AbstractBaseAction;
import com.wifiin.util.string.ThreadLocalStringBuilder;

/**
 * 计算每次请求的耗时，并把耗时和请求参数、action类名、url记录到info日志
 *
 */
public class TimeConsumeAndExceptionInterceptor extends AbstractInterceptor {


	/**
	 * 
	 */
	private static final long serialVersionUID = 300759252325913128L;
	private static final Logger logger=LoggerFactory.getLogger(TimeConsumeAndExceptionInterceptor.class);
	@Override
	public String intercept(ActionInvocation ai) throws Exception {
	    StringBuilder log=null;
		try{
			Map<String, Object> params=(Map<String, Object>)ai.getInvocationContext().getParameters();
			StringBuilder requestString= ThreadLocalStringBuilder.builder();
			for(Map.Entry<String, Object> entry:params.entrySet()){
				String paramName=entry.getKey();
				if(!paramName.equals(CommonConstant.PASSWORD)){
					requestString.append(paramName).append('=').append(Arrays.toString((Object[])entry.getValue())).append('&');
				}
			}
			int last=requestString.length()-1;
			if(last>0 && requestString.lastIndexOf("&")==last){
				requestString.deleteCharAt(last);
			}
			String requextText=requestString.toString();
			log=ThreadLocalStringBuilder.builder();
			AbstractBaseAction action=(AbstractBaseAction)ai.getAction();
			log.append(action.getClass().getName())
			   .append(";ip:").append(action.getIp())
               .append(";url:").append(ServletActionContext.getRequest().getServletPath())
			   .append(";parameters:").append(requextText.replaceAll("\\s+", ""))
			   .append(";userid:").append(action.getCookieValue(CommonConstant.USER_ID_HEADER));
			long start=System.currentTimeMillis();
			String result=ai.invoke();
			logger.info(log.append(";consumed:").append((System.currentTimeMillis()-start)).toString());
			return result;
		}catch(Throwable t){
		    if(log==null){
		        log=ThreadLocalStringBuilder.builder();
		    }
			logger.error(log.append(";throwable:").append(t.toString()).toString(),t);
			return ActionSupport.ERROR;
		}
	}
}