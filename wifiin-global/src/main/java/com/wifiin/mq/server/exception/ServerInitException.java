package com.wifiin.mq.server.exception;

public class ServerInitException extends RuntimeException{

    /**
     * 
     */
    private static final long serialVersionUID=-3118076206552282004L;

    public ServerInitException(){
        super();
        // TODO Auto-generated constructor stub
    }

    public ServerInitException(String message,Throwable cause,boolean enableSuppression,boolean writableStackTrace){
        super(message,cause,enableSuppression,writableStackTrace);
        // TODO Auto-generated constructor stub
    }

    public ServerInitException(String message,Throwable cause){
        super(message,cause);
        // TODO Auto-generated constructor stub
    }

    public ServerInitException(String message){
        super(message);
        // TODO Auto-generated constructor stub
    }

    public ServerInitException(Throwable cause){
        super(cause);
        // TODO Auto-generated constructor stub
    }
    
}
