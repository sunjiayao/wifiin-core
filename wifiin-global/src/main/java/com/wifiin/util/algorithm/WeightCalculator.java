package com.wifiin.util.algorithm;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

import org.apache.commons.beanutils.BeanUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Maps;
import com.wifiin.common.GlobalObject;
import com.wifiin.util.Help;
import com.wifiin.util.algorithm.exception.WeightException;
/**
 * 用类似一致性哈希的方式计算权重
 * @author Running
 *
 */
public class WeightCalculator{
    private static final int WEIGHT_SUM_KEY=0;
    private TreeMap<Integer,Weight> weightRing;
    private WeightCalculator(){}
    /**
     * 生成权重计算器对象
     * @param weights 权重数据
     * @return 权重计算器
     */
    public static WeightCalculator generate(List<Weight> weights){
        WeightCalculator calculator=new WeightCalculator();
        calculator.weightRing=convertWeightList2WeigthMap(weights);
        return calculator;
    }
    /**
     * 生成权重计算器对象
     * @param weightJson 权重数据JSON
     * @return 权重计算器
     */
    @SuppressWarnings("unchecked")
    public static WeightCalculator generate(String weightJson,Class<Weight> weightClass){
        WeightCalculator calculator=new WeightCalculator();
        try{
            Map<Number,Map<String,Object>> data=GlobalObject.getJsonMapper().readValue(weightJson,TreeMap.class);
            calculator.weightRing=Maps.newTreeMap();
            for(Map.Entry<Number,Map<String,Object>> entry:data.entrySet()){
                Weight weight=weightClass.newInstance();
                BeanUtils.populate(weight,(Map)(Map)entry.getValue());
                calculator.weightRing.put(entry.getKey().intValue(),weight);
            }
            return calculator;
        }catch(Exception e){
            throw new WeightException(e);
        }
    }
    /**
     * IneVpn的列表转换为以权重为KEY的TreeMap
     * @param vpns
     * @return
     */
    private static TreeMap<Integer,Weight> convertWeightList2WeigthMap(List<Weight> weights){
        int w=0;
        TreeMap<Integer,Weight> map=Maps.newTreeMap();
        for(int i=0,l=weights.size();i<l;i++){
            Weight vpn=weights.get(i);
            w+=vpn.getWeight();
            map.put(w,vpn);
        }
        int weightSum=w;
        map.put(WEIGHT_SUM_KEY,()->{
            return weightSum;
        });
        return map;
    }
    /**
     * 是否只有一个权重数据对象
     * @return
     */
    private boolean onlyOneWeight(){
        return weightRing.size()==2;
    }
    /**
     * 计算空权重对象
     * @return
     */
    private Weight emptyWeight(){
        Integer weightSum=weightSum();
        if(Help.isEmpty(weightRing) || weightRing.size()==1 || weightSum==null || weightSum<1){
            return ()->{return 0;};
        }
        return null;
    }
    /**
     * 得到所有权重值的和
     * @return
     */
    private Integer weightSum(){
        return weightRing.get(WEIGHT_SUM_KEY).getWeight();
    }
    /**
     * 按照权重随机返回一个权重数据对象，如果没有可以返回的权重数据，就返回一个权重值是0的空对象
     * @return
     */
    public Weight random(){
        Integer weightSum=weightSum();
        Weight emptyWeight=this.emptyWeight();
        if(emptyWeight!=null){
            return emptyWeight;
        }
        return onlyOneWeight()?
                   weightRing.lastEntry().getValue()
               :
                   weightRing.tailMap(ThreadLocalRandom.current().nextInt(weightSum),false).firstEntry().getValue();
    }
    /**
     * 按照权重随机返回一个权重数据对象
     * @param until 获取的权重数据对象必须使until.apply(weight)返回true。例外是如果权重数据对象只有一个不论此参数是否返回true都会返回此对象，如果没有权重数据就返回权重值是0的空对象
     * @return 得到的权重数据
     */
    public Weight randomUntil(Function<Weight,Boolean> until){
        Weight w=null;
        do{
            w=random();
        }while(!(onlyOneWeight() || until.apply(w)));
        return w;
    }
    /**
     * 生成权重树的JSON
     */
    public String toString(){
        try{
            return GlobalObject.getJsonMapper().writeValueAsString(weightRing);
        }catch(JsonProcessingException e){
            throw new WeightException(e);
        }
    }
}
