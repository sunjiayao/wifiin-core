package com.wifiin.struts.interceptor;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import com.wifiin.struts.action.AbstractBaseAction;

/**
 * 判断url的后缀名是否符合要求
 * <interceptor-ref name="postfixInterpector">
 *	   <param name="postfix">zip</param>
 * </interceptor-ref>
 *
 */
public class PostfixInterceptor extends AbstractInterceptor {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2069839859663017346L;
	
	private String postfix;
	@Override
	public String intercept(ActionInvocation ai) throws Exception{
		if(postfix.indexOf("*")>=0 || postfix.indexOf(((AbstractBaseAction)ai.getAction()).getUriPostfix())>=0){
			String result=ai.invoke();
			return result;
		}
		return ActionSupport.ERROR;
	}
	public String getPostfix() {
		return postfix;
	}
	public void setPostfix(String postfix) {
		this.postfix = postfix;
	}
	
}