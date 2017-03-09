package com.wifiin.util.string;
/**
 * 每个线程一个StringBuilder。减少StringBuilder对象创建和扩容的频率。每次需要StringBuilder时只需要调用builder()方法。
 * 如果一个线程或一个方法内要使用两次ThreadLocalStringBuilder.builder()，请确保第一个StringBuilder先toString()再调用第二次。
 * @author Running
 *
 */
public class ThreadLocalStringBuilder{
    private static final ThreadLocal<StringBuilder> BUILDER=new ThreadLocal<StringBuilder>();
    /**
     * 返回一个StringBuilder对象。清空StringBuilder内容，容量不变。
     * @see ThreadLocalStringBuilder.builder(int)
     * @return
     */
    public static StringBuilder builder(){
        return builder(0);
    }
    /**
     * 返回一个指定初始容量的StringBuilder，重复调用本方法不会重置StringBuilder容量。
     * 方法返回前会调用@see java.lang.StringBuilder.delete(int,int)清空上次对StringBuilder的修改。
     * 由于@see java.lang.StringBuilder.delete(int,int)不会创建新对象，这个行为不会产生垃圾。
     * 清空StringBuilder内容，容量不变。
     * @param capacity
     * @return
     */
    public static StringBuilder builder(int capacity){
        return builder(capacity,true);
    }
    /**
     * 返回一个StringBuilder对象。
     * 不清空之前填充的内容。
     * @return
     */
    public static StringBuilder builderWithoutClear(){
        return builderWithoutClear(0);
    }
    /**
     * 返回一个指定初始容量的StringBuilder对象。重复调用本方法不会重置StringBuilder的容量
     * 不清空之前填充的内容。
     * @return
     */
    public static StringBuilder builderWithoutClear(int capacity){
        return builder(capacity,false);
    }
    /**
     * 返回指定初始容量的StringBuilder，重复调用本方法不会重置StringBuilder容量
     * @param capacity StringBuilder的初始容量
     * @param clear 是否清空之前的填充
     * @return
     */
    public static StringBuilder builder(int capacity,boolean clear){
        StringBuilder builder=BUILDER.get();
        if(builder==null){
            if(capacity>0){
                builder=new StringBuilder(capacity);
            }else{
                builder=new StringBuilder();
            }
            BUILDER.set(builder);
        }
        return clear?clear(builder):builder;
    }
    /**
     * 清空指定StringBuilder的内容，容量不变
     * @param builder
     * @return
     */
    private static StringBuilder clear(StringBuilder builder){
        return builder.delete(0,builder.length());
    }
}
