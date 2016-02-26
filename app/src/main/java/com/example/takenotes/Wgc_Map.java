package com.example.takenotes;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.map.Geometry;
import com.baidu.mapapi.map.Graphic;
import com.baidu.mapapi.map.GraphicsOverlay;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MKEvent;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.mapapi.map.Symbol;
import com.baidu.mapapi.utils.CoordinateConvert;
import com.baidu.mapapi.utils.DistanceUtil;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.sun.mail.imap.Utility;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.GpsStatus.Listener;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Wgc_Map extends Activity {
	
	private BMapManager mBMapManager = null;//百度地图管理
	private MapView mMapView = null; 
	public static final String BAIDU_MAP_KEY = "Tpqbtn452qBnQbrDY2WuR4OH";
	private LocationClient mLocClient;
	private LocationData locnote; //查看点
	private BDLocation locData = new BDLocation();//当前点
	private double latitude = 0.0;
	private double longitude = 0.0;
	private double height = 0.0;
	private String describe = "";
	
	private OverlayTest itemOverlay = null;
	private Drawable mark;
	private GraphicsOverlay graphicsoverlay;
	private MyLocationOverlay myLocationOverlay;
	private LocationData lo;
	
	private int map_type = 0;
	private int strid = 0; 
	private boolean draw = false;
	private float dirc = 0;
	private List<LocationData> list_loc = new ArrayList<LocationData>();
	
	private LinearLayout line_all;
	private Button line_begin;
	private Button line_mark;
	private Button line_end;
	private TextView relation;
	private ImageButton tuceng;

    private WgcDatabase _mydb;
	public SQLiteDatabase _db;
	private Dialog dialog;
	private SensorManager manager;
	private SensorListener listener = new SensorListener();
	
	private LocationManager locationManager;
	private GpsStatus gpsstates;
	private List<GpsSatellite> numSatelliteList = new ArrayList<GpsSatellite>(); // 卫星信号
	private boolean gps = false;
	private int num_satellite = 0;
	private coor_tran co;
	private CoordinateConvert converter; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mBMapManager = new BMapManager(this.getApplicationContext());
		mBMapManager.init(BAIDU_MAP_KEY, new MKGeneralListener() {

			@Override
			public void onGetNetworkState(int iError) {
				if (iError == MKEvent.ERROR_NETWORK_CONNECT) {
					Toast.makeText(Wgc_Map.this.getApplicationContext(),
							"您的网络出错啦！", Toast.LENGTH_LONG).show();
				}
			}

			@Override
			public void onGetPermissionState(int iError) {
				if (iError == MKEvent.ERROR_PERMISSION_DENIED) {
					
					Toast.makeText(Wgc_Map.this.getApplicationContext(),
							"请输入正确的授权Key！", Toast.LENGTH_LONG).show();
				}
			}
		});
		setContentView(R.layout.wgc_map);

		mMapView = (MapView) findViewById(R.id.bmapView);
		mLocClient = new LocationClient(Wgc_Map.this);
		mLocClient.registerLocationListener(new MyLocationListenner());
		setLocationOption();
		setMap();
		mLocClient.start();
		locnote = new LocationData();
		lo = new LocationData();

		
		mark = getResources().getDrawable(R.drawable.marker);
		itemOverlay = new OverlayTest(mark, mMapView, Wgc_Map.this,
				getApplicationContext());
		graphicsoverlay = new GraphicsOverlay(mMapView);
		myLocationOverlay = new MyLocationOverlay(mMapView);
		myLocationOverlay.enableCompass();
		mMapView.getOverlays().add(myLocationOverlay);
		
		line_all = (LinearLayout) findViewById(R.id.line_control);
		line_all.setVisibility(View.GONE);
		line_begin = (Button) findViewById(R.id.line_begin);
		line_begin.setOnClickListener(beginlistener);
		line_mark = (Button) findViewById(R.id.line_mark);
		line_mark.setOnClickListener(marklistener);
		line_end = (Button) findViewById(R.id.line_end);
		line_end.setOnClickListener(endlistener);
		relation = (TextView) findViewById(R.id.relation);
		tuceng = (ImageButton) findViewById(R.id.tuceng);
		tuceng.setOnClickListener(tucenglistener);
		
		_mydb = new WgcDatabase(Wgc_Map.this);
		_db = _mydb.getReadableDatabase();
		converter  = new CoordinateConvert(); 
		co = new coor_tran();
		
		mMapView.getOverlays().add(itemOverlay);
		mMapView.getOverlays().add(graphicsoverlay);
		map_type = getIntent().getExtras().getInt("map_type");
		if(map_type == 1){
			locnote.latitude = Double.valueOf(getIntent().getExtras().getString("latitude"));
			locnote.longitude = Double.valueOf(getIntent().getExtras().getString("longitude"));
			GeoPoint p = converter.fromWgs84ToBaidu(new GeoPoint((int)(locnote.latitude * 1E6), (int)(locnote.longitude * 1E6))); 
			OverlayItem item1 = new OverlayItem(p, getIntent().getExtras().getString("describe"), getIntent().getExtras().getString("mark")
					 + " " +  getIntent().getExtras().getString("whereis") + " 0");
			itemOverlay.addItem(item1);
		}else if (map_type == 2){
			line_all.setVisibility(View.VISIBLE);
			strid = Integer.valueOf(getIntent().getExtras().getString("linename"));
			setdata();
		}else if (map_type == 3){
			line_all.setVisibility(View.VISIBLE);
		}

		//GPS
		locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		if(gps){
			String currentProvider = locationManager.getProvider(LocationManager.GPS_PROVIDER).getName();
			locationManager.requestLocationUpdates(currentProvider, 2000, 1, locationListener);
			locationManager.addGpsStatusListener(gpsListener);
		}
		manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		Sensor sensor = manager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		manager.registerListener(listener, sensor,SensorManager.SENSOR_DELAY_GAME);
		
	}
	
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
	
	private void setMap() {
		// 设置启用内置的缩放控件
		mMapView.setBuiltInZoomControls(true);
		
		// 获取地图控制器，可以用它控制平移和缩放112.944849,28.165789
		MapController mMapController = mMapView.getController();
		GeoPoint p;
		double cLat = 28.165789;
		double cLon = 112.944849;
		p = new GeoPoint((int) (cLat * 1E6), (int) (cLon * 1E6));
		mMapController.setCenter(p);

		// 设置地图的缩放级别。 这个值的取值范围是[3,18]。
		mMapController.setZoom(15);
		
	}
	
	public class MyLocationListenner implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			
			if (location == null){
				
				return;
			}
			describe = location.getAddrStr();
			gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			if(gps || num_satellite>3){
				return;
			}
			myLocationOverlay.disableCompass();
			if(!myLocationOverlay.isCompassEnable()){
				myLocationOverlay.enableCompass();
			} 
			GeoPoint pp = CoordinateConvert.fromGcjToBaidu(new GeoPoint((int)(location.getLatitude() * 1E6), (int)(location.getLongitude() * 1E6))); 
			
			lo.latitude = location.getLatitude();
			lo.longitude = location.getLongitude();
			lo.direction = dirc;
			LocationData loc1 = new LocationData();
			loc1.latitude = pp.getLatitudeE6()/1E6;
			loc1.longitude = pp.getLongitudeE6()/1E6;
//			loc1.latitude = location.getLatitude();
//			loc1.longitude = location.getLongitude();
			myLocationOverlay.setData(loc1);
			
			LocationData locationdata = new LocationData();
			locationdata = co.MarsToWGS84(lo);
			latitude = locationdata.latitude;
			longitude = locationdata.longitude;
			height = location.getAltitude();
			
			if(map_type == 0){
				GeoPoint p = new GeoPoint((int)(location.getLatitude() * 1E6), (int)(location.getLongitude() * 1E6));
				mMapView.getController().animateTo(pp);
				map_type = 4;
				
			}else if(map_type == 1){
				GeoPoint p = new GeoPoint((int)(locnote.latitude * 1E6), (int)(locnote.longitude * 1E6));
//				GeoPoint p = new GeoPoint((int)(location.getLatitude() * 1E6), (int)(location.getLongitude() * 1E6));
				mMapView.getController().animateTo(pp);
				map_type = 4;
			}else if(map_type == 2){
				
			}else if(map_type == 3){

				GeoPoint p = new GeoPoint((int)(location.getLatitude() * 1E6), (int)(location.getLongitude() * 1E6));
				mMapView.getController().animateTo(p);
			}
			mMapView.refresh();
		}

		
		
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
			if(!gps || num_satellite<4 || location.getLatitude()==0.0){
				return;
			}
			
			myLocationOverlay.disableCompass();
			if(!myLocationOverlay.isCompassEnable()){
				myLocationOverlay.enableCompass();
			}  
			GeoPoint pp = CoordinateConvert.fromWgs84ToBaidu(new GeoPoint((int)(location.getLatitude() * 1E6), (int)(location.getLongitude() * 1E6))); 
			
			latitude = location.getLatitude();
			longitude = location.getLongitude();
			height = location.getAltitude();
			lo = new LocationData();
			lo.latitude = location.getLatitude();
			lo.longitude = location.getLongitude();
			lo.direction = dirc;
			
			LocationData locationdata = new LocationData();
			locationdata = co.WGS84ToMars(lo);
			LocationData loc1 = new LocationData();
			loc1.latitude = pp.getLatitudeE6()/1E6;
			loc1.longitude = pp.getLongitudeE6()/1E6;
			myLocationOverlay.setData(loc1);
			
			if(map_type == 0){
				mMapView.getController().animateTo(pp);
				map_type = 4;
				
			}

			mMapView.refresh();
			
		}
	};
	
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
			Toast.makeText(Wgc_Map.this, "GPS 未打开", Toast.LENGTH_SHORT).show();
			locationManager.removeGpsStatusListener(gpsListener);
			locationManager.removeUpdates(locationListener);
		}
	}
	
	private Button.OnClickListener beginlistener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			draw = true;
			if(strid>0){
				return;
			}
			dialog = new Dialog(Wgc_Map.this);
//			dialog.setTitle("请选择");
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setCancelable(true);
			dialog.setContentView(R.layout.dlg_newgrid);
			final EditText et_mark = (EditText) dialog.findViewById(R.id.et_mark);
			final CheckBox cb_range = (CheckBox) dialog.findViewById(R.id.cb_range);
			final EditText et_latitude_min = (EditText) dialog.findViewById(R.id.et_latitude_min);
			final EditText et_latitude_max = (EditText) dialog.findViewById(R.id.et_latitude_max);
			final EditText et_longitude_min = (EditText) dialog.findViewById(R.id.et_longitude_min);
			final EditText et_longitude_max = (EditText) dialog.findViewById(R.id.et_longitude_max);
			final EditText et_line = (EditText) dialog.findViewById(R.id.et_line);
			final EditText et_row = (EditText) dialog.findViewById(R.id.et_row);
			Button bt_ok = (Button) dialog.findViewById(R.id.bt_ok);
			Button bt_cancle = (Button) dialog.findViewById(R.id.bt_cancle);
			
			DisplayMetrics dm = new DisplayMetrics();        
	        getWindowManager().getDefaultDisplay().getMetrics(dm);        
	        int widthPixels = dm.widthPixels;        
	        int heightPixels = dm.heightPixels;        
	        float density = dm.density;        
	        // 计算屏幕宽度和高度        
	        int screenWidth = (int) (widthPixels * density)/2;        
	        int screenHeight = (int) (heightPixels * density)/2;         
//	        // 计算屏幕中心点经纬度       
	        
	        GeoPoint centerPoint = mMapView.getProjection().fromPixels(screenWidth/2, screenHeight/2);
			final GeoPoint ltpoint = mMapView.getProjection().fromPixels(0, 0);
			final GeoPoint rtpoint = mMapView.getProjection().fromPixels(screenWidth, 0);
			final GeoPoint lbpoint = mMapView.getProjection().fromPixels(0, screenHeight);
			final GeoPoint rbpoint = mMapView.getProjection().fromPixels(screenWidth, screenHeight);
	        et_latitude_min.setText(String.valueOf(lbpoint.getLatitudeE6()/1E6));
	        et_latitude_max.setText(String.valueOf(ltpoint.getLatitudeE6()/1E6));
	        et_longitude_min.setText(String.valueOf(ltpoint.getLongitudeE6()/1E6));
	        et_longitude_max.setText(String.valueOf(rtpoint.getLongitudeE6()/1E6));
			bt_ok.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					double line = Double.valueOf(et_line.getText().toString());
					double row = Double.valueOf(et_row.getText().toString());
					double lat_min = Double.valueOf(et_latitude_min.getText().toString());
					double lat_max = Double.valueOf(et_latitude_max.getText().toString());
					double lon_min = Double.valueOf(et_longitude_min.getText().toString());
					double lon_max = Double.valueOf(et_longitude_max.getText().toString());
					LocationData grid_84_max = new LocationData();
					LocationData grid_84_min = new LocationData();
					LocationData grid_ma_max = new LocationData();
					LocationData grid_ma_min = new LocationData();
					
					if(cb_range.isChecked() == false){
						grid_84_max.latitude = lat_max;
						grid_84_max.longitude = lon_max;
						grid_84_min.latitude = lat_min;
						grid_84_min.longitude = lon_min;
//						grid_ma_max = co.WGS84ToMars(grid_84_max);
//						grid_ma_min = co.WGS84ToMars(grid_84_min);
						
						GeoPoint nrtpoint = converter.fromWgs84ToBaidu(new GeoPoint((int)(lat_max * 1E6), (int)(lon_max * 1E6)));
						GeoPoint nlbpoint = converter.fromWgs84ToBaidu(new GeoPoint((int)(lat_min * 1E6), (int)(lon_min * 1E6)));
						rtpoint.setLatitudeE6(nrtpoint.getLatitudeE6());
						rtpoint.setLongitudeE6(nrtpoint.getLongitudeE6());
						lbpoint.setLatitudeE6(nlbpoint.getLatitudeE6());
						lbpoint.setLongitudeE6(nlbpoint.getLongitudeE6());

						lat_min = (double)nlbpoint.getLatitudeE6()/1E6;
						lat_max = (double)nrtpoint.getLatitudeE6()/1E6;
						lon_min = (double)nlbpoint.getLongitudeE6()/1E6;
						lon_max = (double)nrtpoint.getLongitudeE6()/1E6;
					}
					
					
					Symbol lineSymbol = new Symbol();
				    Symbol.Color lineColor = lineSymbol.new Color();
				    lineColor.red = 255;
				    lineColor.green = 0;
				    lineColor.blue = 0;
				    lineColor.alpha = 255;
				    lineSymbol.setLineSymbol(lineColor, 1);
				    int line_lat_geopoint = (int)((lat_max-lat_min) * 1E6);
				    int row_lon_geopoint = (int)((lon_max-lon_min) * 1E6);
				    double lat_len = DistanceUtil.getDistance(new GeoPoint((int)(lat_min * 1E6), (int)(lon_max * 1E6)), 
				    										new GeoPoint((int)(lat_max * 1E6), (int)(lon_max * 1E6)));
				    line = (lat_max-lat_min) * 1E6 * line/lat_len;
				    double lon_len = DistanceUtil.getDistance(new GeoPoint((int)(lat_min * 1E6), (int)(lon_min * 1E6)), 
							new GeoPoint((int)(lat_min * 1E6), (int)(lon_max * 1E6)));
				    row = (lon_max-lon_min) * 1E6 * row/lon_len;
					for(double i=0;i<line_lat_geopoint;i+=line){
						Geometry linegeometry = new Geometry();
						GeoPoint[] linepoint = new GeoPoint[2];
						linepoint[0] = new GeoPoint((int)(lat_min*1E6 + i), (int)(lon_min*1E6));
						linepoint[1] = new GeoPoint((int)(lat_min*1E6 + i), (int)(lon_max*1E6));
						linegeometry.setPolyLine(linepoint);
						
					    // 生成Graphic对象
					    Graphic lineGraphic = new Graphic(linegeometry, lineSymbol);
					    graphicsoverlay.setData(lineGraphic);
					}
					for(double j=0;j<row_lon_geopoint;j+=row){
						Geometry linegeometry = new Geometry();
						GeoPoint[] linepoint = new GeoPoint[2];
						linepoint[0] = new GeoPoint((int)(lat_min*1E6), (int)(lon_min*1E6 + j));
						linepoint[1] = new GeoPoint((int)(lat_max*1E6), (int)(lon_min*1E6 + j));
						linegeometry.setPolyLine(linepoint);
						
					    // 生成Graphic对象
					    Graphic lineGraphic = new Graphic(linegeometry, lineSymbol);
					    
					    graphicsoverlay.setData(lineGraphic);
					}
					mMapView.refresh();
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String t = format.format(new Date());
					ContentValues newRow = new ContentValues();
					newRow.put("time", t);
					newRow.put("mark", et_mark.getText().toString());
					newRow.put("lat_min", String.valueOf(lat_min));
					newRow.put("lat_max", String.valueOf(lat_max));
					newRow.put("lon_min", String.valueOf(lon_min));
					newRow.put("lon_max", String.valueOf(lon_max));
					newRow.put("lines", String.valueOf(line));
					newRow.put("rows", String.valueOf(row));
					
					if(_db.insert("lines", null, newRow) == -1){
						Toast.makeText(Wgc_Map.this, "保存失败", Toast.LENGTH_SHORT).show();
					}
					Cursor cursor = _db.rawQuery("select * from lines",null);            
					     
					if(cursor.moveToLast())    
					   strid = Integer.valueOf(cursor.getString(cursor.getColumnIndex("linename")));  
					
					dialog.cancel();
				}
			});
			bt_cancle.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					dialog.cancel();
				}
			});
			dialog.show();
		}
	};
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		Toast.makeText(Wgc_Map.this, String.valueOf(requestCode + " " + resultCode), Toast.LENGTH_SHORT).show();
		if(resultCode == 10){
//			GeoPoint p = new GeoPoint((int)(latitude * 1E6), (int)(longitude * 1E6));
//			OverlayItem item1 = new OverlayItem(p, describe, describe);
//			itemOverlay.addItem(item1);
//			mMapView.refresh();
			itemOverlay.removeAll();
			graphicsoverlay.removeAll();
			setdata();
		}
	}
	
	private Button.OnClickListener marklistener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(draw){
//				list_loc.add(locnote);
				
				Intent it = new Intent();
				it.setClass(Wgc_Map.this, WgcTakephoto.class);
				
				Bundle bundle = new Bundle();
				bundle.putString("linename", String.valueOf(strid));
				bundle.putString("describe", String.valueOf(describe));
				bundle.putString("latitude", String.valueOf(latitude));
				bundle.putString("longitude", String.valueOf(longitude));
				bundle.putString("height", String.valueOf(height));
				it.putExtras(bundle);
				startActivityForResult(it, 0);
				
			}
		}
	};
	
	private void setdata(){
		Cursor cursor = _db.rawQuery("select * from notes where linename='" + strid + "'",null);            
		while(cursor.moveToNext()){
//			cursor.getColumnIndex("describe");
			GeoPoint p = new GeoPoint((int)(cursor.getDouble(cursor.getColumnIndex("latitude")) * 1E6),
					(int)(cursor.getDouble(cursor.getColumnIndex("longitude")) * 1E6));
			GeoPoint pp = converter.fromWgs84ToBaidu(p);
			OverlayItem item1 = new OverlayItem(pp, String.valueOf(cursor.getString(cursor.getColumnIndex("describe"))), 
						String.valueOf(cursor.getString(cursor.getColumnIndex("mark")) + " " + cursor.getString(cursor.getColumnIndex("whereis")) + " 0"));
			itemOverlay.addItem(item1);
		}
		Cursor cursor2 = _db.rawQuery("select * from lines where linename='" + strid + "'",null);
		if(cursor2.moveToLast()){
			double line = Double.valueOf(cursor2.getString(cursor2.getColumnIndex("lines")));
			double row = Double.valueOf(cursor2.getString(cursor2.getColumnIndex("rows")));
			double lat_min = cursor2.getDouble(cursor2.getColumnIndex("lat_min"));
			double lat_max = cursor2.getDouble(cursor2.getColumnIndex("lat_max"));
			double lon_min = cursor2.getDouble(cursor2.getColumnIndex("lon_min"));
			double lon_max = cursor2.getDouble(cursor2.getColumnIndex("lon_max"));
			Symbol lineSymbol = new Symbol();
		    Symbol.Color lineColor = lineSymbol.new Color();
		    lineColor.red = 255;
		    lineColor.green = 0;
		    lineColor.blue = 0;
		    lineColor.alpha = 255;
		    lineSymbol.setLineSymbol(lineColor, 1);
		    int line_lat_geopoint = (int)((lat_max-lat_min) * 1E6);
		    int row_lon_geopoint = (int)((lon_max-lon_min) * 1E6);
//		    double lat_len = DistanceUtil.getDistance(new GeoPoint((int)(lat_min * 1E6), (int)(lon_max * 1E6)), 
//						new GeoPoint((int)(lat_max * 1E6), (int)(lon_max * 1E6)));
//			line = (lat_max-lat_min) * 1E6 * line/lat_len;
//			double lon_len = DistanceUtil.getDistance(new GeoPoint((int)(lat_min * 1E6), (int)(lon_min * 1E6)), 
//						new GeoPoint((int)(lat_min * 1E6), (int)(lon_max * 1E6)));
//			row = (lon_max-lon_min) * 1E6 * row/lon_len;
			for(double i=0;i<line_lat_geopoint;i+=line){
				Geometry linegeometry = new Geometry();
				GeoPoint[] linepoint = new GeoPoint[2];
				linepoint[0] = new GeoPoint((int)(lat_min*1E6 + i), (int)(lon_min*1E6));
				linepoint[1] = new GeoPoint((int)(lat_min*1E6 + i), (int)(lon_max*1E6));
				linegeometry.setPolyLine(linepoint);
				
			    // 生成Graphic对象
			    Graphic lineGraphic = new Graphic(linegeometry, lineSymbol);
			    graphicsoverlay.setData(lineGraphic);
			    if(i==0){
			    	mMapView.getController().animateTo(linepoint[0]);
			    }
			}
			for(double j=0;j<row_lon_geopoint;j+=row){
				Geometry linegeometry = new Geometry();
				GeoPoint[] linepoint = new GeoPoint[2];
				linepoint[0] = new GeoPoint((int)(lat_min*1E6), (int)(lon_min*1E6 + j));
				linepoint[1] = new GeoPoint((int)(lat_max*1E6), (int)(lon_min*1E6 + j));
				linegeometry.setPolyLine(linepoint);
				
			    // 生成Graphic对象
			    Graphic lineGraphic = new Graphic(linegeometry, lineSymbol);
			    
			    graphicsoverlay.setData(lineGraphic);
			}
//			for(int i=0;i<line+1;i++){
//				Geometry linegeometry = new Geometry();
//				GeoPoint[] linepoint = new GeoPoint[2];
//				linepoint[0] = new GeoPoint((int)((lat_min + (lat_max-lat_min)*i/line)*1E6), (int)(lon_min*1E6));
//				linepoint[1] = new GeoPoint((int)((lat_min + (lat_max-lat_min)*i/line)*1E6), (int)(lon_max*1E6));
//				linegeometry.setPolyLine(linepoint);
//				
//			    // 生成Graphic对象
//			    Graphic lineGraphic = new Graphic(linegeometry, lineSymbol);
//			    graphicsoverlay.setData(lineGraphic);
//			}
//			for(int j=0;j<row+1;j++){
//				Geometry linegeometry = new Geometry();
//				GeoPoint[] linepoint = new GeoPoint[2];
//				linepoint[0] = new GeoPoint((int)(lat_min*1E6), (int)((lon_min + (lon_max-lon_min)*j/row)*1E6));
//				linepoint[1] = new GeoPoint((int)(lat_max*1E6), (int)((lon_min + (lon_max-lon_min)*j/row)*1E6));
//				linegeometry.setPolyLine(linepoint);
//				
//			    // 生成Graphic对象
//			    Graphic lineGraphic = new Graphic(linegeometry, lineSymbol);
//			    
//			    graphicsoverlay.setData(lineGraphic);
//			}
		}
		mMapView.refresh();
		
	}
	
	private Button.OnClickListener endlistener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			draw = false;
		}
	};
	private boolean satellite = true;
	private Button.OnClickListener tucenglistener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(satellite){
				satellite = false;
				mMapView.setSatellite(true);
				mMapView.refresh();
			}else{
				satellite = true;
				mMapView.setSatellite(false);
				mMapView.refresh();
			}
		}
	};
	
	
	public class SensorListener implements SensorEventListener{

		
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub
			float degree = event.values[0]; 

			relation.setText(String.valueOf(degree));
			dirc = degree;
//			mMapView.refresh();
		}
		
	}

}
