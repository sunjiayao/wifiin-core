package com.wifiin.kv.exception;

public class TaskRunnerException extends RuntimeException{

    /**
     * 
     */
    private static final long serialVersionUID=-2148144251761438606L;

    public TaskRunnerException(){
        super();
        // TODO Auto-generated constructor stub
    }

    public TaskRunnerException(String message,Throwable cause,boolean enableSuppression,boolean writableStackTrace){
        super(message,cause,enableSuppression,writableStackTrace);
        // TODO Auto-generated constructor stub
    }

    public TaskRunnerException(String message,Throwable cause){
        super(message,cause);
        // TODO Auto-generated constructor stub
    }

    public TaskRunnerException(String message){
        super(message);
        // TODO Auto-generated constructor stub
    }

    public TaskRunnerException(Throwable cause){
        super(cause);
        // TODO Auto-generated constructor stub
    }
    
}
