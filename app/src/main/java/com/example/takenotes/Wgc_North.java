package com.example.takenotes;

import com.example.takenotes.Wgc_Map.SensorListener;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

public class Wgc_North extends Activity {

	private float mydegree = 0;
	private ImageView north;
	private TextView position;
	private SensorManager manager;
	private SensorListener listener = new SensorListener();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wgc_north);
		north = (ImageView) findViewById(R.id.north);
		position = (TextView) findViewById(R.id.position);
		
		manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		Sensor sensor = manager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		manager.registerListener(listener, sensor,SensorManager.SENSOR_DELAY_GAME);
	}
	
	public class SensorListener implements SensorEventListener{

		
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub
			float degree = event.values[0]; 
			north.setRotation(360-degree);
			position.setText("方位:" + String.valueOf((int)degree));
//			mMapView.refresh();
		}
		
	}
}
