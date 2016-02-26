package com.example.takenotes;

import java.io.File;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Iterator;
import java.util.List;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.map.LocationData;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.location.GpsStatus;
import android.location.GpsStatus.Listener;
import android.location.GpsSatellite;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class WgcMain extends Fragment {
	
	private LocationManager locationManager;
	private GpsStatus gpsstates;
	private List<GpsSatellite> numSatelliteList = new ArrayList<GpsSatellite>(); // 卫星信号
	
	private TextView sure_num_satellite;
	private TextView latitude;
	private TextView longitude;
	private TextView height;
	private TextView precision;
	private TextView describe;
	private Button new_point_button;
	private Button new_line_button;
	private Button map_button;
	private Button north_button;
	private Button speed_button;
	private Button help_button;
	private ImageView connection;
	private ImageButton share_button;
	
	private LocationData locData = new LocationData();//定位数据
	private String wodedizhi = null;//我的位置具体的城市街道信息
	private LocationClient mLocClient;
	private boolean gps = false;
	private int num_satellite = 0;
	private coor_tran co;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		co = new coor_tran();
		sure_num_satellite = (TextView) getView().findViewById(R.id.sure_num_satellite);
		latitude = (TextView) getView().findViewById(R.id.latitude);
		longitude = (TextView) getView().findViewById(R.id.longitude);
		height = (TextView) getView().findViewById(R.id.height);
		precision = (TextView) getView().findViewById(R.id.precision);
		describe = (TextView) getView().findViewById(R.id.describe);
		new_point_button = (Button) getView().findViewById(R.id.new_point_button);
		new_point_button.setOnClickListener(newpointlisten);
		new_line_button = (Button) getView().findViewById(R.id.new_line_button);
		new_line_button.setOnClickListener(newlinelisten);
		map_button = (Button) getView().findViewById(R.id.map_button);
		map_button.setOnClickListener(maplisten);
		north_button = (Button) getView().findViewById(R.id.north_button);
		north_button.setOnClickListener(northlisten);
		speed_button = (Button) getView().findViewById(R.id.speed_button);
		speed_button.setOnClickListener(speedlisten);
		share_button = (ImageButton) getView().findViewById(R.id.share_button);
		share_button.setOnClickListener(sharelisten);
		help_button = (Button) getView().findViewById(R.id.set_button);
		help_button.setOnClickListener(helplisten);
		connection = (ImageView) getView().findViewById(R.id.connection);
		ObjectAnimator animTxtRotate = ObjectAnimator.ofFloat(connection, "rotation", 0,360);
		animTxtRotate.setRepeatCount(ObjectAnimator.INFINITE);
		animTxtRotate.setRepeatMode(ObjectAnimator.RESTART);
		animTxtRotate.setDuration(3000);
		animTxtRotate.setInterpolator(new AccelerateDecelerateInterpolator());
		animTxtRotate.start();
		
		//GPS
		locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
		gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		if(gps){
			String currentProvider = locationManager.getProvider(LocationManager.GPS_PROVIDER).getName();
			locationManager.requestLocationUpdates(currentProvider, 2000, 1, locationListener);
			locationManager.addGpsStatusListener(gpsListener);
		}
		//baidu
		mLocClient = new LocationClient(this.getActivity());
		
		mLocClient.registerLocationListener(new MyLocationListenner());
		setLocationOption();
		mLocClient.start();
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
//		return super.onCreateView(inflater, container, savedInstanceState);
		return inflater.inflate(R.layout.wgcmain, container, false);
	}

	private GpsStatus.Listener gpsListener = new Listener() {
		
		@Override
		public void onGpsStatusChanged(int event) {
			// TODO Auto-generated method stub
			gpsstates = locationManager.getGpsStatus(null);
			switch (event) {
			case GpsStatus.GPS_EVENT_FIRST_FIX:
				
				break;
			case GpsStatus.GPS_EVENT_STARTED:
				
				break;
			case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
				Iterator<GpsSatellite> it = gpsstates.getSatellites().iterator();
				numSatelliteList.clear();
				int count=0;
				while(it.hasNext() && count < gpsstates.getMaxSatellites()){
					GpsSatellite s = it.next();
					numSatelliteList.add(s);
					count++;
				}
				num_satellite = numSatelliteList.size();
				sure_num_satellite.setText("已发现卫星:"+String.valueOf(numSatelliteList.size()));
				break;
			default:
				break;
			}
		}
	};
	
	private LocationListener locationListener = new LocationListener() {
		
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			if(!gps || num_satellite<4){
				return;
			}
			latitude.setText(String.valueOf("纬度:"+location.getLatitude()));
			longitude.setText(String.valueOf("经度:"+location.getLongitude()));
			height.setText(String.valueOf("海拔:"+location.getAltitude()));
			precision.setText(String.valueOf("精度:"+location.getAccuracy()));
			locData.latitude = location.getLatitude();
			locData.longitude = location.getLongitude();
			
		}
	};

	private Button.OnClickListener newpointlisten = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent it = new Intent();
			it.setClass(getActivity(), WgcTakephoto.class);
			
			Bundle bundle = new Bundle();
			bundle.putString("linename", "0");
			bundle.putString("describe", describe.getText().toString().split(":")[1]);
			bundle.putString("latitude", latitude.getText().toString().split(":")[1]);
			bundle.putString("longitude", longitude.getText().toString().split(":")[1]);
			bundle.putString("height", height.getText().toString().split(":")[1]);
			it.putExtras(bundle);
			startActivityForResult(it, 0);
			
		}
	};

	private Button.OnClickListener newlinelisten = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent it = new Intent();
			it.setClass(getActivity(), Wgc_Map.class);
			Bundle bd = new Bundle();
			bd.putInt("map_type", 3);
			it.putExtras(bd);
			startActivity(it);
		}
	};
	
	private Button.OnClickListener maplisten = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent it = new Intent();
			it.setClass(getActivity(), Wgc_Map.class);
			Bundle bd = new Bundle();
			bd.putInt("map_type", 0);
			it.putExtras(bd);
			startActivity(it);
		}
	};
	
	private Button.OnClickListener northlisten = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent it = new Intent();
			it.setClass(getActivity(), Wgc_North.class);
			
			startActivity(it);
		}
	};
	
	private Button.OnClickListener speedlisten = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent it = new Intent();
			it.setClass(getActivity(), Wgc_Speed.class);
			
			startActivity(it);
		}
	};
	private Button.OnClickListener sharelisten = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Uri smsToUri = Uri.parse("smsto:");   
		    Intent mIntent = new Intent( android.content.Intent.ACTION_SENDTO, smsToUri ); 
		    mIntent.putExtra("sms_body", "我在" + wodedizhi + ",经纬度为：" + String.valueOf(locData.longitude) + "," + String.valueOf(locData.latitude)); 
		    startActivity(mIntent);
		}
	};

	private Button.OnClickListener helplisten = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			new AlertDialog.Builder(getActivity()).setTitle("使用帮助")
				.setMessage("功能说明\n(1)数字填图包含指南针、测速仪(GPS)、经纬度、海拔(GPS)、位置标记、区域记录等功能。\n"
						+ "(2)GPS请确认手机有此功能，并在野外开阔地区使用。\n\n"
						+ "免责申明：按照共享软件惯例，对于本软件安装、复制、使用中导致的任何损害，本软件及著作人不负责任。\n\n"
						+ "关于我们：有问题联系我578006403@qq.com")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						
					}
				}).show();
		}
	};
	
	private void setLocationOption() {
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true); // 设置是否打开gps，使用gps前提是用户硬件打开gps。默认是不打开gps的。
		
		option.setLocationMode(LocationMode.Hight_Accuracy);//设置定位模式
//		option.setCoorType("bd09ll");//返回的定位结果是百度经纬度,默认值gcj02
		option.setCoorType("gcj02");
		option.setScanSpan(3000);//设置发起定位请求的间隔时间为5000ms
		option.setIsNeedAddress(true);//返回的定位结果包含地址信息
		option.setNeedDeviceDirect(true);//返回的定位结果包含手机机头的方向
		mLocClient.setLocOption(option);
	}
	
	public class MyLocationListenner implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			
			if (location == null){
				
				return;
			}
			gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			if(!gps || num_satellite<4){
				LocationData lo_mars = new LocationData();
				LocationData lo_84 = new LocationData();
				lo_mars.latitude = location.getLatitude();
				lo_mars.longitude = location.getLongitude();
				lo_84 = co.MarsToWGS84(lo_mars);
				latitude.setText(String.valueOf("纬度:"+lo_84.latitude));
				longitude.setText(String.valueOf("经度:"+lo_84.longitude));
				height.setText(String.valueOf("海拔:"+location.getAltitude()));
				precision.setText(String.valueOf("精度:"+location.getRadius()));
				locData.latitude = lo_84.latitude;
				locData.longitude = lo_84.longitude;
			}
			describe.setText(String.valueOf("位置:"+location.getAddrStr()));
			wodedizhi = location.getAddrStr();
		}


	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
//		if (resultCode == 0)
//            return;
        
        if (resultCode == 10) {
            Toast.makeText(getActivity(), "成功", Toast.LENGTH_SHORT).show();
        }

        if (data == null)
            return;
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		if(gps){
			String currentProvider = locationManager.getProvider(LocationManager.GPS_PROVIDER).getName();
			locationManager.requestLocationUpdates(currentProvider, 2000, 1, locationListener);
			locationManager.addGpsStatusListener(gpsListener);
		}else{
			Toast.makeText(getActivity(), "GPS 未打开", Toast.LENGTH_SHORT).show();
			locationManager.removeGpsStatusListener(gpsListener);
			locationManager.removeUpdates(locationListener);
		}
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		
	}
	

}
 