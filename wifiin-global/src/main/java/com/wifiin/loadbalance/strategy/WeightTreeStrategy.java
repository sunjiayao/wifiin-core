package com.wifiin.loadbalance.strategy;

import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.wifiin.loadbalance.Service;
import com.wifiin.loadbalance.ServiceCollection;
import com.wifiin.loadbalance.Strategy;
import com.wifiin.util.function.ThreeParamsFunction;

public class WeightTreeStrategy<SV extends Service,S extends WeightTreeStrategy<SV,S>> implements Strategy<SV,S> {
    /**
     * 所有服务集合
     */
    protected ServiceCollection<SV,S> services;
    /**
     * 权重树，遍历所有健康服务，当前服务权重与比它在健康服务列表中靠前的所有服务权重的和作为key，当前服务在健康服务列表的索引作为value
     */
    protected TreeMap<Long,Integer> weightTree;
    protected Function<ServiceCollection<SV,S>,TreeMap<Long,Integer>> treeBuilder;
    private BiFunction<ServiceCollection<SV,S>,TreeMap<Long,Integer>,SV> nextServiceFn;
    private ThreeParamsFunction<ServiceCollection<SV,S>,TreeMap<Long,Integer>,String,SV> getServiceFn;
    public WeightTreeStrategy(Function<ServiceCollection<SV,S>,TreeMap<Long,Integer>> treeBuilder,
            BiFunction<ServiceCollection<SV,S>,TreeMap<Long,Integer>,SV> nextServiceFn,
            ThreeParamsFunction<ServiceCollection<SV,S>,TreeMap<Long,Integer>,String,SV> getServiceFn){
        this.treeBuilder=treeBuilder;
        this.nextServiceFn=nextServiceFn;
        this.getServiceFn=getServiceFn;
    }
    public WeightTreeStrategy(Function<ServiceCollection<SV,S>,TreeMap<Long,Integer>> treeBuilder,
            BiFunction<ServiceCollection<SV,S>,TreeMap<Long,Integer>,SV> nextServiceFn){
        this.treeBuilder=treeBuilder;
        this.nextServiceFn=nextServiceFn;
    }
    public WeightTreeStrategy(Function<ServiceCollection<SV,S>,TreeMap<Long,Integer>> treeBuilder,
            ThreeParamsFunction<ServiceCollection<SV,S>,TreeMap<Long,Integer>,String,SV> getServiceFn){
        this.treeBuilder=treeBuilder;
        this.getServiceFn=getServiceFn;
    }
    
    /**
     * 构建策略数据，每次调用都会创建新的策略对象，以免在添加或移除服务时引起线程安全问题
     */
    @Override
    public S build(ServiceCollection<SV,S> services){
        S strategy=createInstance();
        strategy.services=services;
        strategy.weightTree=strategy.treeBuilder.apply(services);
        return strategy;
    }
    private S createInstance(){
        return (S)new WeightTreeStrategy<>(treeBuilder,nextServiceFn,getServiceFn);
    }
    @Override
    public SV next(){
        if(nextServiceFn==null){
            throw new UnsupportedOperationException("a key for murmurhash must be specified, method get(K) is required");
        }
        return nextServiceFn.apply(services,weightTree);
    }
    @Override
    public SV get(String k){
        if(getServiceFn==null){
            throw new UnsupportedOperationException("a key for murmurhash must be specified, method next() is supported only");
        }
        return getServiceFn.apply(services,weightTree,k);
    }
}
