package com.wifiin.util.message.exception;

import com.wifiin.util.text.template.TextTemplateFormatterType;

public class TooLargeIntMessageException extends RuntimeException{

    /**
     * 
     */
    private static final long serialVersionUID=933423538329699794L;

    public TooLargeIntMessageException(){
        super();
        // TODO Auto-generated constructor stub
    }
    public TooLargeIntMessageException(int maxLengthBytes){
        this("max bytes of message length is "+maxLengthBytes);
    }
    @SuppressWarnings("unchecked")
    public TooLargeIntMessageException(long value,int maxLengthBytes){
        this(TextTemplateFormatterType.PLAIN_TEXT.formatter("max bytes of message length is {}, however, {} is overflow ").format(new long[]{maxLengthBytes,value}));
    }
    public TooLargeIntMessageException(String message,Throwable cause,boolean enableSuppression,
            boolean writableStackTrace){
        super(message,cause,enableSuppression,writableStackTrace);
        // TODO Auto-generated constructor stub
    }

    public TooLargeIntMessageException(String message,Throwable cause){
        super(message,cause);
        // TODO Auto-generated constructor stub
    }

    public TooLargeIntMessageException(String message){
        super(message);
        // TODO Auto-generated constructor stub
    }

    public TooLargeIntMessageException(Throwable cause){
        super(cause);
        // TODO Auto-generated constructor stub
    }
    
}
