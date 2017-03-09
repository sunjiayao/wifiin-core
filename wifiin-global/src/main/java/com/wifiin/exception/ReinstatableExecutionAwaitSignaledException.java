package com.wifiin.exception;

public class ReinstatableExecutionAwaitSignaledException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5149966766783518368L;

	public ReinstatableExecutionAwaitSignaledException() {}

	public ReinstatableExecutionAwaitSignaledException(String message, Throwable cause) {
		super(message, cause);
	}

	public ReinstatableExecutionAwaitSignaledException(String message) {
		super(message);
	}

	public ReinstatableExecutionAwaitSignaledException(Throwable cause) {
		super(cause);
	}

}
