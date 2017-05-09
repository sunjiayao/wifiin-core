package com.wifiin.test.bean;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class IneFlowOrder implements Serializable{
    /**
     * 
     */
    private static final long serialVersionUID=6557141622948465476L;
    private long id;
    private long userId;
    private long partnerId;
    private String partnerKey;
    private String phone;
    private String orderId;
    private long packageId;
    private String packageCode;
    private String callback;
    private int orderCount;
    private long remainder;
    private long consumed;
    private int orderStatus;
    private Date madeTime;
    private Date editTime;
    private Date startTime;
    private Date endTime;
    private Date statusChangeTime;
    private String transno;
    private String packageName;
    private long accumulatedFlow;
    
    public IneFlowOrder(){}
    
    public long getId(){
        return id;
    }
    public void setId(long id){
        this.id=id;
    }
    public long getUserId(){
        return userId;
    }
    public void setUserId(long userId){
        this.userId=userId;
    }
    public long getPartnerId(){
        return partnerId;
    }
    public void setPartnerId(long partnerId){
        this.partnerId=partnerId;
    }
    public String getPartnerKey(){
        return partnerKey;
    }
    public void setPartnerKey(String partnerKey){
        this.partnerKey=partnerKey;
    }
    public String getPhone(){
        return phone;
    }
    public void setPhone(String phone){
        this.phone=phone;
    }
    public String getOrderId(){
        return orderId;
    }
    public void setOrderId(String orderId){
        this.orderId=orderId;
    }
    public long getPackageId(){
        return packageId;
    }
    public void setPackageId(long packageId){
        this.packageId=packageId;
    }
    public String getCallback(){
        return callback;
    }
    public void setCallback(String callback){
        this.callback=callback;
    }
    public int getOrderCount(){
        return orderCount;
    }
    public void setOrderCount(int orderCount){
        this.orderCount=orderCount;
    }
    public long getRemainder(){
        return remainder;
    }
    public void setRemainder(long remainder){
        this.remainder=remainder;
    }
    public long getConsumed(){
        return consumed;
    }
    public void setConsumed(long consumed){
        this.consumed=consumed;
    }
    public int getOrderStatus(){
        return orderStatus;
    }
    public void setOrderStatus(int orderStatus){
        this.orderStatus=orderStatus;
    }
    public Date getMadeTime(){
        return madeTime;
    }
    public void setMadeTime(Date madeTime){
        this.madeTime=madeTime;
    }
    public Date getEditTime(){
        return editTime;
    }
    public void setEditTime(Date editTime){
        this.editTime=editTime;
    }
    public Date getStartTime(){
        return startTime;
    }
    public void setStartTime(Date startTime){
        this.startTime=startTime;
    }
    public Date getEndTime(){
        return endTime;
    }
    public void setEndTime(Date endTime){
        this.endTime=endTime;
    }
    public Date getStatusChangeTime(){
        return statusChangeTime;
    }
    public void setStatusChangeTime(Date statusChangeTime){
        this.statusChangeTime=statusChangeTime;
    }
    public String getTransno(){
        return transno;
    }

    public void setTransno(String transno){
        this.transno=transno;
    }

    public int hashCode(){
        return (int)id^((int)(id>>>32));
    }
    public boolean equals(Object o){
        if(o instanceof IneFlowOrder){
            return id==((IneFlowOrder)o).id;
        }
        return false;
    }

    public void setPackageName(String packageName){
        this.packageName=packageName;
    }
    public String getPackageName(){
        return packageName;
    }
    @JsonIgnore
    public long getTotal(){
        return remainder-consumed;
    }

    public void increaseConsumed(long consumed){
        this.consumed+=consumed;
    }

    public long getAccumulatedFlow(){
        return accumulatedFlow;
    }

    public void setAccumulatedFlow(long accumulatedFlow){
        this.accumulatedFlow=accumulatedFlow;
    }

    public String getPackageCode(){
        return packageCode;
    }

    public void setPackageCode(String packageCode){
        this.packageCode=packageCode;
    }
}
