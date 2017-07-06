package com.wifiin.kv.store;

import java.util.function.BiFunction;

public interface Store{
    public void del(byte[] key);
    public void del(byte[] min,byte[] max);
    public void delAllPrefix(byte[] prefix);
    public byte[] get(byte[] key);
    public void put(byte[] key,byte[] value);
    /**
     * 
     * @param key 
     * @param value要保存的字节序
     * @param start 要保存的字节序在value的第一个字节，包含
     * @param end 要保存的字节序在value的最后一个字节，不包含
     * @return 实际保存的字节序列
     */
    public byte[] put(byte[] key,byte[] value,int start,int end);
    /**
     * 遍历每一个key前缀是prefix的键值对。忽略从fn抛出的异常
     * @param prefix
     * @param fn
     */
    public void iterate(byte[] prefix,BiFunction<byte[],byte[],Boolean> fn);
    /**
     * 遍历每一个key前缀是prefix的键值对。
     * @param prefix
     * @param fn
     * @param continueOnThrown 如果从fn抛出异常是否继续
     */
    public void iterate(byte[] prefix,BiFunction<byte[],byte[],Boolean> fn,boolean continueOnThrown);
    /**
     * 遍历每一个key前缀是prefix的键值对。忽略从fn抛出的异常
     * @param min
     * @param max
     * @param fn
     */
    public void iterate(byte[] min,byte[] max,BiFunction<byte[],byte[],Boolean> fn);
    /**
     * 遍历每一个key前缀是prefix的键值对。前闭后闭区间
     * @param min 包含
     * @param max 包含
     * @param fn 针对遍历的每一个键值对执行的逻辑
     * @param continueOnThrown 如果从fn抛出异常是否继续
     */
    public void iterate(byte[] min,byte[] max,BiFunction<byte[],byte[],Boolean> fn,boolean continueOnThrown);
    /**
     * 遍历每一个key前缀是prefix的键值对。前闭后闭区间
     * @param min 
     * @param max 
     * @param fn
     * @param continueOnThrown 如果从fn抛出异常是否继续
     */
    public void reserveIterate(byte[] min,byte[] max,BiFunction<byte[],byte[],Boolean> fn,boolean continueOnThrown);
    /**
     * 返回一个迭代器
     * @param target 
     * @param start true: target是迭代器的开头，false: target是迭代器的结尾
     */
    public StoreIterator iterator(byte[] target,boolean start);
}