package com.wifiin.multilanguage.aop.exception;

public class LanguageNotFoundException extends RuntimeException{

    /**
     * 
     */
    private static final long serialVersionUID=157149745493215633L;

    public LanguageNotFoundException(){
        super();
        // TODO Auto-generated constructor stub
    }

    public LanguageNotFoundException(String message,Throwable cause,boolean enableSuppression,
            boolean writableStackTrace){
        super(message,cause,enableSuppression,writableStackTrace);
        // TODO Auto-generated constructor stub
    }

    public LanguageNotFoundException(String message,Throwable cause){
        super(message,cause);
        // TODO Auto-generated constructor stub
    }

    public LanguageNotFoundException(String message){
        super(message);
        // TODO Auto-generated constructor stub
    }

    public LanguageNotFoundException(Throwable cause){
        super(cause);
        // TODO Auto-generated constructor stub
    }
    
}
