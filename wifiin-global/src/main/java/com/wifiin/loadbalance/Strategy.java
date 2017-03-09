package com.wifiin.loadbalance;

public interface Strategy<SV extends Service,S extends Strategy<SV,S>>{
    /**
     * 构造策略数据，可以重复复构造。服务集合发生变化时，ServiceCollection.notify(...)内部会调用此方法重建策略数据
     * @param services
     * @return
     */
    public S build(ServiceCollection<SV,S> services);
    /**
     * 得到一个服务
     * @return
     */
    public SV next();
    
    /**
     * 得到一个服务
     */
    public SV get(String k);
}
