package com.wifiin.mq.server.exception;

public class MessagePersistanceException extends RuntimeException{

    /**
     * 
     */
    private static final long serialVersionUID=2678403245757029850L;

    public MessagePersistanceException(){
        super();
        // TODO Auto-generated constructor stub
    }

    public MessagePersistanceException(String message,Throwable cause,boolean enableSuppression,
            boolean writableStackTrace){
        super(message,cause,enableSuppression,writableStackTrace);
        // TODO Auto-generated constructor stub
    }

    public MessagePersistanceException(String message,Throwable cause){
        super(message,cause);
        // TODO Auto-generated constructor stub
    }

    public MessagePersistanceException(String message){
        super(message);
        // TODO Auto-generated constructor stub
    }

    public MessagePersistanceException(Throwable cause){
        super(cause);
        // TODO Auto-generated constructor stub
    }
    
}
