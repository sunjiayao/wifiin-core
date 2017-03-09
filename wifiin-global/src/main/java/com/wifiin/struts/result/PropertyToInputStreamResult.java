package com.wifiin.struts.result;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import com.wifiin.common.CommonConstant;
import com.wifiin.exception.UnsupportedResultTypeException;

import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.Result;

public class PropertyToInputStreamResult implements Result{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6183228370902227886L;
	/**
	 * 成员属性名
	 * */
	private String result="result";
	private int bufferSize=8192;//缓冲大小
	private String charset=CommonConstant.DEFAULT_CHARSET_NAME;
	private String contentType="text/plain";
	private boolean filepath;//只有在结果属性表示一个文件路径时才需要指定此值
	
	
	@Override
	public void execute(ActionInvocation ai) throws Exception {
		Object action = ai.getAction();
		HttpServletResponse response=ServletActionContext.getResponse();
		response.setContentType(contentType+";charset="+charset);
		Object result=action.getClass().getMethod(
				"get" + Character.toUpperCase(this.result.charAt(0))
				+ this.result.substring(1)).invoke(action);
		byte[] buf=new byte[bufferSize];
		InputStream in=null;
		if(result instanceof String){
			if(filepath){
				in=new FileInputStream((String)result);
			}else{
				in=new ByteArrayInputStream(((String)result).getBytes(charset));
			}
		}else if(result instanceof byte[]){
			in=new ByteArrayInputStream((byte[])result);
		}else if(result instanceof File){
			in=new FileInputStream((File)result);
		}else if(result instanceof InputStream){
			in=(InputStream)result;
		}else{
			throw new UnsupportedResultTypeException(result);
		}
		try{
			int len=in.read(buf);
			OutputStream out=response.getOutputStream();
			response.setBufferSize(bufferSize);
			while(len>=0){
				out.write(buf,0,len);
			}
			out.flush();
		}finally{
			if(in!=null){
				in.close();
			}
		}
	}

	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public String getCharset() {
		return charset;
	}
	public void setCharset(String charset) {
		this.charset = charset;
	}

	public int getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	public boolean isFilepath() {
		return filepath;
	}

	public void setFilepath(boolean filepath) {
		this.filepath = filepath;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

}
