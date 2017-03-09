package com.wifiin.springmvc;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.wifiin.util.ip.IPSeeker;

/**
 * 提供基本的获取HttpServletRequest的API
 * @author Running
 *
 */
public class SpringMVCContext {
    public static HttpServletRequest getRequest(){
        return ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
    }
    public static String getIp(){
        return getIp(getRequest());
    }
    public static String getIp(HttpServletRequest request){
        return IPSeeker.getIp(request);
    }
    public static String getURI(){
        return getRequest().getRequestURI();
    }
    public static String getQueryString(){
        return getRequest().getQueryString();
    }
}
