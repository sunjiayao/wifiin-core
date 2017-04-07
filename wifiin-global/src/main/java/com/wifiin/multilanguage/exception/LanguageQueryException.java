package com.wifiin.multilanguage.exception;

public class LanguageQueryException extends RuntimeException{

    /**
     * 
     */
    private static final long serialVersionUID=9015935342189630357L;

    public LanguageQueryException(){
        super();
        // TODO Auto-generated constructor stub
    }
    public LanguageQueryException(int status){
        this(Integer.toString(status));
    }
    public LanguageQueryException(String message,Throwable cause,boolean enableSuppression,boolean writableStackTrace){
        super(message,cause,enableSuppression,writableStackTrace);
        // TODO Auto-generated constructor stub
    }

    public LanguageQueryException(String message,Throwable cause){
        super(message,cause);
        // TODO Auto-generated constructor stub
    }

    public LanguageQueryException(String message){
        super(message);
        // TODO Auto-generated constructor stub
    }

    public LanguageQueryException(Throwable cause){
        super(cause);
        // TODO Auto-generated constructor stub
    }
    
}
