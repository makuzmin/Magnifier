package com.makuzmin.apps.magnifier;

import android.app.*;
import android.os.*;
import android.view.*;
import android.view.View.OnTouchListener;
import android.widget.*;
import android.content.*;
import android.content.pm.*;
import android.hardware.*;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import java.util.*;
import android.preference.PreferenceManager;
import java.util.concurrent.*;
import android.content.SharedPreferences.Editor;
import android.util.*;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.FloatingActionButton.OnClickListener;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.graphics.Typeface;
import java.io.*;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.net.Uri;
import android.graphics.Matrix;
import android.content.res.Configuration;

public class MainActivity extends AppCompatActivity implements OnTouchListener
{
	final int API_LEVEL = 14;
	
	final Double SENSOR_ACCURACY = 0.7;
	final int SECONDS_SLEEP = 30;
	final float TEXT_SIZE = 20;
	final int PADDING_TOP = 10;
	final int PADDING_LEFT = 30;
	final int PADDING_BOTTOM = 0;
	final int PADDING_RIGHT = 0;

	//shared preference constants
	final String SAVED_I = "saved_i";
	final String SAVED_FRONT = "saved_front";
	final String SAVED_BACK = "saved_back";
	final String SAVED_NUM = "camera_num";
	final String PREF_NAME = "magnifier_data";

	private Camera cam;
	private int cameraNumber;
	private CamView mCView;
	Camera.AutoFocusCallback cb;
	Boolean hasOnCreate;

	boolean isBack = true;

	ActionBar bar;
	FrameLayout camPreview;
	TextView tvCamParameters;	
	FloatingActionButton fab, fab1;

	int iBack, iFront;
	int basei = 0;
	Double startDist = 0.0;
	float a0, a1, a2;

	Handler h;

	// sensor variables
	private SensorManager mSensorManager;
	private Sensor mAcceleration;
	SensorEventListener listener;	

    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState)
	{	

        super.onCreate(savedInstanceState);
		hasOnCreate = true;
        setContentView(R.layout.main);	

		a0 = a1 = a2 = 0;
		//restoreParameters();
		restoreParam();

		camPreview = (FrameLayout) findViewById(R.id.camera_preview);
		camPreview.setOnTouchListener(this);

		fab = (FloatingActionButton) findViewById(R.id.fab);
		fab1 = (FloatingActionButton) findViewById(R.id.fab1);
		setLightBtnImg();

		//connect sensors
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
			mAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

			listener = new SensorEventListener() {

				@Override
				public void onSensorChanged(SensorEvent event)
				{
					if(getPDif(a0, event.values[0]) > SENSOR_ACCURACY || 
					   getPDif(a1, event.values[1]) > SENSOR_ACCURACY ||
					   getPDif(a2, event.values[2]) > SENSOR_ACCURACY){
						if (cam != null){
							try{
			    				cam.autoFocus(cb);
							}catch (Exception e){}
						}
						a0 = event.values[0];
						a1 = event.values[1];
						a2 = event.values[2];
					}
				}

				@Override
				public void onAccuracyChanged(Sensor p1, int p2)
				{
					// TODO: Implement this method
				}
			};
		}		

		if(HelperClass.checkCameraHardware(this)) {
			bar = getSupportActionBar();
			if(bar != null)
				if(Build.VERSION.SDK_INT >= API_LEVEL)
	        		bar.setBackgroundDrawable(getResources().getDrawable(R.drawable.textured));
				else
					bar.setBackgroundDrawable(getResources().getDrawable(R.drawable.ab_solid_example));
			try{
				tvCamParameters = new TextView(getBaseContext());
				if (cam != null){
					cam.release(); 
					cam = null;
					camPreview.removeAllViews();
					mCView = null;
				} 
				initCamera(cameraNumber);
			}catch(Exception e){}	

//keep screen on
			camPreview.setKeepScreenOn(true);	
		}
		else {
			Toast.makeText(getBaseContext(), getResources().getString(R.string.cam_not_present), Toast.LENGTH_SHORT).show();
			finish();
		} 

		fab.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v)
				{
					if(HelperClass.isBackCamera(cameraNumber)){
	    				SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
			    		Editor ed = sp.edit();
		    			Boolean bol = true;
						fab.setImageResource(R.drawable.ic_flash_on_white_48dp);
		    			String code =getResources().getString(R.string.show_param2);
	    				if(sp.getBoolean(code, false)) { 
			    			bol=false;
							fab.setImageResource(R.drawable.ic_flash_off_white_48dp);
						}
		    			ed.putBoolean(code, bol);
	    				ed.commit();	
	    				CamView.setFlashlight(MainActivity.this, cameraNumber, cam);
					}
				}

			});

		fab1.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v)
				{
					++cameraNumber;
					if(cameraNumber > Camera.getNumberOfCameras() -1) cameraNumber = 0;
					saveParameters();						
					try{
						tvCamParameters = new TextView(getBaseContext());
						if (cam != null){
							cam.release(); 
							cam = null;
							camPreview.removeAllViews();
							mCView = null;
						} 
						initCamera(cameraNumber);
					}catch(Exception e){}	
				}
			});
	}
// End of onCreate()

//	Multitouch ZOOM control

	@Override
	public boolean onTouch(View view, MotionEvent event)
	{
		//external variables: iBack, iFront, basei, startDist, cam;
		Camera.Parameters camParam = cam.getParameters();
		int result; 
		int i = iFront;

		if(isBack) i=iBack;

		switch(event.getActionMasked()){
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_POINTER_DOWN:
				if(event.getPointerCount() >= 2){
					startDist = getTouchDist(event.getX(1), event.getX(0), event.getY(1), event.getY(0));
					basei = i;
				}
				break;
			case MotionEvent.ACTION_UP:
				startDist = 0.0;
				break;
			case MotionEvent.ACTION_MOVE:
				if(event.getPointerCount() >= 2){
					double currDist = getTouchDist(event.getX(1), event.getX(0), event.getY(1), event.getY(0));
					if(currDist-startDist < 0) {
						result = (int)((currDist - startDist)/currDist);
					} else {
						result = (int)((currDist - startDist)/startDist);
					}
					if(camParam.isZoomSupported()) {
						i = basei + result;
						if(i > camParam.getZoomRatios().size() -1) i = camParam.getZoomRatios().size() -1;
						if(i < 0) i = 0;
						camParam.setZoom(i);
						if(isBack) iBack = i;
						else iFront = i;
						cam.setParameters(camParam);
						setZoomText(camParam, i);	
					}
				}
				break;
		}
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		getMenuInflater().inflate(R.menu.menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.menu_info:
				Intent intent = new Intent(this, InfoActivity.class);
				startActivity(intent);
				return true;
			case R.id.menu_exit:
				finish();
				return true;
			case R.id.menu_pict:
				cam.takePicture(mShutter, null, mPicture);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}	

	@Override
    protected void onPause() {
        super.onPause();
		saveParameters();
		mSensorManager.unregisterListener(listener);
		try {
			if (cam != null){
				cam.release(); 
				cam = null;
				camPreview.removeAllViews();
				mCView = null;
			}
		} catch (Exception e) {
			// is error
		}
	} 

	@Override
	protected void onResume() {
		super.onResume();
		mSensorManager.registerListener(listener, mAcceleration, SensorManager.SENSOR_DELAY_NORMAL);
		if(!hasOnCreate){
			restoreParam();
			initCamera(cameraNumber);
		} else hasOnCreate = false;

	}

	@Override
	public void onDestroy(){
		super.onDestroy();
	}

	// on back pressed event
	@Override
	public void onBackPressed(){
		finish();
	}

	private Boolean initCamera(int position){
		try{
    		cameraNumber = position;
    		cam = HelperClass.getCameraInstance(cameraNumber);	
    		if(cam == null) {return false;}
    		isBack = HelperClass.isBackCamera(cameraNumber);
    		mCView = new CamView(MainActivity.this, cameraNumber, cam);	
    		camPreview.addView(mCView);
     		camPreview.addView(tvCamParameters);
    		setActionBarLogo();
			tvCamParameters.setGravity(Gravity.TOP | Gravity.LEFT);
    		tvCamParameters.setTextSize(TEXT_SIZE);
			tvCamParameters.setPadding(PADDING_LEFT, PADDING_TOP, PADDING_RIGHT, PADDING_BOTTOM);
			tvCamParameters.setTypeface(null, Typeface.BOLD);
    		setCameraParameters(cameraNumber);
		}catch(Exception e){return false;}
		return true;
	}

	// sets Camera focus mode and initial zoom, gets camera info
	public void setCameraParameters(int camNum){
		// external variables: i, tvCamParameters, cb, cam
		Camera.Parameters camParam = cam.getParameters();			
		if(camParam.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO))
			camParam.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO); 
		if(camParam.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_MACRO))
			camParam.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);

		/* recall shared preference block */
		restoreParam();
		//restoreParameters();
		try {				
			if(camParam.isZoomSupported()) {
				if(isBack) {
					camParam.setZoom(iBack);
					setZoomText(camParam, iBack);
				}else {
					camParam.setZoom(iFront);
					setZoomText(camParam, iFront);	
				}
			} else {
				tvCamParameters.setText(getResources().getString(R.string.zoom_is) 
										+ " " + getResources().getString(R.string.zoom_100));
			}
			
		}catch (NullPointerException ne){
			tvCamParameters.setText(getResources().getString(R.string.na));
		}
		
		cam.setParameters(camParam);	
	}

	// calculate distance between fingers on multitouch	
	private Double getTouchDist(float x1, float x0, float y1, float y0){
		return Math.sqrt(Math.pow(x1 - x0, 2) + Math.pow(y1 - y0, 2));
	}

	// get Neutral (positive) Difference
	private Double getPDif(float a, float b){
		return Math.sqrt(Math.pow(a - b, 2));
	}

    // subroutine to set ActionBar text
	private void setZoomText(Camera.Parameters param, int i)
	{
		tvCamParameters.setText(getResources().getString(R.string.zoom_is) + " " + param.getZoomRatios().get(i) 
								+ getResources().getString(R.string.percent));
	}

	// subroutine to set ActionBar logo
	private void setActionBarLogo()	
	{
		if(isBack){ 
			fab1.setImageResource(R.drawable.ic_camera_rear_white_48dp);
			bar.setTitle(getResources().getString(R.string.cam_magnifier_bk));
		}else {
			fab1.setImageResource(R.drawable.ic_camera_front_white_48dp);
			bar.setTitle(getResources().getString(R.string.cam_mirror_fr));
		}
	}
	
	// restore zoom rate and cam num
	private void restoreParam(){
		SharedPreferences sPref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
		iFront = sPref.getInt(SAVED_FRONT, 0);
		iBack = sPref.getInt(SAVED_BACK, 0);	
		cameraNumber = sPref.getInt(SAVED_NUM, 0);
	}

	//save zoom rate on exit
	private void saveParameters()
	{
		SharedPreferences sPref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
		Editor ed = sPref.edit();
		ed.putInt(SAVED_FRONT, iFront);
		ed.putInt(SAVED_BACK, iBack);
		ed.putInt(SAVED_NUM, cameraNumber);
		ed.commit();
	}

	private void setLightBtnImg(){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		fab.setImageResource(R.drawable.ic_flash_off_white_48dp);
		if(sp.getBoolean(getResources().getString(R.string.show_param2), false)) 
			fab.setImageResource(R.drawable.ic_flash_on_white_48dp);	
	}
	
	// picture callback for capturing image
	
	private PictureCallback mPicture = new PictureCallback(){

		@Override
		public void onPictureTaken(byte[] data, Camera camera)
		{
			// commented for new version test
			/* int rotation = getWindowManager().getDefaultDisplay().getRotation();
			savePicture(data);
			Intent intent = new Intent(getBaseContext(), PicViewActivity.class);
		
	    	intent.putExtra(HelperClass.STR_ROTATION,rotation).putExtra(HelperClass.STR_ISBACK,isBack);
			startActivity(intent); */
			
			long time = System.currentTimeMillis();
			File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), String.valueOf(time)+".png");
			if(saveFile(file, data)){
				addImageGallery(file);
				Intent intent = new Intent();
				intent.setAction(android.content.Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(file), "image/jpeg");
				try{
					startActivity(intent);
				}catch(Exception e){
					Toast.makeText(getBaseContext(),"No application found", Toast.LENGTH_SHORT).show();}
			}
		}
	};
	
	// shutter calback for capturing image
	
	private ShutterCallback mShutter = new ShutterCallback(){

		@Override
		public void onShutter()
		{
			// TODO: Implement this method
		}
	};
	
	// save data for Picture Viewer
	private void savePicture(byte[] data)
	{
		String filename = HelperClass.TMP_FILE;
		try
		{
			FileOutputStream fos = openFileOutput(filename, Context.MODE_PRIVATE);
			try
			{
				fos.write(data);
				fos.close();
			}
			catch (IOException e)
			{}
		}
		catch (FileNotFoundException e)
		{}
	}
	
	// test part 19.02.2017
	
	private boolean saveFile(File file, byte[] data){

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inMutable = true;
		int rotation = getWindowManager().getDefaultDisplay().getRotation();
		rotation = findDegrees(rotation);
		Log.d("myLogs", "orient = " + getDevDefOrient() + " rot = " + rotation);
		rotation = rotation + getDevDefOrient();
		Log.d("myLogs", "rot = " + rotation);
		Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length, options);
		Matrix matrix = new Matrix();
		if(!isBack) matrix.postScale(-1,1);
		matrix.postRotate(rotation);
		bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
		try {
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(file);
				//		File filePath = getFileStreamPath(HelperClass.TMP_FILE);
				//		BitmapFactory.decodeFile(filePath.getAbsolutePath()).compress(Bitmap.CompressFormat.JPEG, 100, fos);
				

				
				bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
			} finally {
				if (fos != null) fos.close();
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			//		Log.d("myLogs", "error " + e.getMessage());
			return false;
		}
	}
	
	private void addImageGallery(File file){
		ContentValues values = new ContentValues();
		values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
		values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
		getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
	}
	
	private int findDegrees(int orientation){
		int degrees = 0;
		switch (orientation) {
			case Surface.ROTATION_0: degrees = 0; break;
			case Surface.ROTATION_90: degrees = /*(isBack ? 270 : 90)*/ 90 - 2*getDevDefOrient(); break;
			case Surface.ROTATION_180: degrees = 180; break;
			case Surface.ROTATION_270: degrees = Math.abs(/*(isBack ? 90 : 270)*/ 90 - 2*getDevDefOrient()); break;
		}
		return degrees;
	}
	
	private int getDevDefOrient(){
		WindowManager wManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
		Configuration conf = getResources().getConfiguration();
		int rotation = wManager.getDefaultDisplay().getRotation();
		//	Log.d("myLogs", "rotation = " + rotation + ", orientation = " + conf.orientation);
		if(((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) &&
		   conf.orientation == Configuration.ORIENTATION_LANDSCAPE)
		   || ((rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) &&
		   conf.orientation == Configuration.ORIENTATION_PORTRAIT)) {
			//	return Configuration.ORIENTATION_LANDSCAPE;
		    return 0;
		}else {
			//	return Configuration.ORIENTATION_PORTRAIT;
			return 90;
		}
	}
	
}
