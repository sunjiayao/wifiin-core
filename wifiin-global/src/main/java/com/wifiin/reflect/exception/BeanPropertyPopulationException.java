package com.wifiin.reflect.exception;

public class BeanPropertyPopulationException extends RuntimeException{

    /**
     * 
     */
    private static final long serialVersionUID=-5361707895217544196L;

    public BeanPropertyPopulationException(){
        super();
        // TODO Auto-generated constructor stub
    }

    public BeanPropertyPopulationException(String message,Throwable cause,boolean enableSuppression,
            boolean writableStackTrace){
        super(message,cause,enableSuppression,writableStackTrace);
        // TODO Auto-generated constructor stub
    }

    public BeanPropertyPopulationException(String message,Throwable cause){
        super(message,cause);
        // TODO Auto-generated constructor stub
    }

    public BeanPropertyPopulationException(String message){
        super(message);
        // TODO Auto-generated constructor stub
    }

    public BeanPropertyPopulationException(Throwable cause){
        super(cause);
        // TODO Auto-generated constructor stub
    }
    
}
