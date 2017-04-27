package com.wifiin.spring.exception;

public class TooManyApplicationContextHolderException extends RuntimeException{
    /**
     * 
     */
    private static final long serialVersionUID=7445072631157896564L;
    public static final TooManyApplicationContextHolderException instance=new TooManyApplicationContextHolderException();
    public TooManyApplicationContextHolderException(){
        super();
        // TODO Auto-generated constructor stub
    }

    public TooManyApplicationContextHolderException(String message,Throwable cause,boolean enableSuppression,
            boolean writableStackTrace){
        super(message,cause,enableSuppression,writableStackTrace);
        // TODO Auto-generated constructor stub
    }

    public TooManyApplicationContextHolderException(String message,Throwable cause){
        super(message,cause);
        // TODO Auto-generated constructor stub
    }

    public TooManyApplicationContextHolderException(String message){
        super(message);
        // TODO Auto-generated constructor stub
    }

    public TooManyApplicationContextHolderException(Throwable cause){
        super(cause);
        // TODO Auto-generated constructor stub
    }
    
}
