package com.wifiin.pool.exception;

public class PoolException extends RuntimeException{

    /**
     * 
     */
    private static final long serialVersionUID=8564480279089374657L;

    public PoolException(){
        super();
        // TODO Auto-generated constructor stub
    }

    public PoolException(String message,Throwable cause,boolean enableSuppression,boolean writableStackTrace){
        super(message,cause,enableSuppression,writableStackTrace);
        // TODO Auto-generated constructor stub
    }

    public PoolException(String message,Throwable cause){
        super(message,cause);
        // TODO Auto-generated constructor stub
    }

    public PoolException(String message){
        super(message);
        // TODO Auto-generated constructor stub
    }

    public PoolException(Throwable cause){
        super(cause);
        // TODO Auto-generated constructor stub
    }
    
}
