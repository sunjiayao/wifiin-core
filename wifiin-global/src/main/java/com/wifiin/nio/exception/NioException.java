package com.wifiin.nio.exception;

public class NioException extends RuntimeException{

    /**
     * 
     */
    private static final long serialVersionUID=427554272850611934L;

    public NioException(){
        super();
        // TODO Auto-generated constructor stub
    }

    public NioException(String message,Throwable cause,boolean enableSuppression,boolean writableStackTrace){
        super(message,cause,enableSuppression,writableStackTrace);
        // TODO Auto-generated constructor stub
    }

    public NioException(String message,Throwable cause){
        super(message,cause);
        // TODO Auto-generated constructor stub
    }

    public NioException(String message){
        super(message);
        // TODO Auto-generated constructor stub
    }

    public NioException(Throwable cause){
        super(cause);
        // TODO Auto-generated constructor stub
    }
    
}
