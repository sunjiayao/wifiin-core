package com.wifiin.struts.result;

import javax.servlet.http.HttpServletResponse;

import com.wifiin.common.CommonConstant;
import com.wifiin.common.GlobalObject;

import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.Result;

/**
 * 把action内指定属性转化成json，作为浏览器响应
 * 
 * */
public class JsonPropertyResult implements Result {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4490223544019927709L;
	
	/**
	 * 成员属性名
	 * */
	private String jsonProperty="result";
	private String charset=CommonConstant.DEFAULT_CHARSET_NAME;

	@Override
	public void execute(ActionInvocation ai) throws Exception {
		Object action = ai.getAction();
		HttpServletResponse response=ServletActionContext.getResponse();
		response.setContentType("text/json;charset="+charset);
		response.getWriter()
				.print(GlobalObject.getJsonMapper().writeValueAsString(action.getClass().getMethod(
						"get" + Character.toUpperCase(jsonProperty.charAt(0))
						+ jsonProperty.substring(1)).invoke(action)));
	}

	public String getJsonProperty() {
		return jsonProperty;
	}
	public void setJsonProperty(String jsonProperty) {
		this.jsonProperty = jsonProperty;
	}
	public String getCharset() {
		return charset;
	}
	public void setCharset(String charset) {
		this.charset = charset;
	}
}