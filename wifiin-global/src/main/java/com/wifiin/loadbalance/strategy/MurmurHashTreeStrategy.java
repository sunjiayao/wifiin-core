package com.wifiin.loadbalance.strategy;

import java.math.BigInteger;
import java.util.NavigableMap;
import java.util.TreeMap;

import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import com.wifiin.common.CommonConstant;
import com.wifiin.loadbalance.Service;
import com.wifiin.loadbalance.ServiceCollection;
import com.wifiin.loadbalance.Strategy;
import com.wifiin.util.Help;
import com.wifiin.util.string.ThreadLocalStringBuilder;

/**
 * 权重策略抽象
 * @author Running
 *
 */
public class MurmurHashTreeStrategy<SV extends Service> extends WeightTreeStrategy<SV,MurmurHashTreeStrategy<SV>> implements Strategy<SV,MurmurHashTreeStrategy<SV>>{
    private WeightTreeStrategy<SV,MurmurHashTreeStrategy<SV>> strategy;
    public MurmurHashTreeStrategy(){
        super((services)->{
            TreeMap<Long,Integer> tree=Maps.newTreeMap();
            StringBuilder serviceNode=ThreadLocalStringBuilder.builder().append("service-");
            for(int i=0,l=((ServiceCollection<SV,MurmurHashTreeStrategy<SV>>)services).healthyServiceCount();i<l;i++){
                Service service=services.get(i);
                long weight=service.weight();
                if(weight<=0){
                    weight=1;
                }
                weight*=160;//在指定权重的基础上扩容160倍
                String name=Help.convert(service.name(),"");//每次计算一致性哈希的结果必须一致不能使用随机性质的字符串或字符
                serviceNode.append(i).append('-').append(name).append('-');
                for(long n=0;n<weight;n++){
                    serviceNode.append(n).append('-');
                    long murmur=0;
                    for(long c=0;murmur==0 || tree.containsKey(murmur);c++){//如果murmur已在一致性哈希树中存在，就在serviceNode后面增加一个long型数再重算一个新的murmur
                        murmur=Hashing.murmur3_128().hashString(
                                serviceNode.append(c),//不能使用随机数，否则每次计算的一致性哈希都不一样，必须确保每次程序重启后的计算结果与以前的计算结果一致
                                CommonConstant.DEFAULT_CHARSET_INSTANCE).asLong();
                        serviceNode.delete(serviceNode.lastIndexOf("-")+1,serviceNode.length());
                    }
                    tree.put(murmur,i);
                    serviceNode.delete(
                        serviceNode.lastIndexOf("-",serviceNode.lastIndexOf("-")-1)+1,serviceNode.length());
                }
            }
            return tree;
        },(services,weightTree,k)->{
            NavigableMap<Long,Integer> sub=weightTree.tailMap(Hashing.murmur3_128().hashString(k,CommonConstant.DEFAULT_CHARSET_INSTANCE).asLong(),true);
            return services.get((Help.isEmpty(sub)?weightTree:sub).firstEntry().getValue());
        });
    }
    private MurmurHashTreeStrategy(ServiceCollection<SV,MurmurHashTreeStrategy<SV>> services){
        this();
        strategy=super.build(services);
    }
    @Override
    public SV next(){
        throw new UnsupportedOperationException("only method get(K) can be invoked in MurmurHashTreeStrategy");
    }
    @Override
    public SV get(String k){
        return strategy.get(k);
    }
    @SuppressWarnings({"unchecked","rawtypes"})
    @Override
    public MurmurHashTreeStrategy<SV> build(ServiceCollection<SV,MurmurHashTreeStrategy<SV>> services){
        return new MurmurHashTreeStrategy(services);
    }
}
