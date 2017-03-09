package com.wifiin.exception;

/**
 * 创建、更新、删除数据时使用此异常封装所有抛出的异常
 *
 */
public class CUDException extends RuntimeException {

	public CUDException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public CUDException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public CUDException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public CUDException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -2688216589310874425L;

}
