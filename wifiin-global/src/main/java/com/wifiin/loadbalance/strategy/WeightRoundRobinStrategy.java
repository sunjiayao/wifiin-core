package com.wifiin.loadbalance.strategy;

import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;

import com.wifiin.loadbalance.Service;
import com.wifiin.loadbalance.ServiceCollection;
import com.wifiin.loadbalance.Strategy;

public class WeightRoundRobinStrategy<SV extends Service> extends AbstractWeightStrategy<SV,WeightRoundRobinStrategy<SV>> implements Strategy<SV,WeightRoundRobinStrategy<SV>>{
    private static final <SV extends Service> BiFunction<ServiceCollection<SV,WeightRoundRobinStrategy<SV>>,TreeMap<Long,Integer>,SV> nextServiceFn(AtomicLong cursor){
        return (sc,weightMap)->{
            return sc.get(weightMap.get(cursor.getAndIncrement()%weightMap.size()));
        };
    }
    private WeightRoundRobinStrategy(ServiceCollection<SV,WeightRoundRobinStrategy<SV>> services,BiFunction<ServiceCollection<SV,WeightRoundRobinStrategy<SV>>,TreeMap<Long,Integer>,SV> nextServiceFn){
        super(services,nextServiceFn);
    }
    private WeightRoundRobinStrategy(BiFunction<ServiceCollection<SV,WeightRoundRobinStrategy<SV>>,TreeMap<Long,Integer>,SV> nextServiceFn){
        super(nextServiceFn);
    }
    public WeightRoundRobinStrategy(){
        super(nextServiceFn(new AtomicLong(0)));
    }
    @Override
    public WeightRoundRobinStrategy<SV> build(ServiceCollection<SV,WeightRoundRobinStrategy<SV>> services){
        return new WeightRoundRobinStrategy<>(services,nextServiceFn(new AtomicLong(0)));
    }
}
