package com.wifiin.common.query;

import com.wifiin.common.query.cellphone.CellPhone360;
import com.wifiin.common.query.cellphone.CellPhoneBaiFuBao;
import com.wifiin.common.query.cellphone.CellPhoneBaidu;
import com.wifiin.common.query.cellphone.CellPhoneDaHanBank;
import com.wifiin.common.query.cellphone.CellPhoneIteBlog;
import com.wifiin.common.query.cellphone.CellPhoneTaobao;
import com.wifiin.common.query.cellphone.CellPhoneTenPay;
import com.wifiin.common.query.ip.IpQuerySina;
import com.wifiin.common.query.ip.IpQueryTaobao;

public enum RemoteQueryAdapterHolder {
    CELL_PHONE_HOME{
        private final RemoteQuery query=new RemoteQueryAdapter(
//                new CellPhoneLocalLibrary(),
                null,
                new CellPhone360(),
                new CellPhoneBaiFuBao(),
                new CellPhoneTaobao(),
                new CellPhoneBaidu(),
                new CellPhoneDaHanBank(),
                new CellPhoneIteBlog(),
                new CellPhoneTenPay()
                );
        @Override
        public String[] query(String param){
            return query.query(param);
        }
    },
    IP {
        private final RemoteQuery query=new RemoteQueryAdapter(
//                new IpQueryLocalLibrary(),
                null,
                new IpQuerySina(),
                new IpQueryTaobao());
        @Override
        public String[] query(String param){
            return query.query(param);
        }
    };
    
    public abstract String[] query(String param);
    
    public static void main(String[] args){
        System.out.println(java.util.Arrays.toString(CELL_PHONE_HOME.query("18971611819")));
        System.out.println(java.util.Arrays.toString(CELL_PHONE_HOME.query("15841949186")));
        System.out.println(java.util.Arrays.toString(CELL_PHONE_HOME.query("18612033170")));
        System.out.println(java.util.Arrays.toString(CELL_PHONE_HOME.query("18503263171")));
        System.out.println(java.util.Arrays.toString(CELL_PHONE_HOME.query("13501252662")));
        System.out.println(java.util.Arrays.toString(CELL_PHONE_HOME.query("18330263281")));
        System.out.println(java.util.Arrays.toString(CELL_PHONE_HOME.query("18993447296")));
        System.out.println(java.util.Arrays.toString(CELL_PHONE_HOME.query("15128179570")));
        System.out.println(java.util.Arrays.toString(CELL_PHONE_HOME.query("18801115834")));
//        String s=java.util.Arrays.toString(IP.query("133.233.123.157"));
//        long start=System.currentTimeMillis();
////        for(int i=0;i<10000;i++){
//            s=java.util.Arrays.toString(IP.query("133.233.123.157"));//180.217.138.84
//            System.out.println(s);
//            s=java.util.Arrays.toString(IP.query("180.217.138.84"));
//            System.out.println(s);
//            s=java.util.Arrays.toString(IP.query("43.242.252.0"));
//            System.out.println(s);
//            s=java.util.Arrays.toString(IP.query("210.75.225.254"));
//            System.out.println(s);
//            s=java.util.Arrays.toString(IP.query("8.8.8.8"));
//            System.out.println(s);
////        }
//        System.out.println((System.currentTimeMillis()-start)+"   "+s);
    }
}
