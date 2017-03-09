package com.wifiin.exception;

/**
 * 加载me.jor.common.CommonConstant类时，使用此类的实例封装所有抛出的异常
 *
 */
public class ConstantLoadingException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3326038267821571060L;

	public ConstantLoadingException() {}

	public ConstantLoadingException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConstantLoadingException(String message) {
		super(message);
	}

	public ConstantLoadingException(Throwable cause) {
		super(cause);
	}
}