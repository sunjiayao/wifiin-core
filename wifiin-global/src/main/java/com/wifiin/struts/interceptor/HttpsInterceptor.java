package com.wifiin.struts.interceptor;


public class HttpsInterceptor extends SchemeInterceptor{

	/**
	 * 
	 */
	private static final long serialVersionUID = 147826933595963044L;

	public HttpsInterceptor(){
		super.setScheme("https");
	}
}
