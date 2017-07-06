package com.wifiin.mq.server.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.rocksdb.FlushOptions;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;

import com.google.common.hash.Hashing;
import com.wifiin.common.CommonConstant;
import com.wifiin.log.LoggerFactory;
import com.wifiin.mq.server.exception.MessagePersistanceException;
import com.wifiin.mq.server.exception.ServerInitException;
import com.wifiin.util.Help;
import com.wifiin.util.ShutdownHookUtil;
import com.wifiin.util.bytes.ThreadLocalByteArray;
import com.wifiin.util.io.ThreadLocalByteArrayOutputStream;
import com.wifiin.util.net.Localhost;
import com.wifiin.util.process.ProcessUtil;

public class Rocks{
    private static final Logger log=LoggerFactory.getLogger(Rocks.class);
    private static final int SEQUENCE_MASK=0x3fffff;
    private static final String MESSAGE_TAIL=createMessageTail();
    private static final AtomicInteger SEQUENCE_INCREASER=new AtomicInteger(0);
    private static final byte DB_KEY_SPLITOR=(byte)':';
    private static final byte BLANK_DB_KEY_PART=(byte)'-';
    private static final byte[] EMPTY_BYTE_ARRAY=CommonConstant.EMPTY_BYTE_ARRAY;
    private static String generate36RadixDigit(long digit){
        byte[] buf=new byte[8];
        for(int i=7;i>=0;i--){
            buf[i]=(byte)digit;
            digit>>>=8;
        }
        return new BigInteger(1,buf).toString(36);
    }
    private static String createMessageTail(){
        return generate36RadixDigit((((long)ProcessUtil.getPid())<<30)|Localhost.getLocalMacLong());
    }
    private static final byte[] generateMessageId(){
        return (generate36RadixDigit((System.currentTimeMillis()<<22)|(SEQUENCE_INCREASER.getAndIncrement()&SEQUENCE_MASK))+MESSAGE_TAIL).getBytes(CommonConstant.DEFAULT_CHARSET_INSTANCE);
    }
    //messageId:42bit_millis 22bitsequence 16bitpid mac   
    //           6            2             8
    //server client各自生成messageId，发布后，server给client返回server的messageId
    //相同tab的消息保证绝对有序，没有tab的消息不保证有序
    //tab决定接收消息的broker
    //tab决定同一consumer组内接收消息的consumer
    //server_messageId tab key topic -> data
    //topic server_messageId -> tab
    //topic tab server_messageId -> 1
    //topic key tab -> server_messageId //topic key保证惟一性或topic key tab保证惟一性
    //consumer_ip:port topic -> server_messageId  //保存每一个消费者的offset server_messageId
    //server_messageId -> {"ip1:port":CONSUMER_SENT,"ip2:port":CONSUMER_COMP,...} //已完成的消息，
    
    private CombinatedRocksDB[] dbs;
    private class CombinatedRocksDB{
        private RocksDB data;
        private RocksDB tab;
        private RocksDB empty;
        private RocksDB messageId;
        private RocksDB consumerKey;
        private RocksDB consumed;
        public CombinatedRocksDB(Options options,String path) throws RocksDBException{
            data=open(options,path,"data");
            tab=open(options,path,"tab");
            empty=open(options,path,"one");
            messageId=open(options,path,"messageId");
            consumerKey=open(options,path,"consumerKey");
            consumed=open(options,path,"consumed");
        }
        private RocksDB open(Options options,String path,String part) throws RocksDBException{
            return RocksDB.open(options,path+"/"+part);
        }
        public void close() throws RocksDBException{
            FlushOptions fo=new FlushOptions().setWaitForFlush(true);
            if(close(fo,data)){
                data=null;
                log.info("RocksDB.closeed:data");
            }
            if(close(fo,tab)){
                tab=null;
                log.info("RocksDB.closeed:tab");
            }
            if(close(fo,empty)){
                empty=null;
                log.info("RocksDB.closeed:one");
            }
            if(close(fo,messageId)){
                messageId=null;
                log.info("RocksDB.closeed:messageId");
            }
            if(close(fo,consumerKey)){
                consumerKey=null;
                log.info("RocksDB.closeed:consumerKey");
            }
            if(close(fo,consumed)){
                consumed=null;
                log.info("RocksDB.closeed:consumed");
            }
        }
        private boolean close(FlushOptions options,RocksDB db){
            if(db!=null){
                try{
                    db.flush(options);
                    db.close();
                    return true;
                }catch(RocksDBException e){
                    log.warn("CombinatedRocksDB.close:",e);
                }
            }
            return false;
        }
        public byte[] write(byte[] topic,byte[] tab,byte[] key,byte[] message){
            byte[] messageId=generateMessageId();
            writeData(messageId,topic,tab,key,message);
            writeTab(messageId,topic,tab);
            writeEmpty(messageId,topic,tab);
            writeMessageId(messageId,topic,tab,key);
            return messageId;
        }
        private void writeMessageId(byte[] messageId,byte[] topic,byte[] tab,byte[] key){
            if(Help.isNotEmpty(tab) && Help.isNotEmpty(tab)){
                ByteArrayOutputStream buf=ThreadLocalByteArrayOutputStream.stream();
                try{
                    buf.write(topic);
                    buf.write(DB_KEY_SPLITOR);
                    buf.write(key);
                    buf.write(DB_KEY_SPLITOR);
                    buf.write(tab);
                    this.messageId.put(buf.toByteArray(),messageId);
                }catch(IOException | RocksDBException e){
                    throw new MessagePersistanceException(e);
                }
            }
        }
        private void writeEmpty(byte[] messageId,byte[] topic,byte[] tab){
            if(Help.isNotEmpty(tab)){
                ByteArrayOutputStream buf=ThreadLocalByteArrayOutputStream.stream();
                try{
                    buf.write(topic);
                    buf.write(DB_KEY_SPLITOR);
                    buf.write(tab);
                    buf.write(DB_KEY_SPLITOR);
                    buf.write(messageId);
                    this.empty.put(buf.toByteArray(),EMPTY_BYTE_ARRAY);
                }catch(IOException | RocksDBException e){
                    throw new MessagePersistanceException(e);
                }
            }
        }
        private void writeTab(byte[] messageId,byte[] topic,byte[] tab){
            if(Help.isNotEmpty(tab)){
                ByteArrayOutputStream buf=ThreadLocalByteArrayOutputStream.stream();
                try{
                    buf.write(topic);
                    buf.write(DB_KEY_SPLITOR);
                    buf.write(messageId);
                    this.tab.put(buf.toByteArray(),tab);
                }catch(IOException | RocksDBException e){
                    throw new MessagePersistanceException(e);
                }
            }
        }
        private void writeData(byte[] messageId,byte[] topic,byte[] tab,byte[] key,byte[] message){
            if(Help.isNotEmpty(tab)){
                tab=new BigInteger(1,tab).toString(36).getBytes(CommonConstant.DEFAULT_CHARSET_INSTANCE);
            }
            if(Help.isNotEmpty(key)){
                key=new BigInteger(1,key).toString(36).getBytes(CommonConstant.DEFAULT_CHARSET_INSTANCE);
            }
            ByteArrayOutputStream buf=ThreadLocalByteArrayOutputStream.stream();
            try{
                buf.write(messageId);
                buf.write(DB_KEY_SPLITOR);
                generateDbKey(buf,tab);
                buf.write(DB_KEY_SPLITOR);
                generateDbKey(buf,key);
                buf.write(DB_KEY_SPLITOR);
                buf.write(topic);
            }catch(IOException e){}
            try{
                data.put(buf.toByteArray(),message);
            }catch(RocksDBException e){
                throw new MessagePersistanceException(e);
            }
        }
        private void generateDbKey(ByteArrayOutputStream buf,byte[] part){
            if(Help.isNotEmpty(part)){
                try{
                    buf.write(part);
                }catch(IOException e){}
            }else{
                buf.write(BLANK_DB_KEY_PART);
            }
        }
    }
    public Rocks(Options options,String[] paths) {
        dbs=(CombinatedRocksDB[])Arrays.stream(paths).map((p)->{
            try{
                return new CombinatedRocksDB(options,p);
            }catch(RocksDBException e){
                throw new ServerInitException(e);
            }
        }).toArray();
        ShutdownHookUtil.addHook(this::close);
    }
    public void close(){
        Arrays.stream(dbs).forEach((db)->{
            try{
                db.close();
            }catch(Exception e){
                log.warn("RocksDB.close:",e);
            }
        });
    }
    public byte[] write(String topic,byte[] tab,byte[] key,byte[] message){
        byte[] hasher=null;
        byte[] topicBytes=topic.getBytes(CommonConstant.DEFAULT_CHARSET_INSTANCE);
        if(Help.isNotEmpty(tab)){
            hasher=tab;
        }else if(Help.isNotEmpty(key)){
            hasher=key;
        }else{
            hasher=topicBytes;
        }
        int idx=Hashing.crc32c().hashBytes(hasher).asInt()%dbs.length;
        return dbs[idx].write(topicBytes,tab,key,message);
    }
}
