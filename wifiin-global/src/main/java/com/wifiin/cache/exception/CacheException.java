package com.wifiin.cache.exception;

public class CacheException extends RuntimeException{

    /**
     * 
     */
    private static final long serialVersionUID=-5562219135572683314L;

    public CacheException(){
        super();
        // TODO Auto-generated constructor stub
    }

    public CacheException(String message,Throwable cause,boolean enableSuppression,boolean writableStackTrace){
        super(message,cause,enableSuppression,writableStackTrace);
        // TODO Auto-generated constructor stub
    }

    public CacheException(String message,Throwable cause){
        super(message,cause);
        // TODO Auto-generated constructor stub
    }

    public CacheException(String message){
        super(message);
        // TODO Auto-generated constructor stub
    }

    public CacheException(Throwable cause){
        super(cause);
        // TODO Auto-generated constructor stub
    }
    
}
