package com.wifiin.common.exception;

/**
 * 为业务异常定义的超类。此类覆盖了fillInStackTrace()确保在类的对象创建时不填充异常栈。增加了status属性作为异常状态码
 * @author Running
 *
 */
public abstract class BusinessException extends RuntimeException{
    /**
     * 
     */
    private static final long serialVersionUID=-6515750637653651621L;
    
    private int status;
    
    public BusinessException(int status){
        this.status=status;
    }
    public BusinessException(int status,String message){
        this(message,null);
        this.status=status;
    }
    public BusinessException(int status,String message,Throwable cause){
        super(message,cause);
        this.status=status;
    }
    
    public BusinessException(){
        super();
        // TODO Auto-generated constructor stub
    }

    public BusinessException(String message,Throwable cause){
        super(message,cause);
        // TODO Auto-generated constructor stub
    }

    public BusinessException(String message){
        super(message);
        // TODO Auto-generated constructor stub
    }

    public BusinessException(Throwable cause){
        super(cause);
        // TODO Auto-generated constructor stub
    }

    @Override
    public Throwable fillInStackTrace(){
        return this;
    }
    
    public int getStatus(){
        return status;
    }
}
