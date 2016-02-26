package com.example.takenotes;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;

import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.mapapi.map.PopupClickListener;
import com.baidu.mapapi.map.PopupOverlay;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.baidu.mapapi.map.*;
import com.baidu.mapapi.search.MKPoiInfo;

import android.graphics.drawable.*;
import android.widget.*;
import android.os.Environment;
import android.view.*;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View.MeasureSpec;
import android.content.Context;
import android.content.DialogInterface;


public class OverlayTest extends ItemizedOverlay<OverlayItem> {
	private Activity at;
	GeoPoint centerpoint;
	private MapView mv;
	private Context ct;
	ArrayList<OverlayItem> pointlist;
	private AlertDialog.Builder aldlg;
	public GeoPoint start;

    PopupOverlay popup = null;
	
	//��MapView����ItemizedOverlay  
    public OverlayTest(Drawable mark,MapView mapView,Activity activity,Context context){  	
            super(mark,mapView);  
            at =activity;
            mv =mapView;
            ct =context;
    }  
    
	public static Bitmap convertViewToBitmap(View view) {
		view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
		view.buildDrawingCache();
		Bitmap bitmap = view.getDrawingCache();

		return bitmap;
	}
	
	@Override
    protected boolean onTap(final int index) {  
        
        //System.out.println("item onTap: "+index);
		pointlist = this.getAllItem();
//        Toast.makeText(at.getApplicationContext(), pointlist.get(index).getSnippet(), Toast.LENGTH_SHORT).show();       
		View popview = LayoutInflater.from(ct).inflate(  
	            R.layout.poipop, null);
	    TextView TestText = (TextView)popview.findViewById(R.id.poinote);  
	    TestText.setText(pointlist.get(index).getTitle());  
	    TextView TestText2 = (TextView)popview.findViewById(R.id.poinote2);  
	    TestText2.setText(pointlist.get(index).getSnippet().split(" ")[0]);
	    ImageView imageview = (ImageView)popview.findViewById(R.id.imageView);

		if(pointlist.get(index).getSnippet().split(" ")[1] != "0"){
		    Bitmap bitmap = BitmapFactory.decodeFile(pointlist.get(index).getSnippet().split(" ")[1], null);    
	        imageview.setImageBitmap(bitmap); 
		}
	    Bitmap popbitmap = convertViewToBitmap(popview);
	    mv.getController().setCenter(start);
	    mv.refresh();
	    popup = new PopupOverlay(mv, new PopupClickListener() {
			
			@Override
			public void onClickedPopup(int arg0) {
				// TODO Auto-generated method stub
				
			}
		}
	);
	    
	    popup.showPopup(popbitmap, pointlist.get(index).getPoint(), 50);  
	    
		return true;  
    }  
        
	@Override
    public boolean onTap(GeoPoint pt, MapView mapView){  
                
                super.onTap(pt,mapView);  
        	    start = pt;
        	    try{
        	    	popup.hidePop();
        	    }catch(Exception e){
        	    	
        	    }
                return false;  
    }  
	public GeoPoint getstart(){
		return start;
	}

}
