package com.wifiin.cron.exception;

public class CronException extends RuntimeException{

    /**
     * 
     */
    private static final long serialVersionUID=5147101398288855799L;

    public CronException(){
        super();
        // TODO Auto-generated constructor stub
    }

    public CronException(String message,Throwable cause,boolean enableSuppression,boolean writableStackTrace){
        super(message,cause,enableSuppression,writableStackTrace);
        // TODO Auto-generated constructor stub
    }

    public CronException(String message,Throwable cause){
        super(message,cause);
        // TODO Auto-generated constructor stub
    }

    public CronException(String message){
        super(message);
        // TODO Auto-generated constructor stub
    }

    public CronException(Throwable cause){
        super(cause);
        // TODO Auto-generated constructor stub
    }
    
}
