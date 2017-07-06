package com.wifiin.test.bean;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.Maps;
import com.wifiin.common.JSON;
import com.wifiin.model.builder.ModelBuilder;
import com.wifiin.reflect.BeanUtil;

public class UserBaseInfoTest{
    public static enum Gender {
        UNKNOWN(0),
        MALE(1),
        FEMALE(2);
        
        private static final Map<Integer,Gender> GENDER_MAP=Maps.newHashMap();
        static{
            for(Gender gender:Gender.values()){
                GENDER_MAP.put(gender.value,gender);
            }
        }
        
        private Gender(int value){
            this.value=value;
        }
        private int value;
        public int value(){
            return value;
        }
        public static Gender valueOf(int value){
            return GENDER_MAP.get(value);
        }
    }

public static class UserRegistryData implements Serializable{
    /**
     * 
     */
    private static final long serialVersionUID=4715142670166829649L;
    private String username;
    private String password;
    private String nickname;
    private String smsCode;
    private LoginType loginType;
    private String app;
    private String realName;
    private Gender gender;
    private Date birthday;
    private String registerIp;
    public static class UserRegistryDataBuilder extends ModelBuilder<UserRegistryData>{
        private UserRegistryDataBuilder(){}
        private UserRegistryData data=new UserRegistryData();
        public UserRegistryData build(){
            super.build();
            return data;
        }
        
        public void setUsername(String username){
            built();
            data.username=username;
        }
        public void setPassword(String password){
            built();
            data.password=password;
        }
        public void setNickname(String nickname){
            built();
            data.nickname=nickname;
        }
        public void setSmsCode(String smsCode){
            built();
            data.smsCode=smsCode;
        }
        public void setLoginType(LoginType loginType){
            built();
            data.loginType=loginType;
        }
        public void setApp(String app){
            built();
            data.app=app;
        }
        public void setRealName(String realName){
            built();
            data.realName=realName;
        }
        public void setGender(Gender gender){
            built();
            data.gender=gender;
        }
        public void setBirthday(Date birthday){
            built();
            data.birthday=birthday;
        }
        public void setRegisterIp(String registerIp){
            built();
            data.registerIp=registerIp;
        }
    }
    public static UserRegistryDataBuilder builder(){
        return new UserRegistryDataBuilder();
    }
    public static UserRegistryDataBuilder builder(UserRegistryData data){
        UserRegistryDataBuilder builder=builder();
        BeanUtil.populate(data,builder,false,false,true);
        return builder;
    }
    private UserRegistryData(){}
    public UserRegistryData(String username,String password,String nickname,String smsCode,LoginType loginType,String app){
        this.username=username;
        this.password=password;
        this.nickname=nickname;
        this.smsCode=smsCode;
        this.loginType=loginType;
        this.app=app;
    }
    public String getUsername(){
        return username;
    }
    public String getPassword(){
        return password;
    }
    public String getNickname(){
        return nickname;
    }
    public String getSmsCode(){
        return smsCode;
    }
    public LoginType getLoginType(){
        return loginType;
    }
    public String getApp(){
        return app;
    }
    public String getRealName(){
        return realName;
    }
    public Gender getGender(){
        return gender;
    }
    public Date getBirthday(){
        return birthday;
    }
    public String getRegisterIp(){
        return registerIp;
    }
}

public static class UserBaseInfo implements Serializable{
    /**
     * 
     */
    private static final long serialVersionUID=-8920734088948824117L;
    private long id;
    private String realName;
    private int gender;
    private Date birthday;
    private String nickname;
    private String password;
    private String registerIp;
    private boolean lock;
    private Date saveTime;
    private Date firstLoginTime;
    private Date lastLoginTime;
    private Date uptime;
    public UserBaseInfo(){}
    public long getId(){
        return id;
    }
    public void setId(long id){
        this.id=id;
    }
    public String getRealName(){
        return realName==null?"":realName;
    }
    public void setRealName(String realName){
        this.realName=realName;
    }
    public int getGender(){
        return gender;
    }
    public void setGender(int gender){
        this.gender=gender;
    }
    public Date getBirthday(){
        return birthday==null?new Date(LocalDate.of(0,1,1).toEpochDay()*86400_000):birthday;
    }
    public void setBirthday(Date birthday){
        this.birthday=birthday;
    }
    public String getNickname(){
        return nickname==null?"":nickname;
    }
    public void setNickname(String nickname){
        this.nickname=nickname;
    }
    public String getPassword(){
        return password;
    }
    public void setPassword(String password){
        this.password=password;
    }
    public String getRegisterIp(){
        return registerIp;
    }
    public void setRegisterIp(String registerIp){
        this.registerIp=registerIp;
    }
    public boolean getLock(){
        return lock;
    }
    public void setLock(boolean lock){
        this.lock=lock;
    }
    public Date getSaveTime(){
        return saveTime;
    }
    public void setSaveTime(Date saveTime){
        this.saveTime=saveTime;
    }
    public Date getFirstLoginTime(){
        return firstLoginTime;
    }
    public void setFirstLoginTime(Date firstLoginTime){
        this.firstLoginTime=firstLoginTime;
    }
    public Date getLastLoginTime(){
        return lastLoginTime;
    }
    public void setLastLoginTime(Date lastLoginTime){
        this.lastLoginTime=lastLoginTime;
    }
    public Date getUptime(){
        return uptime;
    }
    public void setUptime(Date uptime){
        this.uptime=uptime;
    }
}

public static enum LoginType{
    UNKNOWN(0),
    WEIBO(1),
    QQ(2),
    PHONE(3),
    EMAIL(4),
    WEIXIN(5);
    private static final Map<Integer,LoginType> LOGIN_TYPE_MAP=Maps.newHashMap();
    static{
        for(LoginType loginType:LoginType.values()){
            LOGIN_TYPE_MAP.put(loginType.value,loginType);
        }
    }
    private LoginType(int value){
        this.value=value;
    }
    private int value;
    public int value(){
        return value;
    }
    public static LoginType valueOf(int value){
        return LOGIN_TYPE_MAP.get(value);
    }
}

    @Test
    public void testPopulate(){
        //regist.getOpenId(),regist.getPassword(),"","",LoginType.EMAIL,SpeedinConstant.APP_NAME
        UserRegistryData data=new UserRegistryData("jingrun.wu@wifiin.com","aaaaaa","","",LoginType.EMAIL,"test");
        System.out.println(JSON.toJSON(BeanUtil.populate(data,UserBaseInfo.class,false,false,true)));
    }
}
