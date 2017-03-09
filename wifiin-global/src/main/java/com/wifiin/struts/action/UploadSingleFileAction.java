package com.wifiin.struts.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.wifiin.exception.FileUploadException;

public class UploadSingleFileAction extends AbstractBaseAction{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1701683142591830805L;
	private File upload;//域对应的文件内容
	private String uploadFileName;//文件名
	private String uploadContentType;//文件类型
	private String targetPath;
	
	protected void copy(File src, String dst, String contentType){
		File target=new File(targetPath,dst);
		FileOutputStream out=null;
		FileInputStream in=null;
		try{
			out=new FileOutputStream(target);
			in=new FileInputStream(src);
			out.getChannel().transferFrom(in.getChannel(), 0, src.length());
		}catch(Exception e){
			throw new FileUploadException(dst,contentType,e);
		}finally{
			if(out!=null){
				try {
					out.close();
				} catch (IOException e) {}
			}
			if(in!=null){
				try {
					in.close();
				} catch (IOException e) {}
			}
		}
	}
	
	public String execute(){
		copy(upload,uploadFileName,uploadContentType);
		return SUCCESS;
	}
}
