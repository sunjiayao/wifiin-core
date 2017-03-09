package com.wifiin.loadbalance;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
/**
 * 所有服务集合
 * @author Running
 *
 */
public class ServiceCollection<SV extends Service,S extends Strategy<SV,S>> implements Iterable<SV>,Strategy<SV,S>{
    /**
     * 故障服务集合，从可用服务中摘除
     */
    Set<SV> failovers;
    /**
     * 健康服务服务列表
     */
    List<SV> healthyServices;
    /**
     * 服务选择策略
     */
    private S strategy;
    /**
     * @param services  所有服务列表
     * @param strategy 服务策略
     */
    public ServiceCollection(List<SV> services,Strategy<SV,S> strategy){
        healthyServices=Lists.newCopyOnWriteArrayList();
        healthyServices.addAll(services);
        failovers=Sets.newConcurrentHashSet();
        this.strategy=strategy.build(this);
    }
    /**
     * 通知服务健康状况发生了变化
     * @param service 服务健康状况发生变化的服务
     * @param status 指定服务的当前健康状况
     */
    public void notify(SV service,ServiceStatus status){
        status.notify(this,service);
        strategy=strategy.build(this);
    }
    @Override
    public S build(ServiceCollection<SV,S> services){
        return strategy=strategy.build(this);
    }
    @Override
    public SV next(){
        return strategy.next();
    }
    @Override
    public SV get(String k){
        return strategy.get(k);
    }
    public <R> R execute(Function<SV,R> fn){
        return fn.apply(next());
    }
    public <R> R execute(String k,BiFunction<String,SV,R> fn){
        return fn.apply(k,get(k));
    }
    /**
     * 健康服务迭代器
     */
    @Override
    public Iterator<SV> iterator(){
        return healthyServices.iterator();
    }
    /**
     * 返回可用服务数量
     * @return
     */
    public int healthyServiceCount(){
        return healthyServices.size();
    }
    /**
     * 返回服务列表中的第i个服务，i>=0 && i<healthyServiceCount();
     * @param i
     * @return
     */
    public SV get(int i){
        return healthyServices.get(i);
    }
}
