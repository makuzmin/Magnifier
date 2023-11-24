package com.makuzmin.apps.magnifier;
//import android.app.*;
import android.os.*;
import android.widget.*;
import java.io.*;
import android.graphics.*;
import android.view.*;
import android.view.View.OnTouchListener;
import android.util.*;
import android.support.v7.app.*;
import com.makuzmin.apps.magnifier.ScrollImageView.ScaleFinishedListener;
import android.content.*;
import android.provider.*;
import android.content.res.*;

public class PicViewActivity extends AppCompatActivity implements OnTouchListener, ScaleFinishedListener
{

	@Override
	public void onScaleFinished(float factor)
	{
		if(bar != null) // bar.setTitle((int)(factor) + "X");
		bar.setTitle(getResources().getString(R.string.zoom_is) + " " + (int)(factor*100)
			+ getResources().getString(R.string.percent));
	}
	

	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		iv.onTouchEvent(event);
	//	Log.d("myLogs", "onTouch()");
		return true;
	}
	
	final int API_LEVEL = 14;
	
	
//	TextView tv;
	ScrollImageView iv;
	Bitmap bitmap;
	Boolean isBack;
	float factor;
	private Matrix matrix;
	
	ActionBar bar;
	
	@Override
    protected void onCreate(Bundle savedInstanceState)
	{	
	
	    int scrWidth=((WindowManager)this.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
		int scrHeight=((WindowManager)this.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getHeight();

		iv  = new ScrollImageView(this);
		matrix = new Matrix();
        super.onCreate(savedInstanceState);
		setContentView(iv);
		
		
	//	Log.d("myLogs", "def orient = " +getDevDefOrient());
	
 //       setContentView(R.layout.picview);	
		
		bar = getSupportActionBar();
		if(bar != null)
			if(Build.VERSION.SDK_INT >= API_LEVEL)
				bar.setBackgroundDrawable(getResources().getDrawable(R.drawable.textured));
			else
				bar.setBackgroundDrawable(getResources().getDrawable(R.drawable.ab_solid_example));
		
		
		
		factor = 1;
		isBack = getIntent().getExtras().getBoolean(HelperClass.STR_ISBACK);
		int camrotate = getIntent().getExtras().getInt(HelperClass.STR_ROTATION);
	//	Log.d("myLogs", "original rotation = " + findDegrees(camrotate));
		
		
		
	//	tv = (TextView) findViewById(R.id.picview_tv);
		
		try{
		
		File filePath = getFileStreamPath(HelperClass.TMP_FILE);
		memStatus();
		bitmap = decodeSampledBitmapFromResource(filePath.getAbsolutePath(), scrWidth, scrHeight);
		memStatus();
	//		Log.d("myLogs", "bitmap size = " + bitmap.getWidth() + "x" + bitmap.getHeight());
		
		//iv = (ImageView) findViewById(R.id.picview_iv);
		iv.setOnTouchListener(this);
	//	if(!isBack) iv.setScaleX(-1.0f);
	
		camrotate = findDegrees(camrotate);
		
		camrotate = camrotate + getDevDefOrient();
	//	Log.d("myLogs", "default orientation = " + getDevDefOrient());
	//	Log.d("myLogs", "modified rotation = " + camrotate);
	//	matrix.postRotate(camrotate);

	//	iv.setRotation(camrotate);
	    matrix = new Matrix();
		if(!isBack) matrix.postScale(-1,1);
		matrix.postRotate(camrotate);
		
	    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
	//	bitmap = Bitmap.createBitmap(BitmapFactory.decodeFile(filePath.getAbsolutePath()), 0, 0, 
	//		BitmapFactory.decodeFile(filePath.getAbsolutePath()).getWidth(), 
	//		BitmapFactory.decodeFile(filePath.getAbsolutePath()).getHeight(), matrix, true);
        memStatus();
   
	//	iv.setImage(bitmap, (isBack ? 1 : 1));
	    iv.setImage(bitmap);
		
	//	setContentView(iv);
	
		}catch(OutOfMemoryError ome){this.finish();}
		catch(NullPointerException ne){this.finish();}
    	
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		getMenuInflater().inflate(R.menu.menu1, menu);
		return super.onCreateOptionsMenu(menu);
	}

	 @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			
			case R.id.menu_exit:
				finish();
				return true;
			case R.id.menu_save:
				long time = System.currentTimeMillis();
				 File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), String.valueOf(time)+".png");
				 if(saveFile(file)){
		    		 addImageGallery(file);
					 Toast.makeText(getBaseContext(), getResources().getString(R.string.saved) + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
				 }else{Toast.makeText(getBaseContext(), getResources().getString(R.string.not_saved), Toast.LENGTH_SHORT).show();}
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}	
	
	private int findDegrees(int orientation){
		int degrees = 0;
		switch (orientation) {
			case Surface.ROTATION_0: degrees = 0; break;
			case Surface.ROTATION_90: degrees = Math.abs(/*(isBack ? 270 : 90)*/ 270 - 2*getDevDefOrient()); break;
			case Surface.ROTATION_180: degrees = 180; break;
			case Surface.ROTATION_270: degrees = Math.abs(/*(isBack ? 90 : 270)*/ 90 - 2*getDevDefOrient()); break;
		}
		return degrees;
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		bitmap = null;

	}
	
	private void memStatus(){
	//	Log.d("myLogs", "total memory = " + (int)(Runtime.getRuntime().totalMemory()/1024)
	//    	+ ", free memory = " +(int)(Runtime.getRuntime().freeMemory()/1024));
		
	}
	
	public static Bitmap decodeSampledBitmapFromResource(String path,
														 int reqWidth, int reqHeight) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
	//	options.inPreferredConfig = Bitmap.Config.RGB_565;
		return BitmapFactory.decodeFile(path, options);
	}
	
	public static int calculateInSampleSize(
		BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height; // / 2;
			final int halfWidth = width; // / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
				   && (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

	//	Log.d("myLogs", "inSampleSize = " + inSampleSize);
		return inSampleSize;
	}
	
	private void addImageGallery(File file){
		ContentValues values = new ContentValues();
		values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
		values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
		getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
	}
	
	private boolean saveFile(File file){
		

		try {
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(file);
		//		File filePath = getFileStreamPath(HelperClass.TMP_FILE);
		//		BitmapFactory.decodeFile(filePath.getAbsolutePath()).compress(Bitmap.CompressFormat.JPEG, 100, fos);
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
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
	
//	public Bitmap getResizedBitmap(Bitmap bm, float factor)
//	{
//		
//		int width = bm.getWidth();
//		int height = bm.getHeight();
//	
//		// create a matrix for the manipulation
//		Matrix matrix = new Matrix();
//		// resize the bit map
//		matrix.postScale(factor, factor);
//		// recreate the new Bitmap
//		try{
//		bm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
//		}catch(OutOfMemoryError oe){Log.d("myLogs", "out of memory error");}
//		return bm;
//	}
//	
}
