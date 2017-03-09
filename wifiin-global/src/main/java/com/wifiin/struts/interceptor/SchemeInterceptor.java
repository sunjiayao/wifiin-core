package com.wifiin.struts.interceptor;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.StrutsStatics;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

public class SchemeInterceptor extends AbstractInterceptor{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 105755397315604277L;
	private String scheme;
	
	public String getScheme() {
		return scheme;
	}

	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	@Override
	public String intercept(ActionInvocation ai) throws Exception {
		if(((HttpServletRequest)ai.getInvocationContext().get(StrutsStatics.HTTP_REQUEST)).getScheme().toLowerCase().equals(scheme)){
			return ai.invoke();
		}else{
			return ActionSupport.ERROR;
		}
	}

}
