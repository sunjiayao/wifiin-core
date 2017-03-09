package com.wifiin.common;

/**
 * 待执行的任务
 * 作为ExecutionAwait的回调接口
 * */
public interface Task{
	public <T> T execute()throws Throwable;
}
