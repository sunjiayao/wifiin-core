package com.wifiin.springmvc.exception;

public class MsgCodeNotFoundException extends RuntimeException{

    /**
     * 
     */
    private static final long serialVersionUID=6733110335429198384L;

    public MsgCodeNotFoundException(){
        super();
        // TODO Auto-generated constructor stub
    }

    public MsgCodeNotFoundException(String message,Throwable cause,boolean enableSuppression,
            boolean writableStackTrace){
        super(message,cause,enableSuppression,writableStackTrace);
        // TODO Auto-generated constructor stub
    }

    public MsgCodeNotFoundException(String message,Throwable cause){
        super(message,cause);
        // TODO Auto-generated constructor stub
    }

    public MsgCodeNotFoundException(String message){
        super(message);
        // TODO Auto-generated constructor stub
    }

    public MsgCodeNotFoundException(Throwable cause){
        super(cause);
        // TODO Auto-generated constructor stub
    }
    
}
