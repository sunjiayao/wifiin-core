package com.wifiin.struts.result;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.Result;

import com.wifiin.common.CommonConstant;

public class PlainTextPropertyResult implements Result{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6183228370902227886L;
	/**
	 * 成员属性名
	 * */
	private String textProperty="result";
	private String charset=CommonConstant.DEFAULT_CHARSET_NAME;
	private String contentType="text/plain";
	
	
	@Override
	public void execute(ActionInvocation ai) throws Exception {
		Object action = ai.getAction();
		HttpServletResponse response=ServletActionContext.getResponse();
		response.setContentType(contentType+";charset="+charset);
		response.getWriter()
				.print(action.getClass().getMethod(
						"get" + Character.toUpperCase(textProperty.charAt(0))
						+ textProperty.substring(1)).invoke(action).toString());
	}

	public String getTextProperty() {
		return textProperty;
	}
	public void setTextProperty(String textProperty) {
		this.textProperty = textProperty;
	}
	public String getCharset() {
		return charset;
	}
	public void setCharset(String charset) {
		this.charset = charset;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

}
