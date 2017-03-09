package com.wifiin.common;

import java.io.IOException;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.RandomStringUtils;
import org.nustaq.serialization.FSTConfiguration;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.wifiin.config.ConfigManager;
import com.wifiin.util.Help;
import com.wifiin.util.ShutdownHookUtil;

/**
 * 此类包含一些全局对象
 * 不可实例化，不可继承
 * 意义在于，在基于架构中不需要重复创建的且一些基础代码需要使用的对象
 */
public final class GlobalObject {
	private GlobalObject(){}
	
	private volatile static ExecutorService executorService;
	private volatile static ObjectMapper jsonMapper;
	private static final JsonFactory[] EMPTY_JSON_FACTORY_ARRAY=new JsonFactory[0];
	
	/**
	 * 全局ExecutorService对象，对于基础代码中需要用到的线程池可使用此对象
	 * 常量定义文件的key是：properties.dev.project.globalobject.executorservice取值有：正整数 SINGLE CACHE
	 * 分别创建Executors.newFixedThreadPool() Executors.newSingleThreadPool() Executors.newCachedThreadPool();
	 * 默认是CACHE。
	 * @throws IllegalArgumentException
	 */
	public static ExecutorService getExecutorService(){
		if(executorService==null){
			synchronized(ExecutorService.class){
				if(executorService==null){
					String cmd=ConfigManager.getInstance().getString("properties.dev.project.globalobject.executorservice","CACHE");
					if(cmd.equals("CACHE")){
						executorService=Executors.newCachedThreadPool();
					}else if(cmd.equals("SINGLE")){
						executorService=Executors.newSingleThreadExecutor();
					}else if(cmd.matches("^\\d+$")){
						executorService=Executors.newFixedThreadPool(Integer.parseInt(cmd));
					}else{
						throw new IllegalArgumentException("illegal constant defination:"+cmd);
					}
					if(executorService!=null){
					    ShutdownHookUtil.addHook(()->{
					        executorService.shutdown();
					    });
					}
				}
			}
		}
		return executorService;
	}
	
	/**
	 * 通用json解析对象，这是jackson包的入口
	 */
	public static ObjectMapper getJsonMapper(JsonFactory... jsonFactory){
		if(jsonMapper==null){
			synchronized(ObjectMapper.class){
				if(jsonMapper==null){
					if(Help.isEmpty(jsonFactory)){
						jsonMapper=new ObjectMapper();
					}else{
						jsonMapper=new ObjectMapper(jsonFactory[0]);
					}
                    SimpleModule module = new SimpleModule();  
                    module.addSerializer(Instant.class,new JsonSerializer<Instant>(){
                        public void serialize(Instant value,JsonGenerator gen,SerializerProvider serializers) throws IOException,JsonProcessingException{
                            if(value==null){
                                gen.writeNull();
                            }
                            gen.writeNumber(value.toEpochMilli());
                        }
                    });
                    module.addDeserializer(Instant.class,new JsonDeserializer<Instant>(){
                        public Instant deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException,JsonProcessingException{
                            Number value=parser.getNumberValue();
                            if(value==null){
                                return null;
                            }
                            return Instant.ofEpochMilli(value.longValue());
                        }
                        
                    });
                    jsonMapper.registerModule(module); 
				}
			}
		}
		return jsonMapper;
	}
	public static ObjectMapper getJsonMapper(){
	    return getJsonMapper(EMPTY_JSON_FACTORY_ARRAY);
	}
	
	private static MethodHandles.Lookup methodHandlesLookup=null;
	public static MethodHandles.Lookup getMethodHandlesLookup(){
		if(methodHandlesLookup==null){
			synchronized(MethodHandles.Lookup.class){
				if(methodHandlesLookup==null){
					methodHandlesLookup=MethodHandles.lookup();
				}
			}
		}
		return methodHandlesLookup;
	}
	
	private static FSTConfiguration fstConf;
	public static FSTConfiguration getFSTConfiguration(){
	    if(fstConf==null){
	        synchronized(FSTConfiguration.class){
	            if(fstConf==null){
	                fstConf=FSTConfiguration.createJsonNoRefConfiguration();
	            }
	        }
	    }
	    return fstConf;
	}

    public static class FlowConsumed implements Serializable{
        private String orderId=RandomStringUtils.random(32,true,true);
        private long bytes=ThreadLocalRandom.current().nextLong(1024*1024*1024);
        private long incrementalBytes=ThreadLocalRandom.current().nextLong(1024*1024*1024);
        public String getOrderId(){
            return orderId;
        }
        public void setOrderId(String orderId){
            this.orderId=orderId;
        }
        public long getBytes(){
            return bytes;
        }
        public void setBytes(long bytes){
            this.bytes=bytes;
        }
        public long getIncrementalBytes(){
            return incrementalBytes;
        }
        public void setIncrementalBytes(long incrementalBytes){
            this.incrementalBytes=incrementalBytes;
        }
    }
    public static class Test implements Serializable{
        private String deviceId;
        private String mac;
        private String imei;
        private String udid;
        private String openUdid;
        private String uuid;
        private String idfa;
        private int os;
        private String osVersion;
        private String manufacture;
        private String deviceType;
        private String clientVersion;
        private int promoPlatformCode;
        private int loginType;
        private long userId;
        private String token;
        private String time;
        private String verify;
        private String lang;
        private String certification;
        private String sdkPartnerKey;
        private String sdkPartnerUserId;
        public String getDeviceId(){
            return deviceId;
        }
        public void setDeviceId(String deviceId){
            this.deviceId=deviceId;
        }
        public String getMac(){
            return mac;
        }
        public void setMac(String mac){
            this.mac=mac;
        }
        public String getImei(){
            return imei;
        }
        public void setImei(String imei){
            this.imei=imei;
        }
        public String getUdid(){
            return udid;
        }
        public void setUdid(String udid){
            this.udid=udid;
        }
        public String getOpenUdid(){
            return openUdid;
        }
        public void setOpenUdid(String openUdid){
            this.openUdid=openUdid;
        }
        public String getUuid(){
            return uuid;
        }
        public void setUuid(String uuid){
            this.uuid=uuid;
        }
        public String getIdfa(){
            return idfa;
        }
        public void setIdfa(String idfa){
            this.idfa=idfa;
        }
        public int getOs(){
            return os;
        }
        public void setOs(int os){
            this.os=os;
        }
        public String getOsVersion(){
            return osVersion;
        }
        public void setOsVersion(String osVersion){
            this.osVersion=osVersion;
        }
        public String getManufacture(){
            return manufacture;
        }
        public void setManufacture(String manufacture){
            this.manufacture=manufacture;
        }
        public String getDeviceType(){
            return deviceType;
        }
        public void setDeviceType(String deviceType){
            this.deviceType=deviceType;
        }
        public String getClientVersion(){
            return clientVersion;
        }
        public void setClientVersion(String clientVersion){
            this.clientVersion=clientVersion;
        }
        public int getPromoPlatformCode(){
            return promoPlatformCode;
        }
        public void setPromoPlatformCode(int promoPlatformCode){
            this.promoPlatformCode=promoPlatformCode;
        }
        public int getLoginType(){
            return loginType;
        }
        public void setLoginType(int loginType){
            this.loginType=loginType;
        }
        public long getUserId(){
            return userId;
        }
        public void setUserId(long userId){
            this.userId=userId;
        }
        public String getToken(){
            return token;
        }
        public void setToken(String token){
            this.token=token;
        }
        public String getTime(){
            return time;
        }
        public void setTime(String time){
            this.time=time;
        }
        public String getVerify(){
            return verify;
        }
        public void setVerify(String verify){
            this.verify=verify;
        }
        public String getLang(){
            return lang;
        }
        public void setLang(String lang){
            this.lang=lang;
        }
        public String getCertification(){
            return certification;
        }
        public void setCertification(String certification){
            this.certification=certification;
        }
        public String getSdkPartnerKey(){
            return sdkPartnerKey;
        }
        public void setSdkPartnerKey(String sdkPartnerKey){
            this.sdkPartnerKey=sdkPartnerKey;
        }
        public String getSdkPartnerUserId(){
            return sdkPartnerUserId;
        }
        public void setSdkPartnerUserId(String sdkPartnerUserId){
            this.sdkPartnerUserId=sdkPartnerUserId;
        }
        
//        private long userId;
//        private String sessionId;
//        private String token;
//        private String orderId;
//        public long getUserId(){
//            return userId;
//        }
//        public void setUserId(long userId){
//            this.userId=userId;
//        }
//        public String getSessionId(){
//            return sessionId;
//        }
//        public void setSessionId(String sessionId){
//            this.sessionId=sessionId;
//        }
//        public String getToken(){
//            return token;
//        }
//        public void setToken(String token){
//            this.token=token;
//        }
//        public String getOrderId(){
//            return orderId;
//        }
//        public void setOrderId(String orderId){
//            this.orderId=orderId;
//        }
        
        private FlowConsumed consumed;
        public FlowConsumed getConsumed(){
            return consumed;
        }
        public void setConsumed(FlowConsumed consumed){
            this.consumed=consumed;
        }
        
//      private List list=ImmutableList.builder().add(ThreadLocalRandom.current().nextInt(1024*1024))
//          .add(ThreadLocalRandom.current().nextInt(1024*1024))
//          .add(ThreadLocalRandom.current().nextInt(1024*1024))
//          .add(ThreadLocalRandom.current().nextInt(1024*1024))
//          .build();
//      private List getList(){
//          return list;
//      }
//      private Map map=ImmutableMap.builder()
//          .put(RandomStringUtils.random(5,true,false),ThreadLocalRandom.current().nextInt())
//          .put(RandomStringUtils.random(5,true,false),ThreadLocalRandom.current().nextInt())
//          .put(RandomStringUtils.random(5,true,false),ThreadLocalRandom.current().nextInt())
//          .build();
//      public Map getMap(){
//          return map;
//      }
        public Test(){}
        public Test(boolean value){
            if(value){
//                userId=12345678;
//                sessionId=RandomStringUtils.random(32,true,true);
//                token=RandomStringUtils.random(32,true,true);
//                orderId=RandomStringUtils.random(32,true,true);
                
                consumed=new FlowConsumed();
          
              deviceId=RandomStringUtils.random(48,"0123456789abcdef");
              mac=RandomStringUtils.random(12,"0123456789abcdef");
              imei=RandomStringUtils.random(48,"0123456789abcdef");
              udid=RandomStringUtils.random(36,"0123456789abcdef");
              openUdid=RandomStringUtils.random(36,"0123456789abcdef");
              uuid=UUID.randomUUID().toString();
              idfa=RandomStringUtils.random(36,"0123456789abcdef");
              os=3;
              osVersion="10.0.0";
              manufacture="Apple Inc.";
              deviceType="IPhone";
              clientVersion="3.2.8";
              promoPlatformCode=4000090;
              loginType=3;
              userId=ThreadLocalRandom.current().nextInt(99999999);
              token=RandomStringUtils.random(32,"0123456789abcdef");
              time=Help.nowToTxt("yyyyMMddHHmmssSSS");
              verify=RandomStringUtils.random(32,"0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ+/=");
              lang = "CN-zh";
              certification=RandomStringUtils.random(32,"0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ+/=");
              sdkPartnerKey=RandomStringUtils.random(32,true,true);
              sdkPartnerUserId=RandomStringUtils.random(16,"0123456789abcdef");
            }
        }
    }
}
