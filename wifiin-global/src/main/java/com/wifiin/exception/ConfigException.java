package com.wifiin.exception;

public class ConfigException extends RuntimeException{

    /**
     * 
     */
    private static final long serialVersionUID=6212795620950952673L;

    public ConfigException(){
        super();
        // TODO Auto-generated constructor stub
    }

    public ConfigException(String message,Throwable cause,boolean enableSuppression,boolean writableStackTrace){
        super(message,cause,enableSuppression,writableStackTrace);
        // TODO Auto-generated constructor stub
    }

    public ConfigException(String message,Throwable cause){
        super(message,cause);
        // TODO Auto-generated constructor stub
    }

    public ConfigException(String message){
        super(message);
        // TODO Auto-generated constructor stub
    }

    public ConfigException(Throwable cause){
        super(cause);
        // TODO Auto-generated constructor stub
    }
    
}
