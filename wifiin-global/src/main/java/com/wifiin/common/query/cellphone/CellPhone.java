package com.wifiin.common.query.cellphone;

import com.wifiin.common.query.RemoteQueryAdapterHolder;
import com.wifiin.constant.TelecomOperator;

public class CellPhone{
    public static CellPhone getInstance(String phone){
        return new CellPhone(phone);
    }
    private String phone;
    private CellPhone(String phone){
        this.phone=phone;
    }
    
    private String province;
    private String city;
    private void home(){
        String[] home=RemoteQueryAdapterHolder.CELL_PHONE_HOME.query(phone);
        province=home[0];
        city=home[1];
    }
    public String province(){
        if(province==null){
            home();
        }
        return province;
    }
    public String city(){
        if(city==null){
            home();
        }
        return city;
    }
    public TelecomOperator operator(){
        return TelecomOperator.getByPhone(phone);
    }
    public boolean isCMCC(){
        return TelecomOperator.isCMCCPhone(phone);
    }
    public boolean isChinaUnicom(){
        return TelecomOperator.isChinaUnicomPhone(phone);
    }
    public boolean isChinaNet(){
        return TelecomOperator.isChinaNetPhone(phone);
    }
    public boolean isVirtual(){
        return TelecomOperator.isVirtual(phone);
    }
    public boolean isWirelessNetworkCard(){
        return TelecomOperator.isWirelessNetworkCard(phone);
    }
    public boolean isSatelite(){
        return TelecomOperator.isSatelite(phone);
    }
}
