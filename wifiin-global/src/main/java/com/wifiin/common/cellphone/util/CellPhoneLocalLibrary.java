package com.wifiin.common.cellphone.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import com.wifiin.exception.CellPhoneHomeException;
/**
 * cellphone library classpath:phone.dat  is from https://github.com/lovedboy/phone
 * @author Running
 *
 */
public class CellPhoneLocalLibrary implements CellPhoneHome{
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
    
    private static byte[] dataByteArray;
    private ByteBuffer byteBuffer;
    private int indexAreaOffset=-1;
    private int phoneRecordCount=-1;
    
    public CellPhoneLocalLibrary(){
        if(dataByteArray == null){
            synchronized(CellPhoneLocalLibrary.class){
                if(dataByteArray == null){
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
                        throw new CellPhoneHomeException(e);
                    }
                    dataByteArray=byteData.toByteArray();
                }
            }
        }
        
        byteBuffer=ByteBuffer.wrap(dataByteArray);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        int dataVersion=byteBuffer.getInt();
        indexAreaOffset=byteBuffer.getInt();
        
        // print data version
        // System.out.println(dataVersion);
        
        phoneRecordCount=(dataByteArray.length - indexAreaOffset) / INDEX_SEGMENT_LENGTH;
        // print record count
        // System.out.println(phoneRecordCount);
    }
    
    public String[] lookup(String phoneNumber){
        if(phoneNumber == null || phoneNumber.length() > 11 || phoneNumber.length() < 7){
            return null;
        }
        int phoneNumberPrefix;
        try{
            phoneNumberPrefix=Integer.parseInt(phoneNumber.substring(0,7));
        }catch(Exception e){
            return null;
        }
        int left=0;
        int right=phoneRecordCount;
        while(left <= right){
            int middle=(left + right) >> 1;
            int currentOffset=indexAreaOffset + middle * INDEX_SEGMENT_LENGTH;
            if(currentOffset >= dataByteArray.length){
                return null;
            }
            
            byteBuffer.position(currentOffset);
            int currentPrefix=byteBuffer.getInt();
            if(currentPrefix > phoneNumberPrefix){
                right=middle - 1;
            }else if(currentPrefix < phoneNumberPrefix){
                left=middle + 1;
            }else{
                int infoBeginOffset=byteBuffer.getInt();
                int phoneType=byteBuffer.get();
                
                int infoLength=-1;
                for(int i=infoBeginOffset;i < indexAreaOffset;++i){
                    if(dataByteArray[i] == 0){
                        infoLength=i - infoBeginOffset;
                        break;
                    }
                }
                
                String infoString=new String(dataByteArray,infoBeginOffset,infoLength,StandardCharsets.UTF_8);
                int idx=infoString.indexOf('|');
                String province=infoString.substring(0,idx);
                idx++;
                String city=infoString.substring(idx,infoString.indexOf('|',idx));
//                String[] infoSegments=infoString.split("\\|");
//                PhoneNumberInfo phoneNumberInfo=new PhoneNumberInfo();
//                phoneNumberInfo.setPhoneNumber(phoneNumber);
//                phoneNumberInfo.setProvince(infoSegments[0]);
//                phoneNumberInfo.setCity(infoSegments[1]);
//                phoneNumberInfo.setZipCode(infoSegments[2]);
//                phoneNumberInfo.setAreaCode(infoSegments[3]);
//                phoneNumberInfo.setPhoneType(numberType[phoneType]);
                return new String[]{province,city};
            }
        }
        return null;
    }
    
    public static class PhoneNumberInfo{
        private String phoneNumber;
        private String province;
        private String city;
        private String zipCode;
        private String areaCode;
        private String phoneType;
        
        public String getPhoneNumber(){
            return phoneNumber;
        }
        
        public void setPhoneNumber(String phoneNumber){
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
