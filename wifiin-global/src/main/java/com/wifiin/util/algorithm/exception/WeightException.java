package com.wifiin.util.algorithm.exception;

@SuppressWarnings("serial")
public class WeightException extends RuntimeException{

    public WeightException(){
        super();
    }

    public WeightException(String message,Throwable cause,boolean enableSuppression,boolean writableStackTrace){
        super(message,cause,enableSuppression,writableStackTrace);
        // TODO Auto-generated constructor stub
    }

    public WeightException(String message,Throwable cause){
        super(message,cause);
        // TODO Auto-generated constructor stub
    }

    public WeightException(String message){
        super(message);
        // TODO Auto-generated constructor stub
    }

    public WeightException(Throwable cause){
        super(cause);
        // TODO Auto-generated constructor stub
    }
    
}
