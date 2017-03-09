package com.wifiin.exception;

public class DSLException extends RuntimeException{

    /**
     * 
     */
    private static final long serialVersionUID=5278138339875203148L;

    public DSLException(){
        super();
        // TODO Auto-generated constructor stub
    }

    public DSLException(String message,Throwable cause,boolean enableSuppression,boolean writableStackTrace){
        super(message,cause,enableSuppression,writableStackTrace);
        // TODO Auto-generated constructor stub
    }

    public DSLException(String message,Throwable cause){
        super(message,cause);
        // TODO Auto-generated constructor stub
    }

    public DSLException(String message){
        super(message);
        // TODO Auto-generated constructor stub
    }

    public DSLException(Throwable cause){
        super(cause);
        // TODO Auto-generated constructor stub
    }
    
}
