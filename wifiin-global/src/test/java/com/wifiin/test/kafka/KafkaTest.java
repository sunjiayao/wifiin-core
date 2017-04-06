package com.wifiin.test.kafka;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.streams.StreamsConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Maps;
import com.wifiin.config.ConfigManager;
import com.wifiin.kafka.KafkaClient;
import com.wifiin.kafka.KafkaMessageKey;
import com.wifiin.kafka.SerdeFactory;
import com.wifiin.util.Help;

//public class KafkaTest{
//    private final String[] KAFKA_PRODUCER_CONFIG={KafkaClient.KAFKA_PRODUCER_CONFIG,KafkaClient.KAFKA_CLIENT_CONFIG};
//    private final String[] KAFKA_CONSUMER_CONFIG={KafkaClient.KAFKA_CONSUMER_CONFIG,KafkaClient.KAFKA_CLIENT_CONFIG};
//    private final String[] KAFKA_STREAMS_CONFIG={KafkaClient.KAFKA_STREAMS_CONFIG,KafkaClient.KAFKA_CLIENT_CONFIG};
//    private final String TOPIC="test.topic";
//    private final String KEY="test.key";
//    public static class TestMessage{
//        public int i=1;
//        public boolean b=true;
//        public String s="s";
//        public long l=2;
//        public BigInteger bi=BigInteger.TEN;
//        public BigDecimal bd=BigDecimal.TEN;
//        public TestEnum te=TestEnum.TEST;
//        public Date now=new Date();
//        public String toString(){
//            return Help.toString(this);
//        }
//    }
//    public static enum TestEnum{
//        TEST;
//    }
//    /**
//     *  Properties props = new Properties();
// props.put("bootstrap.servers", "localhost:9092");
// props.put("acks", "all");
// props.put("retries", 0);
// props.put("batch.size", 16384);
// props.put("linger.ms", 1);
// props.put("buffer.memory", 33554432);
// props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
// props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
//     * @throws Exception 
//     */
//    @Before
//    public void initGlobalConfig() throws Exception{
//        Map props=Maps.newHashMap();
//        props.put(KafkaClient.BOOTSTRAP_SERVERS,"localhost:9092");//172.16.1.7
//        props.put(KafkaClient.KAFKA_SERDE_AUTO,SerdeFactory.Auto());
//        ConfigManager.getInstance().setDataOrCreateToGlobal(KafkaClient.KAFKA_CLIENT_CONFIG,props);
//        System.out.println("initGlobalConfig:"+ConfigManager.getInstance().getProperties(KafkaClient.KAFKA_CLIENT_CONFIG));
//    }
//    @Before
//    public void initProducerConfig() throws Exception{
//        Map props=Maps.newHashMap();
//        props.put(ProducerConfig.ACKS_CONFIG,"all");
//        props.put(ProducerConfig.RETRIES_CONFIG,0);
//        props.put(ProducerConfig.BATCH_SIZE_CONFIG,16384);
//        props.put(ProducerConfig.LINGER_MS_CONFIG,1);
//        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG,33554432);
//        ConfigManager.getInstance().setDataOrCreate(KafkaClient.KAFKA_PRODUCER_CONFIG,props);
//        System.out.println("initProducerConfig:"+ConfigManager.getInstance().getProperties(KafkaClient.KAFKA_PRODUCER_CONFIG));
//    }
//    /**
//     * Properties props = new Properties();
//     props.put("bootstrap.servers", "localhost:9092");
//     props.put("group.id", "test");
//     props.put("enable.auto.commit", "true");
//     props.put("auto.commit.interval.ms", "1000");
//     * @throws Exception 
//     */
//    @Before
//    public void initConsumerConfig() throws Exception{
//        Map props=Maps.newHashMap();
//        props.put(ConsumerConfig.GROUP_ID_CONFIG,"test");
//        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,true);
//        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG,1000);
//        props.put(KafkaClient.KAFKA_CONSUMER_POLL_TIMEOUT,10000L);
//        ConfigManager.getInstance().setDataOrCreate(KafkaClient.KAFKA_CONSUMER_CONFIG,props);
//        System.out.println("initConsumerConfig:"+ConfigManager.getInstance().getProperties(KafkaClient.KAFKA_CONSUMER_CONFIG));
//    }
//    @Before
//    public void initStreamsConfig() throws Exception{
//        Map props=Maps.newHashMap();
//        props.put(StreamsConfig.APPLICATION_ID_CONFIG,"kafka.streams.test");
//        ConfigManager.getInstance().setDataOrCreate(KafkaClient.KAFKA_STREAMS_CONFIG,props);
//        System.out.println("initStreamsConfig:"+ConfigManager.getInstance().getProperties(KafkaClient.KAFKA_STREAMS_CONFIG));
//    }
//    @Test
//    public void testProducer() throws InterruptedException, ExecutionException{
//        testProducer(TOPIC,new TestMessage());
//    }
//    private void testProducer(String topic,Object value) throws InterruptedException, ExecutionException{
//        Map<String,Object> props=ConfigManager.getInstance().mergeHashMap(KAFKA_PRODUCER_CONFIG);
//        System.out.println("testProducer:"+ConfigManager.getInstance().mergeHashMap(KAFKA_PRODUCER_CONFIG));
//        Future<RecordMetadata> future=KafkaClient.producer(props).send(topic,KEY,value,(RecordMetadata metadata,Exception exception)->{
//            System.out.println("producerCallback:"+Help.toString(metadata));
//            System.out.println("producerCallback:"+exception);
//        });
//        System.out.println("future:"+Help.toString(future.get()));
//    }
//    @SuppressWarnings({"rawtypes", "unchecked"})
//    @Test
//    public void testConsumer(){
//        System.out.println("testConsumer:"+ConfigManager.getInstance().mergeHashMap(KAFKA_CONSUMER_CONFIG));
//        KafkaClient.consumer(TOPIC,KAFKA_CONSUMER_CONFIG).addExecutors((ConsumerRecords<KafkaMessageKey,?> crs,ConsumerRecord<KafkaMessageKey,?> cr)->{
//            System.out.println("consumerExecutor:count:"+crs.count());
//            System.out.println("consumerExecutor:partitions:"+crs.partitions());
////            System.out.println("consumerExecutor:checksum:"+cr.checksum());
//            System.out.println("consumerExecutor:offset:"+cr.offset());
//            System.out.println("consumerExecutor:partition:"+cr.partition());
////            System.out.println("consumerExecutor:timestamp:"+new Date(cr.timestamp()));
//            System.out.println("consumerExecutor:topic:"+cr.topic());
//            System.out.println("consumerExecutor:key:"+cr.key());
////            System.out.println("consumerExecutor:timestampType:"+cr.timestampType());
//            System.out.println("consumerExecutor:value:"+cr.value());
//        }).subscribe().execute();
//        System.out.println("############################");
//        synchronized(KafkaClient.class){
//            try{
//                KafkaClient.class.wait();
//            }catch(InterruptedException e){
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
//    }
//    /**
//     * KafkaStreamsBuilder builder=KafkaStreamsBuilder.newInstance();
//     * builder.builder().stream("my-input-topic").forEach((k,v)->{...});//关键在这里，这里操作的是KStream对象，KStream对象有丰富的api可以实现复杂的流处理逻辑。k,v分别是Producer发布消息时指定的K V。
//     * builder.start();
//     * .....//do sth others
//     * builder.close();
//     * 本类创建的所有对象需要由本类关闭，无法从KafkaClient关闭
//     * @author Running
//     * @throws InterruptedException
//     * @throws ExecutionException
//     */
////    @Test
////    public void testStream() throws InterruptedException, ExecutionException{
//////        testProducer("kafka.streams.topic","kafka.streams.value");
////        System.out.println("testStream:"+ConfigManager.getInstance().mergeProperties(KAFKA_STREAMS_CONFIG));
////        KafkaStreamsBuilder builder=KafkaStreamsBuilder.newInstance();
////        builder.builder().stream(TOPIC).foreach((k,v)->{
////            System.out.println("streams:"+k+"  "+v);
////        });
////        builder.start(KAFKA_STREAMS_CONFIG);
////        synchronized(KafkaClient.class){
////            try{
////                KafkaClient.class.wait();
////            }catch(InterruptedException e){
////                // TODO Auto-generated catch block
////                e.printStackTrace();
////            }
////        }
////    }
//    @After
//    public void delGlobalConfig() throws Exception{
//        ConfigManager.getInstance().deleteFromGlobal(KafkaClient.KAFKA_CLIENT_CONFIG);
//    }
//    @After
//    public void delProducerConfig() throws Exception{
//        ConfigManager.getInstance().delete(KafkaClient.KAFKA_PRODUCER_CONFIG);
//    }
//    @After
//    public void delConsumerConfig() throws Exception{
//        ConfigManager.getInstance().delete(KafkaClient.KAFKA_CONSUMER_CONFIG);
//    }
//    @After
//    public void delStreamsConfig() throws Exception{
//        ConfigManager.getInstance().delete(KafkaClient.KAFKA_STREAMS_CONFIG);
//    }
//}
