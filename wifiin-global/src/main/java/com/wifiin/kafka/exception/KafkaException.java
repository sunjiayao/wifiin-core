package com.wifiin.kafka.exception;

public class KafkaException extends RuntimeException{
    /**
     * 
     */
    private static final long serialVersionUID = -6228207140394566432L;

    public KafkaException() {
        super();
        // TODO Auto-generated constructor stub
    }

    public KafkaException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        // TODO Auto-generated constructor stub
    }

    public KafkaException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    public KafkaException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    public KafkaException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

}
