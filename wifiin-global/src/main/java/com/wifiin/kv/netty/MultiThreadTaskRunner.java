package com.wifiin.kv.netty;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;

import com.google.common.collect.Queues;
import com.wifiin.kv.DataType;
import com.wifiin.kv.Result;
import com.wifiin.kv.ResultSink;
import com.wifiin.kv.ResultSinkMap;
import com.wifiin.kv.command.SystemCommand;
import com.wifiin.kv.exception.KVTaskPutException;
import com.wifiin.kv.exception.TaskRunnerException;
import com.wifiin.kv.store.Store;
import com.wifiin.util.ShutdownHookUtil;

public class MultiThreadTaskRunner{
    private final int qSize=0xff;
    private Store store;
    private SynchronousQueue<Task>[] queues;
    private ExecutorService executor;
    private AtomicBoolean running=new AtomicBoolean(true);
    
    @SuppressWarnings("unchecked")
    public MultiThreadTaskRunner(Store store,int threads){
        this.store=store;
        this.queues=new SynchronousQueue[threads];
        executor=Executors.newFixedThreadPool(threads+qSize);
        for(int i=0;i<qSize;i++){
            SynchronousQueue<Task> q=Queues.newSynchronousQueue();
            queues[i]=q;
            executor.submit(()->{
                for(;;){
                    q.take().execute();
                }
            });
        }
        ShutdownHookUtil.addHook(()->{
            running.set(false);
            executor.shutdown();
        });
        asyncIterate();
    }
    private void asyncIterate(){
        for(int i=0;i<qSize;i++){
            final int qidx=i;
            executor.submit(()->{
                byte[] min=new byte[]{(byte)DataType.SYSTEM.value(),(byte)SystemCommand.CMD.value(),(byte)qidx};
                AtomicBoolean recursive=new AtomicBoolean(false);
                for(;running.get();){
                    recursive.set(false);
                    store.iterate(min,new BiFunction<byte[],byte[],Boolean>(){
                        @Override
                        public Boolean apply(byte[] k,byte[] v){
                            //millis uuid key 
                            int idx=3+k[2];
                            byte[] uuid=new byte[16];
                            System.arraycopy(k,idx,uuid,0,uuid.length);
                            idx+=16;
                            byte[] key=new byte[k.length-idx];
                            System.arraycopy(k,idx,key,0,key.length);
                            byte dataType=v[0];
                            byte command=v[1];
                            byte[] params=new byte[v.length-2];
                            System.arraycopy(v,2,params,0,params.length);
                            push(false,uuid,dataType,command,params);
                            store.del(k);//如果命令执行完再删除，删除时出异常或进程挂了，就会在重启后重复执行。
                            if(!recursive.get()){
                                recursive.set(true);
                                store.iterate(min,k,this,false,true);
                                recursive.set(false);
                            }
                            return true;
                        }
                    },true);
                    synchronized(MultiThreadTaskRunner.this){
                        try{
                            MultiThreadTaskRunner.this.wait(0,100);
                        }catch(InterruptedException e){}
                    }
                }
            });
        }
    }
    public void execute(byte[] uuid,byte dataType,byte command,byte[] key,byte... params){
        boolean offer=push(true,uuid,dataType,command,key,params);
        if(!offer){
            byte[] p=new byte[2+params.length];
            p[0]=dataType;
            p[1]=command;
            System.arraycopy(params,0,p,2,params.length);
            SystemCommand.CMD.execute(uuid,store,key,p);
            synchronized(MultiThreadTaskRunner.this){
                MultiThreadTaskRunner.this.notify();
            }
        }
    }
    private boolean push(boolean offerOrPut,byte[] uuid,byte dataType,byte command,byte[] key,byte... params){
        SynchronousQueue<Task> q=queues[Arrays.hashCode(key)%queues.length];
        Task task=new Task(()->{
            SystemCommand.TYPE.execute(null,store,key,new byte[]{dataType,command});
            return DataType.valueOf(dataType).command(command).execute(uuid,store,key,params);
        },ResultSinkMap.get(uuid));
        if(offerOrPut){
            return q.offer(task);
        }else{
            try{
                q.put(task);
                return true;
            }catch(InterruptedException e){
                throw new KVTaskPutException(e);
            }
        }
    }
    private class Task{
        private Callable<Result> callable;
        private ResultSink sink;
        
        public Task(Callable<Result> callable,ResultSink sink){
            this.callable=callable;
            this.sink=sink;
        }
        
        public void execute(){
            try{
                sink.sink(callable.call());
            }catch(Exception e){
                throw new TaskRunnerException(e);
            }
        }
    }
}
