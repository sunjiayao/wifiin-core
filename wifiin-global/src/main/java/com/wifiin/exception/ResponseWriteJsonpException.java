package com.wifiin.exception;

/**
 * web应用向浏览器输出jsonp时可能抛出此异常
 *
 */
public class ResponseWriteJsonpException extends ResponseWriteException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 767365562593866524L;

	public ResponseWriteJsonpException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ResponseWriteJsonpException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public ResponseWriteJsonpException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public ResponseWriteJsonpException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
