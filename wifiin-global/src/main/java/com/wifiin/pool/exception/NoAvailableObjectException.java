package com.wifiin.pool.exception;

public class NoAvailableObjectException extends RuntimeException{

    /**
     * 
     */
    private static final long serialVersionUID=-8597750924439224056L;

    public NoAvailableObjectException(){
        super();
        // TODO Auto-generated constructor stub
    }

    public NoAvailableObjectException(String message,Throwable cause,boolean enableSuppression,
            boolean writableStackTrace){
        super(message,cause,enableSuppression,writableStackTrace);
        // TODO Auto-generated constructor stub
    }

    public NoAvailableObjectException(String message,Throwable cause){
        super(message,cause);
        // TODO Auto-generated constructor stub
    }

    public NoAvailableObjectException(String message){
        super(message);
        // TODO Auto-generated constructor stub
    }

    public NoAvailableObjectException(Throwable cause){
        super(cause);
        // TODO Auto-generated constructor stub
    }
    
}
