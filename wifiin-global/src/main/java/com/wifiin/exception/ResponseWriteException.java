package com.wifiin.exception;

/**
 * web应用向浏览器输出数据时可能抛出此异常
 */
public class ResponseWriteException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6696551812060137126L;

	public ResponseWriteException() {
	}

	public ResponseWriteException(String message, Throwable cause) {
		super(message, cause);
	}

	public ResponseWriteException(String message) {
		super(message);
	}

	public ResponseWriteException(Throwable cause) {
		super(cause);
	}
}