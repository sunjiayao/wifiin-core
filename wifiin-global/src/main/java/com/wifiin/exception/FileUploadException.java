package com.wifiin.exception;

public class FileUploadException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4501748071154608479L;
	public FileUploadException(String fileName, String contentType, Exception e){
		super(fileName+"/"+contentType, e);
	}
}
