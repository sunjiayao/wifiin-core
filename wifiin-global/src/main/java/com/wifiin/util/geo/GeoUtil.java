package com.wifiin.util.geo;

import java.math.BigInteger;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.binary.Base32;

import com.wifiin.util.Help;


public class GeoUtil {
	private static final double RAD = Math.PI / 180.0; //一角度=？弧度
	private static final double EARTH_RADIUS = 6378137.0;//单位是米
	
	/**
	 * 计算两个经纬度之间的距离，单位是米
	 * @param lng1  第一个点的经度
	 * @param lat1 第一个点的纬度
	 * @param lng2 第二个点的经度
	 * @param lat2 第二个点的纬度
	 * @return
	 */
	public static double calculateDistance(double lng1, double lat1, double lng2, double lat2){
		double radLat1 = lat1*RAD;
	    double radLat2 = lat2*RAD;
	    return Math.abs(2 * EARTH_RADIUS * Math.asin(Math.sqrt(Math.pow(Math.sin((radLat1 - radLat2)/2),2)+
	    			Math.cos(radLat1)*Math.cos(radLat2)*Math.pow(Math.sin((lng1 - lng2)*RAD/2),2))));
	}
	/**
	 * 计算经纬度hash
	 * @param lng      经度
	 * @param lat      纬度
	 * @param iterate  迭代次数，最多32次，大于32或小于等于0按32次算
	 * @return
	 */
	public static long enGeoHashCode(double lng,double lat,int iterate){
		double minLng=-180,maxLng=180,minLat=-90,maxLat=90;
		long hash=0;
		if(iterate<=0 || iterate>32){iterate=32;}
		for(int i=0;i<iterate;i++){
			hash<<=1;
			double midLng=(minLng+maxLng)/2;
			if(lng>midLng){
				hash|=1;
				minLng=midLng;
			}else{
				maxLng=midLng;
			}
			hash<<=1;
			double midLat=(minLat+maxLat)/2;
			if(lat>midLat){
				hash|=1;
				minLat=midLat;
			}else{
				maxLat=midLat;
			}
		}
		return hash<<(64-iterate*2);
	}
	private static String convertHash(long hash,int radix,int iterate,int bitBlock){
		String geoHash=null;
		if(hash==0){
			return "0";
		}else if(hash>0){
			geoHash=Long.toString(hash,radix);
		}else{
			byte[] bytes=Help.transferLongToByteArray(hash);
			geoHash=new BigInteger(1,bytes).toString(radix);
		}
		int len=iterate*2;
		len=len/bitBlock+(len%bitBlock>0?1:0);
		return geoHash.substring(0, len);
	}
	public static String enGeoHashCodeTo32Radix(double lng,double lat,int iterate){
		long hash=enGeoHashCode(lng,lat,iterate);
		return convertHash(hash,32,iterate,5);
	}
	public static String enGeoHashCodeToHex(double lng,double lat, int iterate){
		long hash=enGeoHashCode(lng,lat,iterate);
		return convertHash(hash,16,iterate,4);
	}
	public static String enGeoHashCodeTo4Radix(double lng,double lat,int iterate){
	    long hash=enGeoHashCode(lng,lat,iterate);
	    return convertHash(hash,4,iterate,2);
	}
	public static String enGeoHashCodeToBase32(double lng,double lat,int iterate) throws EncoderException{
		long hash=enGeoHashCode(lng,lat,iterate);
		byte[] hashBytes=Help.transferLongToByteArray(hash);
		int i=0;
		while(hashBytes[i]==0){
			i++;
		}
		byte[] finalBytes=new byte[8-i];
		System.arraycopy(hashBytes, i, finalBytes, 0, finalBytes.length);
		return new Base32().encodeToString(finalBytes);
	}
	/**
	 * 根据geohash值计算大致的经纬度
	 * @param hash    geohash
	 * @param iterate 计算hash时的迭代次数最大32，如果小于等于0，或大于32，就按32算
	 * @return  从hash返回大致的经纬度数组[lng,lat]
	 */
	public static double[] deGeoHashCode(long hash,int iterate){
		if(iterate>32 || iterate<=0){iterate=32;}
		int start=32*2-2;
		long mask=0b11L<<start;
		double minLng=-180,maxLng=180,minLat=-90,maxLat=90,midLat=0,midLng=0;
		for(;iterate>0;mask>>>=2,start-=2,iterate--){
			switch((int)((hash&mask)>>>start)){
			case 0:
				maxLng=midLng;
				maxLat=midLat;
				break;
			case 1:
				maxLng=midLng;
				minLat=midLat;
				break;
			case 2:
				minLng=midLng;
				maxLat=midLat;
				break;
			case 3:
				minLng=midLng;
				minLat=midLat;
				break;
			}
			midLng=(maxLng+minLng)/2;
			midLat=(maxLat+minLat)/2;
		}
		return new double[]{midLng,midLat};
	}
	public static double[] deGeoHashCodeFromBase32(String hash,int iterate){
		byte[] hashBytes=new Base32().decode(hash);
		long hashVal=Help.transferByteArrayToLong(hashBytes);
		return deGeoHashCode(hashVal, iterate);
	}
	public static double[] deGeoHashCodeFrom32Radix(String hash,int iterate){
	    return deGeoHashCodeFromXRadix(hash,iterate,13,32);
	}
	public static double[] deGeoHashCodeFromHex(String hash,int iterate){
	    return deGeoHashCodeFromXRadix(hash,iterate,16,16);
	}
	public static double[] deGeoHashCodeFrom4Radix(String hash,int iterate){
	    return deGeoHashCodeFromXRadix(hash,iterate,32,4);
	}
	private static double[] deGeoHashCodeFromXRadix(String hash,int iterate,int maxLen,int radix){
	    return deGeoHashCode(new BigInteger(hash+Help.concat("0",maxLen-hash.length()),radix).longValue(),iterate);
	}
	/**
	 * 高德 阿里云 腾讯 灵图经纬度转百度经纬度
	 * @param gcjLng
	 * @param gcjLat
	 * @return
	 */
	public static double[] gcj2bd(double gcjLng,double gcjLat){
		double x = gcjLng, y = gcjLat;
	    double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * Math.PI);
	    double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * Math.PI);
	    double bdLng = z * Math.cos(theta) + 0.0065;
	    double bdLat = z * Math.sin(theta) + 0.006;
	    return new double[]{bdLng,bdLat};
	}
	/**
	 * 百度经纬度转高德 阿里云 腾讯 灵图,这四家都使用火星座标系
	 */
	public static double[] bd2gcj(long bdLng,double bdLat){
		double x = bdLng - 0.0065, y = bdLat - 0.006;
	    double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * Math.PI);
	    double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * Math.PI);
	    double gcjLng = z * Math.cos(theta);
	    double gcjLat = z * Math.sin(theta);
	    return new double[]{gcjLng,gcjLat};
	}
	public static void main(String[] args) throws EncoderException {
//		//漠河121°07′～124°20′，北纬52°10′～53°33′，
//		//三亚北纬18°09′34″——18°37′27″、东经108°56′30″——109°48′28″
//		//0   1   2   3    4      5     6      7       8       9         10           11          12          13             14            15                16                  17                 18
//		//90  0   45 22.5 11.25 5.625 2.8125 1.40625 0.703125 0.3515625 0.17578125 0.087890625 0.0439453125 0.02197265625 0.010986328125 0.0054931640625 0.00274608203125  0.001873041015625
//        //180 0   90 45   22.5  11.25 5.625  2.8125  1.40625  0.703125  0.3515625  0.17578125  0.087890625  0.0439453125  0.02197265625  0.010986328125  0.0054931640625   0.00274608203125  0.001873041015625
		double minus=0.007647845-0.006509918;
		System.out.println(minus);
		double lng1=((121+7/60)+(124+20/60))/2;
		double lat1=(52+10/60+53+33/60)/2;
		double lng2=lng1-minus;//0.07647845;
		double lat2=lat1-minus;///0.07647845;
		System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$");
		System.out.println(calculateDistance(lng1, lat1, lng2, lat2));
		System.out.println(enGeoHashCodeTo32Radix(lng1, lat1, 32));
		System.out.println(enGeoHashCodeTo32Radix(lng2, lat2, 32));
		System.out.println(enGeoHashCodeTo32Radix(lng1, lat1, 14));
		System.out.println(enGeoHashCodeTo32Radix(lng2, lat2, 14));
		System.out.println("*************************");
		System.out.println(enGeoHashCodeToBase32(lng1, lat1, 32));
		System.out.println(enGeoHashCodeToBase32(lng2, lat2, 32));
		System.out.println(enGeoHashCodeToBase32(lng1, lat1, 15));
		System.out.println(enGeoHashCodeToBase32(lng2, lat2, 15));
		System.out.println("###############################");
		lng1=(108+56/60+30/3600+109+48/60+28/3600)/2;
		lat1=((18+9/60+34/3600)+(18+37/60+27/3600))/2;
		lng2=lng1-minus;//0.06509918;
		lat2=lat1-minus;//0.06509918;
		System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$");
		System.out.println(calculateDistance(lng1, lat1, lng2, lat2));
		System.out.println(enGeoHashCodeTo32Radix(lng1, lat1, 32));
		System.out.println(enGeoHashCodeTo32Radix(lng2, lat2, 32));
		System.out.println(enGeoHashCodeTo32Radix(lng1, lat1, 15));
		System.out.println(enGeoHashCodeTo32Radix(lng2, lat2, 15));
		System.out.println("#########################");
		System.out.println(enGeoHashCodeToHex(lng1, lat1, 32));
		System.out.println(enGeoHashCodeToHex(lng2, lat2, 32));
		System.out.println(enGeoHashCodeToHex(lng1, lat1, 16));
		System.out.println(enGeoHashCodeToHex(lng2, lat2, 16));
		System.out.println("*************************");
		System.out.println(enGeoHashCodeToBase32(lng1, lat1, 32));
		System.out.println(enGeoHashCodeToBase32(lng2, lat2, 32));
		System.out.println(enGeoHashCodeToBase32(lng1, lat1, 10));
		System.out.println(enGeoHashCodeToBase32(lng2, lat2, 10));
		System.out.println("@@@@@@@@@@@@@@@@@@@");
		System.out.println(calculateDistance(101.598727, 37.381927, 101.598661,37.380957));
		
		//#ee7rif4  39.235777000000000   117.191693000000000
		System.out.println(GeoUtil.enGeoHashCodeTo32Radix(117.574852000000,    36.680988000000,15));
		double[] loc=GeoUtil.deGeoHashCodeFrom32Radix(GeoUtil.enGeoHashCodeTo32Radix(117.191693000000000,39.235777000000000,13),13);
		System.out.println(GeoUtil.calculateDistance(loc[0],loc[1],117.191693000000000,39.235777000000000));
	}
}
