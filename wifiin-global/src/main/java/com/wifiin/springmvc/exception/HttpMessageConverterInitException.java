package com.wifiin.springmvc.exception;

public class HttpMessageConverterInitException extends RuntimeException{

    /**
     * 
     */
    private static final long serialVersionUID=-3140558443646685038L;

    public HttpMessageConverterInitException(){
        super();
        // TODO Auto-generated constructor stub
    }

    public HttpMessageConverterInitException(String message,Throwable cause,boolean enableSuppression,
            boolean writableStackTrace){
        super(message,cause,enableSuppression,writableStackTrace);
        // TODO Auto-generated constructor stub
    }

    public HttpMessageConverterInitException(String message,Throwable cause){
        super(message,cause);
        // TODO Auto-generated constructor stub
    }

    public HttpMessageConverterInitException(String message){
        super(message);
        // TODO Auto-generated constructor stub
    }

    public HttpMessageConverterInitException(Throwable cause){
        super(cause);
        // TODO Auto-generated constructor stub
    }
    
}
