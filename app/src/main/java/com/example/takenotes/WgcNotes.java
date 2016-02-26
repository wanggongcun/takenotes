package com.example.takenotes;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class WgcNotes extends Fragment {

	public List<HashMap<String,Object>> list_notes = new ArrayList<HashMap<String,Object>>();
	private ListAdapter lia = new ListAdapter();

    private WgcDatabase _mydb;
	public SQLiteDatabase _db;
	
	private Dialog dialog;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);

		_mydb = new WgcDatabase(getActivity());
		_db = _mydb.getReadableDatabase();
		list_notes.clear();
		Cursor mcursor = _db.rawQuery("select * from notes", null);
		while(mcursor.moveToNext()){
			HashMap<String,Object> item = new HashMap<String,Object>();
			item.put("describe", mcursor.getString(mcursor.getColumnIndex("describe")));
			item.put("latitude", mcursor.getString(mcursor.getColumnIndex("latitude")));
			item.put("longitude", mcursor.getString(mcursor.getColumnIndex("longitude")));
			item.put("height", mcursor.getString(mcursor.getColumnIndex("height")));
			item.put("whereis", mcursor.getString(mcursor.getColumnIndex("whereis")));
			item.put("time", mcursor.getString(mcursor.getColumnIndex("time")));
			item.put("mark", mcursor.getString(mcursor.getColumnIndex("mark")));
			list_notes.add(item);
		}
		
		ListView lv = (ListView)getView().findViewById(R.id.list_notes);
		
		lv.setAdapter(lia);
		lia.notifyDataSetChanged();
		lv.setOnItemClickListener(listlistener);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
//		return super.onCreateView(inflater, container, savedInstanceState);

		return inflater.inflate(R.layout.wgcnotes, container, false);
	}

	private class ListAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
//			return 0;
			return list_notes.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			final int pos = position;
			if (convertView == null) {
				convertView = getActivity().getLayoutInflater().inflate(R.layout.wgcnotes_info, null);
			}
			
			TextView location_describe = (TextView) convertView.findViewById(R.id.location_describe);
			TextView latitude = (TextView) convertView.findViewById(R.id.latitude);
			TextView longitude = (TextView) convertView.findViewById(R.id.longitude);
			TextView height = (TextView) convertView.findViewById(R.id.height);
			TextView time = (TextView) convertView.findViewById(R.id.time);
			TextView mark = (TextView) convertView.findViewById(R.id.mark);
			
			location_describe.setText(list_notes.get(pos).get("describe").toString());
			latitude.setText(list_notes.get(pos).get("latitude").toString());
			longitude.setText(list_notes.get(pos).get("longitude").toString());
			height.setText(list_notes.get(pos).get("height").toString());
			time.setText(list_notes.get(pos).get("time").toString());
			mark.setText(list_notes.get(pos).get("mark").toString());
			
			return convertView;
		}
		
	}

	private OnItemClickListener listlistener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			final int pos = position;
			dialog = new Dialog(getActivity());
//			dialog.setTitle("请选择");
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setCancelable(true);
			dialog.setContentView(R.layout.dlg_note);
			
			Button picture = (Button) dialog.findViewById(R.id.picture);
			picture.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if(list_notes.get(pos).get("whereis").toString() == ""){
						Toast.makeText(getActivity(), "没有图片", Toast.LENGTH_SHORT).show();
						return;
					}
					File picture = new File(list_notes.get(pos).get("whereis").toString());
					Intent intent = new Intent(Intent.ACTION_VIEW);
		            intent.setDataAndType(Uri.fromFile(picture), "image/*");
		            startActivity(intent);

					dialog.cancel();
				}
			});
			Button tomap = (Button) dialog.findViewById(R.id.tomap);
			tomap.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent it = new Intent();
					it.setClass(getActivity(), Wgc_Map.class);
					Bundle bd = new Bundle();
					bd.putInt("map_type", 1);
					bd.putString("latitude", list_notes.get(pos).get("latitude").toString());
					bd.putString("longitude", list_notes.get(pos).get("longitude").toString());
					bd.putString("describe", list_notes.get(pos).get("describe").toString());
					bd.putString("mark", list_notes.get(pos).get("mark").toString());
					bd.putString("whereis", list_notes.get(pos).get("whereis").toString());
					it.putExtras(bd);
					startActivity(it);

					dialog.cancel();
				}
			});
			Button delete_one = (Button) dialog.findViewById(R.id.delete_one);
			delete_one.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					
					new AlertDialog.Builder(getActivity()).setTitle("确认删除点？") 
				    .setPositiveButton("确定", new DialogInterface.OnClickListener() { 
				 
				        @Override 
				        public void onClick(DialogInterface dialog, int which) { 
				        // 点击“确认”后的操作 
				        	_db.execSQL("delete from notes where time='" + list_notes.get(pos).get("time").toString() + "'");
							lia.notifyDataSetChanged();
				        } 
				    }) 
				    .setNegativeButton("返回", new DialogInterface.OnClickListener() { 
				 
				        @Override 
				        public void onClick(DialogInterface dialog, int which) { 
				        // 点击“返回”后的操作,这里不设置没有任何操作 
				        	
				        } 
				    }).show(); 
					dialog.cancel();
				}
			});
			Button delete_all = (Button) dialog.findViewById(R.id.delete_all);
			delete_all.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					new AlertDialog.Builder(getActivity()).setTitle("确认删除全部？")
						.setPositiveButton("确认删除", new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										// TODO Auto-generated method stub
										_db.execSQL("delete from notes");
										lia.notifyDataSetChanged();
									}
								})
						.setNegativeButton("返回", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								
							}
						}).show();
					dialog.cancel();
				}
			});
			Button cancle = (Button) dialog.findViewById(R.id.cancle);
			cancle.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					dialog.cancel();
				}
			});
			
			dialog.show();
		}
	};
}
