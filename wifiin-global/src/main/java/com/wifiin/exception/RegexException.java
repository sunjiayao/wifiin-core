package com.wifiin.exception;

public class RegexException extends RuntimeException{

    /**
     * 
     */
    private static final long serialVersionUID=2631289227476014982L;

    public RegexException(){
        super();
        // TODO Auto-generated constructor stub
    }

    public RegexException(String message,Throwable cause,boolean enableSuppression,boolean writableStackTrace){
        super(message,cause,enableSuppression,writableStackTrace);
        // TODO Auto-generated constructor stub
    }

    public RegexException(String message,Throwable cause){
        super(message,cause);
        // TODO Auto-generated constructor stub
    }

    public RegexException(String message){
        super(message);
        // TODO Auto-generated constructor stub
    }

    public RegexException(Throwable cause){
        super(cause);
        // TODO Auto-generated constructor stub
    }
    
}
