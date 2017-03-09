package com.wifiin.data.exception;

public class DataTransformerException extends RuntimeException{

    /**
     * 
     */
    private static final long serialVersionUID = -6028104253493022629L;

    public DataTransformerException(int type) {
        super("Not supported data transformer type:"+type);
        // TODO Auto-generated constructor stub
    }

    public DataTransformerException() {
        super();
        // TODO Auto-generated constructor stub
    }

    public DataTransformerException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        // TODO Auto-generated constructor stub
    }

    public DataTransformerException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    public DataTransformerException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    public DataTransformerException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }
}
