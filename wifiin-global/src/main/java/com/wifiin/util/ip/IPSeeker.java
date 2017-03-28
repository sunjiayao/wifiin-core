package com.wifiin.util.ip;

import javax.servlet.http.HttpServletRequest;

import com.wifiin.util.Help;

public class IPSeeker {
	public static final String LOCALHOST="127.0.0.1";
	
	/**
	 * 得到发起请求的ip
	 * @param request
	 * @return
	 */
	public static String getIp(HttpServletRequest request){
		String[] header=new String[]{"X-Forwarded-For","Proxy-Client-IP","WL-Proxy-Client-IP"};
		String ip=null;
		int i=0,l=header.length;
		do{
			ip=request.getHeader(header[i]);
		}while(++i<l && (Help.isEmpty(ip) || "unknown".equalsIgnoreCase(ip) || LOCALHOST.equals(ip)));
		if(Help.isEmpty(ip) || "unknown".equalsIgnoreCase(ip) || LOCALHOST.equals(ip)){
			ip = request.getRemoteAddr();
		}
		return ip;
	}
	
	/**
	 * 判断参数表示的ip是不是本机ip 127.0.0.1或localhost
	 * @param ip
	 * @return
	 */
	public static boolean isLocalhost(String ip){
		return LOCALHOST.equals(ip) || "localhost".equalsIgnoreCase(ip);
	}
}
