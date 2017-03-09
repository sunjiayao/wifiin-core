package com.wifiin.util.ip;

import com.wifiin.util.Help;


/** 
 * 
 * @category 用来封装ip相关信息，目前只有两个字段，ip所在的国家和地区
 */

public class IPLocation {
	private static final String[] regions=new String[]{"內蒙古","宁夏","新疆","西藏","广西"};
	private static final String[] municipalities=new String[]{"北京","上海","天津","重庆"};
	private String country;
	private String province;
	private String city;
	private String area;
	
	public IPLocation() {
	    country = area = "";
	}
	
	public IPLocation getCopy() {
	    IPLocation ret = new IPLocation();
	    ret.country = country;
	    ret.area = area;
	    return ret;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
                //如果为局域网，纯真IP地址库的地区会显示CZ88.NET,这里把它去掉
		if(area.trim().equals("CZ88.NET")){
			this.area="本机或本网络";
		}else{
			this.area = area;
		}
	}

	public String getProvince() {
		if(country.indexOf("国")>=0){
			return null;
		}else if(province==null){
			synchronized(this){
				if(province==null){
					int i=0,l=regions.length;
					for(;i<l;i++){
						String region=regions[i];
						if(country.indexOf(region)>=0){
							province=region;
							break;
						}
					}
					if(i>=l){
						i=0;
						l=municipalities.length;
						for(;i<l;i++){
							String city=municipalities[i];
							if(country.startsWith(city)){
								this.province=this.city=city+'市';
								break;
							}
						}
						if(i>=l){
							province=country.substring(0,country.indexOf('省')+1);
						}
					}
				}
			}
		}
		return province;
	}

	public String getCity() {
		if(city==null){
			synchronized(this){
				if(city==null){
					if(getProvince()!=null){
						if(city==null){
							city=country.substring(province.length());
						}
					}
				}
			}
		}
		return city;
	}
	
	public boolean isRegion(){
		return isRegion(getProvince());
	}
	
	public boolean isMunicipality(){
		return isMunicipality(getCity());
	}
	
	public static boolean isRegion(String province){
		return Help.contains(regions, province);
	}
	public static boolean isMunicipality(String city){
		return Help.contains(municipalities, city);
	}
}
