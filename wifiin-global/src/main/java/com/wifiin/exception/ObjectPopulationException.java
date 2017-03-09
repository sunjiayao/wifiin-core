package com.wifiin.exception;

public class ObjectPopulationException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2273077361549208653L;

	public ObjectPopulationException() {
		super();
	}

//	public ObjectPopulationException(String message, Throwable cause,
//			boolean enableSuppression, boolean writableStackTrace) {
//		super(message, cause, enableSuppression, writableStackTrace);
//	}

	public ObjectPopulationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ObjectPopulationException(String message) {
		super(message);
	}

	public ObjectPopulationException(Throwable cause) {
		super(cause);
	}

}
