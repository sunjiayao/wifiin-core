package com.wifiin.common.query.cellphone;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import com.google.common.collect.Maps;
import com.wifiin.common.query.RemoteQuery;
import com.wifiin.common.query.exception.QueryException;
/**
 * cellphone library classpath:phone.dat  is from https://github.com/lovedboy/phone
 * @author Running
 *
 */
public class CellPhoneLocalLibrary implements RemoteQuery{
    @Override
    public String getURL(String phone){
        // useless
        return null;
    }
    
    public String request(String phone) throws KeyManagementException,NoSuchAlgorithmException,IOException{
        return phone;
    }
    
    @Override
    public String[] parseResponse(String response) throws Exception{
        return lookup(response);
    }
    
    private static String[] numberType={null,"移动","联通","电信","电信虚拟运营商","联通虚拟运营商","移动虚拟运营商"};
    private static final int INDEX_SEGMENT_LENGTH=9;
    private static final Map<Integer,PhoneNumberInfo> cellPhoneMap=Maps.newConcurrentMap();
    public CellPhoneLocalLibrary(BiConsumer<Integer,PhoneNumberInfo> fn){
        ByteArrayOutputStream byteData=new ByteArrayOutputStream();
        byte[] buffer=new byte[1024];
        int readBytesLength;
        try{
            InputStream inputStream=CellPhoneLocalLibrary.class.getResourceAsStream("/phone.dat");
            while((readBytesLength=inputStream.read(buffer)) != -1){
                byteData.write(buffer,0,readBytesLength);
            }
            inputStream.close();
        }catch(Exception e){
            throw new QueryException(e);
        }
        forEach(byteData.toByteArray(),fn);
    }
    public CellPhoneLocalLibrary(){
        this((pp,pi)->{
            cellPhoneMap.put(pp,pi);
        });
    }
    public void forEach(byte[] dataByteArray,BiConsumer<Integer,PhoneNumberInfo> fn){
        ByteBuffer byteBuffer=ByteBuffer.wrap(dataByteArray);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        int dataVersion=byteBuffer.getInt();
        int indexAreaOffset=byteBuffer.getInt();
        int phoneRecordCount=(dataByteArray.length - indexAreaOffset) / INDEX_SEGMENT_LENGTH;
        Pattern splitor=Pattern.compile("\\|");
        for(int i=0;i<phoneRecordCount;i++){
            int currentOffset=indexAreaOffset + i * INDEX_SEGMENT_LENGTH;
            if(currentOffset >= dataByteArray.length){
                return;
            }
            byteBuffer.position(currentOffset);
            int currentPrefix=byteBuffer.getInt();
            int infoBeginOffset=byteBuffer.getInt();
            int phoneType=byteBuffer.get();
            
            int infoLength=-1;
            for(int j=infoBeginOffset;j < indexAreaOffset;++j){
                if(dataByteArray[j] == 0){
                    infoLength=j - infoBeginOffset;
                    break;
                }
            }
            String infoString=new String(dataByteArray,infoBeginOffset,infoLength,StandardCharsets.UTF_8);
            String[] infoSegments=splitor.split(infoString);
            PhoneNumberInfo phoneNumberInfo=new PhoneNumberInfo();
            phoneNumberInfo.setPhoneNumber(currentPrefix);
            phoneNumberInfo.setProvince(infoSegments[0]);
            phoneNumberInfo.setCity(infoSegments[1]);
            phoneNumberInfo.setZipCode(infoSegments[2]);
            phoneNumberInfo.setAreaCode(infoSegments[3]);
            phoneNumberInfo.setPhoneType(numberType[phoneType]);
            fn.accept(currentPrefix,phoneNumberInfo);
        }
    }
    public String[] lookup(String phoneNumber){
        if(phoneNumber == null || phoneNumber.length() > 11 || phoneNumber.length() < 7){
            return null;
        }
        int phoneNumberPrefix=0;
        try{
            phoneNumberPrefix=Integer.parseInt(phoneNumber.substring(0,7));
        }catch(Exception e){}
        PhoneNumberInfo info=cellPhoneMap.get(phoneNumberPrefix);
        return new String[]{info.getProvince(),info.getCity()};
    }
    
    public static class PhoneNumberInfo{
        private int phoneNumber;
        private String province;
        private String city;
        private String zipCode;
        private String areaCode;
        private String phoneType;
        
        public int getPhoneNumber(){
            return phoneNumber;
        }
        
        public void setPhoneNumber(int phoneNumber){
            this.phoneNumber=phoneNumber;
        }
        
        public String getProvince(){
            return province;
        }
        
        public void setProvince(String province){
            this.province=province;
        }
        
        public String getCity(){
            return city;
        }
        
        public void setCity(String city){
            this.city=city;
        }
        
        public String getZipCode(){
            return zipCode;
        }
        
        public void setZipCode(String zipCode){
            this.zipCode=zipCode;
        }
        
        public String getAreaCode(){
            return areaCode;
        }
        
        public void setAreaCode(String areaCode){
            this.areaCode=areaCode;
        }
        
        public String getPhoneType(){
            return phoneType;
        }
        
        public void setPhoneType(String phoneType){
            this.phoneType=phoneType;
        }
        
        @Override
        public String toString(){
            return "PhoneNumberInfo{" + "phoneNumber='" + phoneNumber + '\'' + ", province='" + province + '\''
                    + ", city='" + city + '\'' + ", zipCode='" + zipCode + '\'' + ", areaCode='" + areaCode + '\''
                    + ", phoneType='" + phoneType + '\'' + '}';
        }
    }
    public static void main(String[] args){
        System.out.println(java.util.Arrays.toString(new CellPhoneLocalLibrary().query("15614349950")));
        System.out.println(java.util.Arrays.toString(new CellPhoneLocalLibrary().query("15727300478")));
        System.out.println(java.util.Arrays.toString(new CellPhoneLocalLibrary().query("18330263281")));
        System.out.println(java.util.Arrays.toString(new CellPhoneLocalLibrary().query("13641265576")));
    }
}
