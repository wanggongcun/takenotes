package com.example.takenotes;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class WgcDatabase extends SQLiteOpenHelper{

	private static final String db_name = "wgcdb.db";
	private static final int version = 1;
	
	public WgcDatabase(Context context) {
		super(context, db_name, null, version);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		String sql1 = "create table notes(time timestamp,latitude double,longitude double,height double,describe varchar(64),whereis varchar(64),mark varchar(128),linename integer default '0')";
		String sql2 = "create table lines(linename integer PRIMARY KEY AUTOINCREMENT,time timestamp,mark varchar(64),lat_min double,lat_max double," +
						"lon_min double,lon_max double,lines double,rows double)";
		String sql3 = "create table linename(latitude double,longitude double)";
		db.execSQL(sql1);
		db.execSQL(sql2);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}
}
