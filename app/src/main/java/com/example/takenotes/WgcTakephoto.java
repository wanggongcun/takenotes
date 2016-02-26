package com.example.takenotes;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class WgcTakephoto extends Activity {
    
    private static final int NONE = 0;
    private static final int PHOTO_GRAPH = 1;// 拍照
    private static final int PHOTO_ZOOM = 2; // 缩放
    private static final int PHOTO_RESOULT = 3;// 结果
    private static final String IMAGE_UNSPECIFIED = "image/*";
    private ImageView imageView = null;
    private Button btnPhone = null;
    private Button btnTakePicture = null;
    private Button btnok;
    private Button btncancle;
    private EditText etlocation;
    private EditText etwhere;
    private EditText etmark;
    
    private String photo_name = "";
    private String photo_time = "";
    private String photo_path = "";
    
    private String describe = "";
    private String latitude = "";
    private String longitude = "";
    private String height = "";
    private String strid = "";
    
    private WgcDatabase _mydb;
	public SQLiteDatabase _db;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wgc_takephoto);
		
		imageView = (ImageView) findViewById(R.id.imageView);
        btnPhone = (Button) findViewById(R.id.btnPhone);
        btnPhone.setOnClickListener(onClickListener);
        btnTakePicture = (Button) findViewById(R.id.btnTakePicture);
        btnTakePicture.setOnClickListener(onClickListener);
        btnok = (Button) findViewById(R.id.btnok);
        btnok.setOnClickListener(okclicklistener);
        btncancle = (Button) findViewById(R.id.btncancle);
        btncancle.setOnClickListener(cancleclicklistener);
        etlocation = (EditText) findViewById(R.id.etlocation);
        etwhere = (EditText) findViewById(R.id.etwhere);
        etmark = (EditText) findViewById(R.id.etmark);
        
		_mydb = new WgcDatabase(this);
		_db = _mydb.getReadableDatabase();
		
		strid = getIntent().getExtras().get("linename").toString();
		describe = getIntent().getExtras().get("describe").toString();
		latitude = getIntent().getExtras().get("latitude").toString();
		longitude = getIntent().getExtras().get("longitude").toString();
		height = getIntent().getExtras().get("height").toString();
		etlocation.setText(describe);
		etwhere.setText(latitude + "," + longitude + "," + height);
		
	}
	
	private final Button.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if(v==btnPhone){ //从相册获取图片
                Intent intent = new Intent(Intent.ACTION_PICK, null);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_UNSPECIFIED);
                startActivityForResult(intent, PHOTO_ZOOM);
//                photo_name = DateFormat.format("yyyyMMddhhmmss", new Date()).toString()+".jpg";
            }else if(v==btnTakePicture){ //从拍照获取图片
            	photo_name = DateFormat.format("yyyyMMddhhmmss", new Date()).toString()+".jpg";
            	photo_path = Environment.getExternalStorageDirectory() + "/" + photo_name;
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(Environment
                    .getExternalStorageDirectory(),photo_name)));
                startActivityForResult(intent, PHOTO_GRAPH);
            }

        }

    };
    
    private Button.OnClickListener okclicklistener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String t = format.format(new Date());
			String[] whereis = etwhere.getText().toString().split(",");
			ContentValues newRow = new ContentValues();
			newRow.put("time", t);
			newRow.put("latitude", whereis[0]);
			newRow.put("longitude", whereis[1]);
			newRow.put("height", whereis[2]);
			newRow.put("describe", etlocation.getText().toString());
			newRow.put("mark", etmark.getText().toString());
			newRow.put("linename", strid);
			if(photo_path != "")
				newRow.put("whereis", photo_path);
			
			if(_db.insert("notes", null, newRow) == -1){
				Toast.makeText(WgcTakephoto.this, "保存失败", Toast.LENGTH_SHORT).show();
			}else{
				Intent it = new Intent();
				setResult(10,it);
				finish();
			}
			
			
		}
	};

	 private Button.OnClickListener cancleclicklistener = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent it = new Intent();
				setResult(1,it);
				finish();
			}
	};
		
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == NONE)
            return;
        // 拍照
        if (requestCode == PHOTO_GRAPH) {
            // 设置文件保存路径
//            File picture = new File(Environment.getExternalStorageDirectory()
//                    + "/" + photo_name);
            
//            startPhotoZoom(Uri.fromFile(picture));
//        	photo_path = Environment.getExternalStorageDirectory() + "/" + photo_name;
            Bitmap bitmap = BitmapFactory.decodeFile(photo_path, null);    
            imageView.setImageBitmap(bitmap); 
        }

        if (data == null)
            return;

        // 读取相册缩放图片
        if (requestCode == PHOTO_ZOOM) {
//            startPhotoZoom(data.getData());
//        	 Bitmap bitmap = data.getExtras().getParcelable("data"); 
        	Uri uri = data.getData();
        	String[] proj = { MediaStore.Images.Media.DATA };
        	Cursor actualimagecursor = managedQuery(uri,proj,null,null,null);
        	int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);   
        	actualimagecursor.moveToFirst();   
        	photo_path = actualimagecursor.getString(actual_image_column_index);   
//        	 photo_path = data.getData().getPath();
        	 Bitmap bitmap = BitmapFactory.decodeFile(photo_path, null);   
             imageView.setImageBitmap(bitmap); 
        }
        // 处理结果
        if (requestCode == PHOTO_RESOULT) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                Bitmap photo = extras.getParcelable("data");
//                ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                photo.compress(Bitmap.CompressFormat.JPEG, 75, stream);// (0-100)压缩文件
                //此处可以把Bitmap保存到sd卡中，具体请看：http://www.cnblogs.com/linjiqin/archive/2011/12/28/2304940.html
                imageView.setImageBitmap(photo); //把图片显示在ImageView控件上
            }

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 收缩图片
     * 
     * @param uri
     */
    public void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, IMAGE_UNSPECIFIED);
//        intent.putExtra("crop", "true");
//        // aspectX aspectY 是宽高的比例
//        intent.putExtra("aspectX", 1);
//        intent.putExtra("aspectY", 1);
//        // outputX outputY 是裁剪图片宽高
//        intent.putExtra("outputX", 300);
//        intent.putExtra("outputY", 500);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, PHOTO_RESOULT);
    }

	
}
