package com.wifiin.loadbalance.strategy;

import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.google.common.collect.Maps;
import com.wifiin.loadbalance.Service;
import com.wifiin.loadbalance.ServiceCollection;
import com.wifiin.loadbalance.Strategy;
import com.wifiin.util.Help;
/**
 * 加权随机策略
 * @author Running
 *
 */
public abstract class AbstractWeightStrategy<SV extends Service,S extends AbstractWeightStrategy<SV,S>> extends WeightTreeStrategy<SV,S> implements Strategy<SV,S>{
    private WeightTreeStrategy<SV,S> strategy;
    public AbstractWeightStrategy(BiFunction<ServiceCollection<SV,S>,TreeMap<Long,Integer>,SV> nextServiceFn){
        super((Function<ServiceCollection<SV,S>,TreeMap<Long,Integer>>)(services)->{
            TreeMap<Long,Integer> tree=Maps.newTreeMap();
            int serviceCount=services.healthyServiceCount();
            long[] weights=new long[serviceCount];
            TreeMap<Integer,Long> weightTree=Maps.newTreeMap();
            for(int i=0;i<serviceCount;i++){
                long weight=services.get(i).weight();
                weights[i]=weight;
            }
            long gcd=Help.gcd(weights);
            long weightSum=0;
            for(int i=0;i<serviceCount;i++){
                long weight=weights[i]/gcd;
                weightSum+=weight;
                weightTree.put(i,weight);
            }
            int i=0;
            for(long w=0;w<weightSum;w++){
                Entry<Integer,Long> weightEntry=weightTree.tailMap(i,true).firstEntry();
                Integer service=weightEntry.getKey();
                tree.put(w,service);
                Long weight=weightEntry.getValue()-1;
                if(weight>0){
                    weightTree.put(service,weight);
                }else{
                    weightTree.remove(service);
                }
                i=(service+1)%serviceCount;
            }
            return tree;
        },nextServiceFn);
    }
    public AbstractWeightStrategy(ServiceCollection<SV,S> services,BiFunction<ServiceCollection<SV,S>,TreeMap<Long,Integer>,SV> nextServiceFn){
        this(nextServiceFn);
        strategy=super.build(services);
    }
    @Override
    public SV next(){
        return strategy.next();
    }
    @Override
    public SV get(String k){
        throw new UnsupportedOperationException("only method next() can be invoked in Weight*Strategy");
    }
}
