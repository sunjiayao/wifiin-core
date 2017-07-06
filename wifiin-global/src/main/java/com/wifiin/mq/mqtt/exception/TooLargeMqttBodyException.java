package com.wifiin.mq.mqtt.exception;

public class TooLargeMqttBodyException extends RuntimeException{

    /**
     * 
     */
    private static final long serialVersionUID=5471830868423445341L;

    public TooLargeMqttBodyException(){
        super();
        // TODO Auto-generated constructor stub
    }
    public TooLargeMqttBodyException(int length){
        this("the max length of mqtt body is 268435455, but current mqtt body size is "+length);
    }
    public TooLargeMqttBodyException(String message,Throwable cause,boolean enableSuppression,
            boolean writableStackTrace){
        super(message,cause,enableSuppression,writableStackTrace);
        // TODO Auto-generated constructor stub
    }

    public TooLargeMqttBodyException(String message,Throwable cause){
        super(message,cause);
        // TODO Auto-generated constructor stub
    }

    public TooLargeMqttBodyException(String message){
        super(message);
        // TODO Auto-generated constructor stub
    }

    public TooLargeMqttBodyException(Throwable cause){
        super(cause);
        // TODO Auto-generated constructor stub
    }
    
}
