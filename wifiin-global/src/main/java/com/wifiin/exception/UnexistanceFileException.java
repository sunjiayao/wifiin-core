package com.wifiin.exception;

/**
 * 不存在的文件异常
 *
 */
public class UnexistanceFileException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9205394725223279010L;

	public UnexistanceFileException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public UnexistanceFileException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public UnexistanceFileException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public UnexistanceFileException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}
	
}
