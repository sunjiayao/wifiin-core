package com.wifiin.struts.result;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.Result;

/**
 * 这个类什么也不做，
 * 只是有些action不需要向浏览器返回任何内容，此时可用此result
 * */
public class NoneResult implements Result{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6866808213138732000L;

	@Override
	public void execute(ActionInvocation ai) throws Exception {}

}
