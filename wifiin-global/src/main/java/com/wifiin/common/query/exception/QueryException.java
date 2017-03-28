package com.wifiin.common.query.exception;

public class QueryException extends RuntimeException{

    /**
     * 
     */
    private static final long serialVersionUID=3870678160022706986L;

    public QueryException(){
        super();
        // TODO Auto-generated constructor stub
    }

    public QueryException(String message,Throwable cause,boolean enableSuppression,boolean writableStackTrace){
        super(message,cause,enableSuppression,writableStackTrace);
        // TODO Auto-generated constructor stub
    }

    public QueryException(String message,Throwable cause){
        super(message,cause);
        // TODO Auto-generated constructor stub
    }

    public QueryException(String message){
        super(message);
        // TODO Auto-generated constructor stub
    }

    public QueryException(Throwable cause){
        super(cause);
        // TODO Auto-generated constructor stub
    }
    
}
