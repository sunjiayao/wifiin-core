package com.wifiin.exception;

import com.wifiin.util.string.ThreadLocalStringBuilder;

/**
 * 试图执行不支持的struts2 Result时抛出此异常
 *
 */
public class UnsupportedResultTypeException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4235714774179916294L;

	private Object result;
	
	public UnsupportedResultTypeException() {}

	public UnsupportedResultTypeException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnsupportedResultTypeException(String message) {
		super(message);
	}

	public UnsupportedResultTypeException(Throwable cause) {
		super(cause);
	}
	
	public UnsupportedResultTypeException(Object result){
		this.result=result;
	}
	public UnsupportedResultTypeException(Object result, String message){
		super(message);
		this.result=result;
	}
	public UnsupportedResultTypeException(Object result, Throwable cause){
		super(cause);
		this.result=result;
	}
	public UnsupportedResultTypeException(Object result,String message, Throwable cause){
		super(message,cause);
		this.result=result;
	}
	public String toString(){
		String msg=super.getMessage();
		Throwable cause=super.getCause();
		Class resultClass=result.getClass();
		return ThreadLocalStringBuilder.builder().append(msg).append(cause).append(resultClass).toString();
	}
	
}
