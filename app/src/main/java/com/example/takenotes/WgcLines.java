package com.example.takenotes;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class WgcLines extends Fragment {

	public List<HashMap<String,Object>> list_lines = new ArrayList<HashMap<String,Object>>();
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
		list_lines.clear();
		Cursor mcursor = _db.rawQuery("select * from lines", null);
		while(mcursor.moveToNext()){
			HashMap<String,Object> item = new HashMap<String,Object>();
			item.put("time", mcursor.getString(mcursor.getColumnIndex("time")));
			item.put("mark", mcursor.getString(mcursor.getColumnIndex("mark")));
			item.put("linename", mcursor.getString(mcursor.getColumnIndex("linename")));
			list_lines.add(item);
		}
		
		ListView lv = (ListView)getView().findViewById(R.id.list_lines);
		
		lv.setAdapter(lia);
		lia.notifyDataSetChanged();
		lv.setOnItemClickListener(listlistener);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
//		return super.onCreateView(inflater, container, savedInstanceState);

		return inflater.inflate(R.layout.wgclines, container, false);
	}

	private class ListAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
//			return 0;
			return list_lines.size();
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
				convertView = getActivity().getLayoutInflater().inflate(R.layout.wgclines_info, null);
			}
			
			TextView linename = (TextView) convertView.findViewById(R.id.linename);
			TextView time = (TextView) convertView.findViewById(R.id.time);
			TextView mark = (TextView) convertView.findViewById(R.id.mark);
			
			linename.setText(list_lines.get(pos).get("linename").toString());
			time.setText(list_lines.get(pos).get("time").toString());
			mark.setText(list_lines.get(pos).get("mark").toString());
			
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
			dialog.setContentView(R.layout.dlg_line);
			
			Button tomap = (Button) dialog.findViewById(R.id.tomap);
			tomap.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent it = new Intent();
					it.setClass(getActivity(), Wgc_Map.class);
					Bundle bd = new Bundle();
					bd.putInt("map_type", 2);
					bd.putString("linename", list_lines.get(pos).get("linename").toString());
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
					
					new AlertDialog.Builder(getActivity()).setTitle("确认删除区域？") 
				    .setPositiveButton("确定", new DialogInterface.OnClickListener() { 
				 
				        @Override 
				        public void onClick(DialogInterface dialog, int which) { 
				        // 点击“确认”后的操作 
				        	_db.execSQL("delete from notes where linename='" + list_lines.get(pos).get("linename").toString() + "'");
				        	_db.execSQL("delete from lines where linename='" + list_lines.get(pos).get("linename").toString() + "'");
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
			delete_all.setText("导出文件");
			delete_all.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					final EditText et = new EditText(getActivity());
		        	String fn = "net" + list_lines.get(pos).get("linename").toString() + ".txt";
		        	et.setText(fn);
					new AlertDialog.Builder(getActivity()).setTitle("请输入文件名").setView(   
						     et).setPositiveButton("确定", new DialogInterface.OnClickListener() { 
								 
							        @Override 
							        public void onClick(DialogInterface dialog, int which) { 
							        // 点击“确认”后的操作 
							        	if(et.getText().toString() == ""){
							        		return;
							        	}
							        	String filename = et.getText().toString();
							        	
										FileOutputStream fileout = null;
										try{
											File file = new File(Environment.getExternalStorageDirectory(), filename);  
											fileout = new FileOutputStream(file);
											
											Cursor mcursor = _db.rawQuery("select * from notes where linename='" + list_lines.get(pos).get("linename").toString() + "'", null);
											while(mcursor.moveToNext()){
												String fileContent = "";
												fileContent += mcursor.getString(mcursor.getColumnIndex("time")) + ",";
												fileContent += mcursor.getString(mcursor.getColumnIndex("latitude")) + ",";
												fileContent += mcursor.getString(mcursor.getColumnIndex("longitude")) + ",";
												fileContent += mcursor.getString(mcursor.getColumnIndex("height")) + ",";
												fileContent += mcursor.getString(mcursor.getColumnIndex("describe")) + ",";
												fileContent += mcursor.getString(mcursor.getColumnIndex("whereis")) + ",";
												fileContent += mcursor.getString(mcursor.getColumnIndex("mark")) + ";";
												fileout.write(fileContent.getBytes("UTF-8"));  
											}
											
									        fileout.close();
											Toast.makeText(getActivity(), "保存文件成功", Toast.LENGTH_SHORT).show();
										
										}catch(Exception e){
//											e.printStackTrace();
											Toast.makeText(getActivity(), "失败", Toast.LENGTH_SHORT).show();
										}
							        } 
							    })   
						     .setNegativeButton("取消", new DialogInterface.OnClickListener() { 
								 
							        @Override 
							        public void onClick(DialogInterface dialog, int which) { 
							        // 点击“确认”后的操作 
							        	
							        } 
							    }).show();   
					
					
//					Properties props = new Properties();
//					props.put("mail.smtp.host", "smtp.qq.com");
//					props.put("mail.smtp.auth", "true");
//					Transport transport = null;
//					Session session = Session.getDefaultInstance(props,null);
//					session.setDebug(true);
//					MimeMessage msg = new MimeMessage(session);
//					try {
//						transport = session.getTransport("smtp");
//						transport.connect("smtp.qq.com", "183866155@qq.com", "386090831");
////						msg.setSentDate(new Date());
//						InternetAddress fromAddress = null;
//						fromAddress = new InternetAddress("183866155@qq.com");
//						msg.setFrom(fromAddress);
//						InternetAddress toAddress = null;
//						toAddress = new InternetAddress("578006403@qq.com");
//						msg.setRecipient(Message.RecipientType.TO, toAddress);
//						msg.setSubject("数据");
//						MimeMultipart multi = new MimeMultipart();
//						BodyPart textBodyPart = new MimeBodyPart();
//						textBodyPart.setText("123456");
//						multi.addBodyPart(textBodyPart);
//						//添加附件
//						msg.setContent(multi);
//						msg.saveChanges();
//						transport.sendMessage(msg, msg.getAllRecipients());
//						transport.close();
//						Toast.makeText(getActivity(), "success", Toast.LENGTH_SHORT).show();
//					} catch (NoSuchProviderException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//						Toast.makeText(getActivity(), "失败1", Toast.LENGTH_SHORT).show();
//					} catch (MessagingException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//						Toast.makeText(getActivity(), "失败2", Toast.LENGTH_SHORT).show();
//					} catch (Exception e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//						Toast.makeText(getActivity(), "失败3", Toast.LENGTH_SHORT).show();
//					}
					
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
