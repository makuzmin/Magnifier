package com.makuzmin.apps.magnifier;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
//addition
import java.io.File;
import android.view.Surface;

public class DrawActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(new DrawView(this));
	}

	class DrawView extends View {

		Paint paint;
		Bitmap bitmap;
		Rect rectSrc;
		Rect rectDst;
		Matrix matrix;
		//addition
		Boolean isBack;

		public DrawView(Context context) {
			super(context);
			paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			
			//addition
			int camrotate = getIntent().getExtras().getInt("rotation");
			isBack = getIntent().getExtras().getBoolean("backcamera");
			File filePath = getFileStreamPath("tempstor");

			bitmap = BitmapFactory.decodeFile(filePath.getAbsolutePath());
			//end addition

		//	bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);

			String info = 
				String.format("Info: size = %s x %s, bytes = %s (%s), config = %s",
							  bitmap.getWidth(), 
							  bitmap.getHeight(),
							  bitmap.getByteCount(), 
							  bitmap.getRowBytes(),
							  bitmap.getConfig());
			Log.d("log", info);

			matrix = new Matrix();
			//addition
			if(!isBack) matrix.postScale(-1, 1); 
     		camrotate = findDegrees(camrotate);
			matrix.postRotate(camrotate);
			//end addition
			
	//		matrix.postRotate(45);
	//		matrix.postScale(1, 1);
	//		matrix.postTranslate(200, 50);

			rectSrc = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
			rectDst = new Rect(-1280+1280/2, -960+960/2, 1280+1280/2, 960+960/2);      
		}

		@Override
		protected void onDraw(Canvas canvas) {
			canvas.drawARGB(80, 102, 204, 255);
	//		canvas.drawBitmap(bitmap, 50, 50, paint);
	//		canvas.drawBitmap(bitmap, matrix, paint);
			canvas.drawBitmap(bitmap, rectSrc, rectDst, paint);
		}

	}
	//addition
	private int findDegrees(int orientation){
		int degrees = 0;
		switch (orientation) {
			case Surface.ROTATION_0: degrees = 0; break;
			case Surface.ROTATION_90: degrees = 270; break;
			case Surface.ROTATION_180: degrees = 180; break;
			case Surface.ROTATION_270: degrees = 90; break;
		}
		return degrees;
	}
}
