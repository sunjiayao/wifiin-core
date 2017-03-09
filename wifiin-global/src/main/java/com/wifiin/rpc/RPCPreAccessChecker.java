package com.wifiin.rpc;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * 在执行RPCAccessAOP之前的一些检查，在RPCAccessAOP内部调用本接口的实现
 * 
 * @author Running
 *
 */
public interface RPCPreAccessChecker{
    /**
     * 此方法的返回值决定是否要调用check方法
     * @param point
     * @return
     */
    public boolean needCheck(ProceedingJoinPoint point);
    /**
     * 此方法的返回值可作为rpc调用的status值返回，当且仅当返回值为1时才继续执行本接口的其它实现；当且仅当本接口的所有实现都返回1时才执行RPCAccessAOP的剩余逻辑和真正的RPC业务实现
     * @param point
     * @return
     */
    public int check(ProceedingJoinPoint point);
}
