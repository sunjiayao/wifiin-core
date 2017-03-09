package com.wifiin.struts.action;

import java.io.InputStream;

public abstract class DownloadAction extends AbstractBaseAction{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2215325728810672946L;
	protected InputStream result;
	private String filename;
	public InputStream getResult() {
		return result;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	
}
