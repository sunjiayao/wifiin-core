package com.wifiin.loadbalance;
/**
 * 需要计算负载均衡的服务
 * @author Running
 *
 */
public interface Service{
    /**
     * 通知算法当前服务状态
     * @param status 
     */
    public void notify(ServiceStatus status);
    /**
     * 返回当前服务状态，默认是ServiceStatus.HEALTHY
     * @return
     */
    public default ServiceStatus status(){
        return ServiceStatus.HEALTHY;
    }
    /**
     * 返回当前服务的权重
     * @return
     */
    public long weight();
    
    /**
     * 返回当前服务的名字
     */
    public String name();
}
