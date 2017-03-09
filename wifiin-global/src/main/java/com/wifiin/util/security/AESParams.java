package com.wifiin.util.security;

import java.io.Serializable;

import com.wifiin.common.CommonConstant;
import com.wifiin.util.Help;

public class AESParams implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5509456396118242737L;
	private String key;
	private String salt;
	private String iv;
	private String charset;
	public AESParams(){}
	public AESParams(String key,String salt,String iv,String charset){
		this.key=key;
		this.salt=salt;
		this.iv=iv;
		this.charset=charset;
	}
	public AESParams(String key,String salt,String iv){
		this(key,salt,iv,CommonConstant.DEFAULT_CHARSET_NAME);
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getSalt() {
		return salt;
	}
	public void setSalt(String salt) {
		this.salt = salt;
	}
	public String getIv() {
		return iv;
	}
	public void setIv(String iv) {
		this.iv = iv;
	}
	public String getCharset() {
		if(Help.isEmpty(charset)){
			setCharset(CommonConstant.DEFAULT_CHARSET_NAME);
		}
		return charset;
	}
	public void setCharset(String charset) {
		this.charset = charset;
	}
	
}
