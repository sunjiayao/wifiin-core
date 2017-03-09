package com.wifiin.struts.action;

import java.io.File;

public class UploadMultiFileAction extends UploadSingleFileAction{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7862898155398291486L;
	private File[] upload;//域对应的文件内容
	private String[] uploadFileName;//文件名
	private String[] uploadContentType;//文件类型
	
	public String execute(){
		for(int i=0,l=upload.length;i<l;i++){
			super.copy(upload[i], uploadFileName[i], uploadContentType[i]);
		}
		return SUCCESS;
	}
	
	public File[] getUpload() {
		return upload;
	}
	public void setUpload(File[] upload) {
		this.upload = upload;
	}
	public String[] getUploadFileName() {
		return uploadFileName;
	}
	public void setUploadFileName(String[] uploadFileName) {
		this.uploadFileName = uploadFileName;
	}
	public String[] getUploadContentType() {
		return uploadContentType;
	}
	public void setUploadContentType(String[] uploadContentType) {
		this.uploadContentType = uploadContentType;
	}
	
	
}
