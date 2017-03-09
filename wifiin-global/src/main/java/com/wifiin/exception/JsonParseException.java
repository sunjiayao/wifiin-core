package com.wifiin.exception;

public class JsonParseException extends RuntimeException{

    public JsonParseException(){
        super();
        // TODO Auto-generated constructor stub
    }

    public JsonParseException(String message,Throwable cause,boolean enableSuppression,boolean writableStackTrace){
        super(message,cause,enableSuppression,writableStackTrace);
        // TODO Auto-generated constructor stub
    }

    public JsonParseException(String message,Throwable cause){
        super(message,cause);
        // TODO Auto-generated constructor stub
    }

    public JsonParseException(String message){
        super(message);
        // TODO Auto-generated constructor stub
    }

    public JsonParseException(Throwable cause){
        super(cause);
        // TODO Auto-generated constructor stub
    }

}
