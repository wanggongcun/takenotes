package com.example.takenotes;

import com.baidu.mapapi.map.LocationData;

public class coor_tran {
	private LocationData ld;

	double pi = 3.14159265358979324;
	double a = 6378245.0;
	double ee = 0.00669342162296594323;
	
	public coor_tran() {
		// TODO Auto-generated constructor stub
		ld = new LocationData();
	}
	
	public LocationData WGS84ToMars(LocationData gp){
		LocationData gp1 = new LocationData();
		
		double dLat = MarsFromLat(gp.longitude-105, gp.latitude-35);
		double dLon = MarsFromLon(gp.longitude-105, gp.latitude-35);
		double radLat = gp.latitude/180.0 * pi;
		double magic = Math.sin(radLat);
		magic = 1 - ee*magic*magic;
		double sqrtMagic = Math.sqrt(magic);
		dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
		dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
		gp1.latitude = gp.latitude + dLat;
		gp1.longitude = gp.longitude + dLon;
		
		return gp1; 
	}
	
	public LocationData MarsToWGS84(LocationData gp){
		LocationData gp1 = new LocationData();
		LocationData gp2 = new LocationData();
		gp2 = WGS84ToMars(gp);
		gp1.longitude = 2*gp.longitude - gp2.longitude;
		gp1.latitude = 2*gp.latitude - gp2.latitude;
		
		return gp1; 
	}
	
	private double MarsFromLat(double x,double y){
		double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
		ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
		ret += (20.0 * Math.sin(y * pi) + 40.0 * Math.sin(y / 3.0 * pi)) * 2.0 / 3.0;
		ret += (160.0 * Math.sin(y / 12.0 * pi) + 320 * Math.sin(y * pi / 30.0)) * 2.0 / 3.0;
		return ret;
	}
	
	private double MarsFromLon(double x,double y){
		
		double ret = 300.0 + x + 2.0*y + 0.1*x*x + 0.1*x*y + 0.1*Math.sqrt(Math.abs(x));
		ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
		ret += (20.0 * Math.sin(x * pi) + 40.0 * Math.sin(x / 3.0 * pi)) * 2.0 / 3.0;
		ret += (150.0 * Math.sin(x / 12.0 * pi) + 300.0 * Math.sin(x / 30.0 * pi)) * 2.0 / 3.0;
		return ret;
	}
}
