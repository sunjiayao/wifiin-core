package com.wifiin.common.exception;

public abstract class NonFillInStackTraceException extends RuntimeException{

    /**
     * 
     */
    private static final long serialVersionUID=245024569730797747L;
    public NonFillInStackTraceException(){
        super();
        // TODO Auto-generated constructor stub
    }

    public NonFillInStackTraceException(String message,Throwable cause,boolean enableSuppression,
            boolean writableStackTrace){
        super(message,cause,enableSuppression,writableStackTrace);
        // TODO Auto-generated constructor stub
    }

    public NonFillInStackTraceException(String message,Throwable cause){
        super(message,cause);
        // TODO Auto-generated constructor stub
    }

    public NonFillInStackTraceException(String message){
        super(message);
        // TODO Auto-generated constructor stub
    }

    public NonFillInStackTraceException(Throwable cause){
        super(cause);
        // TODO Auto-generated constructor stub
    }
    @Override
    public Throwable fillInStackTrace(){
        return this;
    }
}
