package com.wifiin.exception;

/**
 * web应用向浏览器输出json时可能抛出此异常
 *
 */
public class ResponseWriteJsonException extends ResponseWriteException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6624909803668896076L;

	public ResponseWriteJsonException() {
	}

	public ResponseWriteJsonException(String message, Throwable cause) {
		super(message, cause);
	}

	public ResponseWriteJsonException(String message) {
		super(message);
	}

	public ResponseWriteJsonException(Throwable cause) {
		super(cause);
	}
}