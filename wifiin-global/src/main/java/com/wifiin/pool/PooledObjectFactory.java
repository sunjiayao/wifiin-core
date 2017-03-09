package com.wifiin.pool;

/**
 * 池对象工厂
 * @author Running
 *
 * @param <T>
 */
public interface PooledObjectFactory<T>{
    public default PooledObject<T> createObject(){
        return new PooledObject<T>(create(),this);
    }
    /**
     * 创建新对象
     * @return
     */
    public T create();
    /**
     * 验证对象是否有效
     * @param o
     * @return true:有效，false:无效
     */
    public boolean validate(T o);
    /**
     * 销毁池对象，然后从池移除
     * @param o
     */
    public void destroy(T o);
    /**
     * 从池借出前调用，做一些开始使用池对象前的准备工作
     * @param o
     */
    public T activate(T o);
    /**
     * 返回池前调用，做一些回池前的工作
     * @param o
     */
    public T deactivate(T o);
    /**
     * 指定池配置参数
     * @param config
     */
    public void setPoolConfig(PoolConfig<T> config);
}
