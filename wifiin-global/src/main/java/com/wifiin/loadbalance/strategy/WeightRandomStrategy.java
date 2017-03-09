package com.wifiin.loadbalance.strategy;

import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;

import com.wifiin.loadbalance.Service;
import com.wifiin.loadbalance.ServiceCollection;
import com.wifiin.loadbalance.Strategy;

public class WeightRandomStrategy<SV extends Service> extends AbstractWeightStrategy<SV,WeightRandomStrategy<SV>> implements Strategy<SV,WeightRandomStrategy<SV>>{
    private static <SV extends Service> BiFunction<ServiceCollection<SV,WeightRandomStrategy<SV>>,TreeMap<Long,Integer>,SV> nextServiceFn(){
        return (services,weightMap)->{
            int size=weightMap.size();
            long w=ThreadLocalRandom.current().nextLong(size);
            int idx=weightMap.get(w);
            SV sv=services.get(idx);
            return sv;
        };
    }
    public WeightRandomStrategy(){
        this(nextServiceFn());
    }
    private WeightRandomStrategy(BiFunction<ServiceCollection<SV,WeightRandomStrategy<SV>>,TreeMap<Long,Integer>,SV> nextServiceFn){
        super((BiFunction<ServiceCollection<SV,WeightRandomStrategy<SV>>,TreeMap<Long,Integer>,SV>)nextServiceFn);
    }
    private WeightRandomStrategy(ServiceCollection<SV,WeightRandomStrategy<SV>> services){
        super(services,nextServiceFn());
    }
    @Override
    public WeightRandomStrategy<SV> build(ServiceCollection<SV,WeightRandomStrategy<SV>> services){
        return new WeightRandomStrategy<>(services);
    }
    
}
