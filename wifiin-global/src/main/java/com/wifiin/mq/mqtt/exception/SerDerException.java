package com.wifiin.mq.mqtt.exception;

public class SerDerException extends RuntimeException{

    /**
     * 
     */
    private static final long serialVersionUID=8369399744740826892L;

    public SerDerException(){
        super();
        // TODO Auto-generated constructor stub
    }

    public SerDerException(String message,Throwable cause,boolean enableSuppression,boolean writableStackTrace){
        super(message,cause,enableSuppression,writableStackTrace);
        // TODO Auto-generated constructor stub
    }

    public SerDerException(String message,Throwable cause){
        super(message,cause);
        // TODO Auto-generated constructor stub
    }

    public SerDerException(String message){
        super(message);
        // TODO Auto-generated constructor stub
    }

    public SerDerException(Throwable cause){
        super(cause);
        // TODO Auto-generated constructor stub
    }
    
}
