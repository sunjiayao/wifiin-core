package com.wifiin.loadbalance;
/**
 * 服务状态
 * @author Running
 *
 */
public enum ServiceStatus{
    /**
     * 健康
     */
    HEALTHY {
        @Override
        <SV extends Service,S extends Strategy<SV,S>> void notify(ServiceCollection<SV,S> collection,SV service){
            collection.failovers.remove(service);
            super.notify(collection,service);
            collection.healthyServices.add(service);
        }
    },
    /**
     * 故障
     */
    FAILOVER {
        @Override
        <SV extends Service,S extends Strategy<SV,S>> void notify(ServiceCollection<SV,S> collection,SV service){
            collection.healthyServices.remove(service);
            super.notify(collection,service);
            collection.failovers.add(service);
        }
    };
    /**
     * 包内使用的方法。通知指定服务健康状况发生了变化，相应健康状况的覆盖方法会把指定服务摘除或填回健康服务列表
     * @param collection
     * @param service
     */
    <SV extends Service,S extends Strategy<SV,S>> void notify(ServiceCollection<SV,S> collection,SV service){
        service.notify(this);
    }
}
