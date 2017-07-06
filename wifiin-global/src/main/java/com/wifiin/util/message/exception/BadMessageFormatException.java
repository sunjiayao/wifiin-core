package com.wifiin.util.message.exception;

public class BadMessageFormatException extends RuntimeException{

    /**
     * 
     */
    private static final long serialVersionUID=1188670876375367970L;

    public BadMessageFormatException(){
        super();
        // TODO Auto-generated constructor stub
    }

    public BadMessageFormatException(String message,Throwable cause,boolean enableSuppression,
            boolean writableStackTrace){
        super(message,cause,enableSuppression,writableStackTrace);
        // TODO Auto-generated constructor stub
    }

    public BadMessageFormatException(String message,Throwable cause){
        super(message,cause);
        // TODO Auto-generated constructor stub
    }

    public BadMessageFormatException(String message){
        super(message);
        // TODO Auto-generated constructor stub
    }

    public BadMessageFormatException(Throwable cause){
        super(cause);
        // TODO Auto-generated constructor stub
    }
    
}
