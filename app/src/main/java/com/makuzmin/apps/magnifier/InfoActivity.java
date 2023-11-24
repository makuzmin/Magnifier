package com.makuzmin.apps.magnifier;

import android.os.Bundle;
import android.view.View;
import android.app.*;
import android.widget.*;
import java.util.*;
import android.hardware.*;
import android.os.*;
import android.view.*;
import android.text.*;
import android.view.View.*;

public class InfoActivity extends Activity { 

    final int MIN_LINES = 11;

	Camera cam;
	Handler h;
	String info;
	TextView tvCamInfo, tvPBInfo;
	LinearLayout llPB;
	ProgressBar pb1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info);

		llPB = (LinearLayout) findViewById(R.id.info_llPB);
		tvCamInfo = (TextView) findViewById(R.id.info_tvCamInfo);
		tvPBInfo = (TextView) findViewById(R.id.info_tvPBInfo);
		pb1 = (ProgressBar) findViewById(R.id.info_pb1);
		tvCamInfo.setMinLines(MIN_LINES);
		tvCamInfo.setVisibility(ViewGroup.INVISIBLE);
		llPB.setVisibility(ViewGroup.VISIBLE);
		pb1.setVisibility(ViewGroup.VISIBLE);
		tvPBInfo.setVisibility(ViewGroup.VISIBLE);

		info = "<b>" + getResources().getString(R.string.cam_not_present_HTML) + "</b><br>"
	    	+ getResources().getString(R.string.cam_zero_HTML) + "<br>"
	    	+ getResources().getString(R.string.cam_info_not_avail_HTML);

		h = new Handler();

		Thread t = new Thread(new Runnable(){

				@Override
				public void run()
				{
					if(HelperClass.checkCameraHardware(getBaseContext())){
						List<String> camList = HelperClass.getCameraList(getBaseContext());
						if(camList.size() > 0){
							info = "<b>" + getResources().getString(R.string.cam_is_present_HTML) + "</b><br>"
								+ getResources().getString(R.string.cam_number_HTML) + " " + String.valueOf(camList.size()) + "<br>";
							for(int i = 0; i < camList.size(); i++){
								info = info + "<b>" + getResources().getString(R.string.camera_HTML) + " " + String.valueOf(i + 1) 
									+ " " + getResources().getString(R.string.info_HTML) + " " + "</b><br>";
								if(HelperClass.isBackCamera(i)) info = info + "\t " + getResources().getString(R.string.cam_back_HTML) + "<br>";
								else info = info + "\t " + getResources().getString(R.string.cam_front_HTML) + "<br>";
								camRelease();
								cam = HelperClass.getCameraInstance(i);
								Camera.Parameters camParam = cam.getParameters();
								if(camParam.isZoomSupported()) {
									info = info + "\t " + getResources().getString(R.string.cam_zoom_HTML) + "<br>";
									info = info + "\t" + getResources().getString(R.string.max_zoom) + " " 
										+ camParam.getZoomRatios().get(camParam.getMaxZoom()) 
										+ getResources().getString(R.string.percent) + "<br>";
								}else{info = info + "\t " + getResources().getString(R.string.cam_no_zoom_HTML) + "<br>";}
								info = info + "\t" + getResources().getString(R.string.focus_mode) + " " + camParam.getFocusMode() + "<br>";

							}
							info = info + "<br>" + getResources().getString(R.string.free_txt_1);
							h.post(setTextMessage);
						}
					}
				}
			});
		t.start();	

	}

	Runnable setTextMessage = new Runnable(){

		@Override
		public void run()
		{
			tvCamInfo.setText(Html.fromHtml( info));
			tvCamInfo.setVisibility(ViewGroup.VISIBLE);
			pb1.setVisibility(ViewGroup.GONE);
			llPB.setVisibility(ViewGroup.GONE);
			tvPBInfo.setVisibility(ViewGroup.GONE);
		}		

	};





	@Override
    protected void onPause() {
        super.onPause();
		camRelease();
	} 

	private void camRelease(){
		try {
			if (cam != null){
				cam.release(); 
				cam = null;
			}
		} catch (Exception e) {
			// is error
		}
	}

	/*    @Override
	 public boolean onCreateOptionsMenu(Menu menu) {
	 // Inflate the menu; this adds items to the action bar if it is present.
	 getMenuInflater().inflate(R.menu.menu, menu);
	 return true;
	 }

	 @Override
	 public boolean onOptionsItemSelected(MenuItem item) {
	 // Handle action bar item clicks here. The action bar will
	 // automatically handle clicks on the Home/Up button, so long
	 // as you specify a parent activity in AndroidManifest.xml.
	 int id = item.getItemId();

	 //noinspection SimplifiableIfStatement
	 if (id == R.id.action_settings) {
	 return true;
	 }

	 return super.onOptionsItemSelected(item);
	 }

	 @Override
	 public void onClick(View v) {
	 Snackbar.make(findViewById(R.id.main_content), "TEST", Snackbar.LENGTH_SHORT).show();
	 } */
}
