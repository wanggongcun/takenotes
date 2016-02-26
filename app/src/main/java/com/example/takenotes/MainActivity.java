package com.example.takenotes;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MKEvent;
import com.baidu.mapapi.map.MapView;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends Activity {


	private BMapManager mBMapManager = null;//百度地图管理
	private MapView mMapView = null; 
	public static final String BAIDU_MAP_KEY = "Tpqbtn452qBnQbrDY2WuR4OH";
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mBMapManager = new BMapManager(this.getApplicationContext());
		mBMapManager.init(BAIDU_MAP_KEY, new MKGeneralListener() {

			@Override
			public void onGetNetworkState(int iError) {
				if (iError == MKEvent.ERROR_NETWORK_CONNECT) {
					Toast.makeText(MainActivity.this.getApplicationContext(),
							"您的网络出错啦！", Toast.LENGTH_LONG).show();
				}
			}

			@Override
			public void onGetPermissionState(int iError) {
				if (iError == MKEvent.ERROR_PERMISSION_DENIED) {
					
					Toast.makeText(MainActivity.this.getApplicationContext(),
							"请输入正确的授权Key！", Toast.LENGTH_LONG).show();
				}
			}
		});
		
		setContentView(R.layout.activity_main);

		setupViewComponent();
		
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
//		return super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Toast.makeText(this, "没有东西设置 ", Toast.LENGTH_SHORT).show();
			return true;
		}else if(id == R.id.action_about){
			Toast.makeText(this, "王功存 ", Toast.LENGTH_SHORT).show();
			return true;
		}else if(id == R.id.action_quit){
			new AlertDialog.Builder(MainActivity.this).setTitle("确认退出")
			.setPositiveButton("退出", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub

					finish();
					System.exit(0);
				}
			}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					
				}
			}).show();
			
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private long exitTime = 0;
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
			if((System.currentTimeMillis()-exitTime) > 2000){
				Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
				exitTime = System.currentTimeMillis();
			} else {
				finish();
				System.exit(0);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void setupViewComponent(){
		final ActionBar actBar = getActionBar();
		actBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		Fragment fra_notes = new WgcNotes();
		actBar.addTab(actBar.newTab().setText("位置记录").setTabListener(new Wgc_TabListener(fra_notes)));
		
		Fragment fra_main = new WgcMain();
		actBar.addTab(actBar.newTab().setText("主页").setTabListener(new Wgc_TabListener(fra_main)),true);
		
		
		Fragment fra_lines = new WgcLines();
		actBar.addTab(actBar.newTab().setText("区域").setTabListener(new Wgc_TabListener(fra_lines)));
		
	}
	
	
}
