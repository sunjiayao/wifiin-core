package com.wifiin.common.exception;

public class ResponseImmutableException extends RuntimeException{
    /**
     * 
     */
    private static final long serialVersionUID=-9163773025963142338L;

    public ResponseImmutableException(){
        super();
        // TODO Auto-generated constructor stub
    }

    public ResponseImmutableException(String message,Throwable cause,boolean enableSuppression,
            boolean writableStackTrace){
        super(message,cause,enableSuppression,writableStackTrace);
        // TODO Auto-generated constructor stub
    }

    public ResponseImmutableException(String message,Throwable cause){
        super(message,cause);
        // TODO Auto-generated constructor stub
    }

    public ResponseImmutableException(String message){
        super(message);
        // TODO Auto-generated constructor stub
    }

    public ResponseImmutableException(Throwable cause){
        super(cause);
        // TODO Auto-generated constructor stub
    }
    
}
