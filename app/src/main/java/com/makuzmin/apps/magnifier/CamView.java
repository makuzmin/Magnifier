package com.makuzmin.apps.magnifier;

import android.view.*;
import android.content.*;
import android.hardware.*;
import android.view.InputQueue.*;
import java.io.*;
import android.app.*;
import android.preference.*;
import android.hardware.Camera.CameraInfo;
import android.util.*;

public class CamView extends SurfaceView
{
	private SurfaceHolder mHold;
	private Camera mCam;
//	Camera.AutoFocusCallback cb;
	MainActivity activity;
	int camNum;

	public CamView(MainActivity act, int cameraId, Camera camera){
		super(act.getBaseContext());
		mCam = camera;
		activity = act;
		camNum = cameraId;

		mHold = getHolder();
		mHold.addCallback(new SurfaceHolder.Callback(){

				@Override
				public void surfaceCreated(SurfaceHolder holder)
				{
					try {
						mCam.setPreviewDisplay(holder);
						mCam.startPreview();
					} catch (IOException e) {
						//error
					} catch (RuntimeException re){}
//					mCam.autoFocus(cb);
				}

				@Override
				public void surfaceChanged(SurfaceHolder holder, int format, int w, int h)
				{
					// If your preview can change or rotate, take care of those events here.
					// Make sure to stop the preview before resizing or reformatting it.

					if (mHold.getSurface() == null){
						// preview surface does not exist
						return;
					}

					// stop preview before making changes
					try {
						mCam.stopPreview();
					} catch (Exception e){
						// ignore: tried to stop a non-existent preview
					}

					setCameraDisplayOrientation(activity, camNum, mCam);
					setFlashlight(activity, camNum, mCam);

					// set preview size and make any resize, rotate or
					// reformatting changes here

					// start preview with new settings
					try {
						mCam.setPreviewDisplay(mHold);
						mCam.startPreview();

					} catch (Exception e){
						// error
					}
//					mCam.autoFocus(cb);
				}

				@Override
				public void surfaceDestroyed(SurfaceHolder p1)
				{
					// TODO: Implement this method
				}
			});
		mHold.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public static void setCameraDisplayOrientation(Activity activity,
												   int cameraId, Camera camera) {
		Camera.CameraInfo info = new Camera.CameraInfo();
		Camera.getCameraInfo(cameraId, info);
		int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
		int degrees = 0;
		switch (rotation) {
			case Surface.ROTATION_0: degrees = 0; break;
			case Surface.ROTATION_90: degrees = 90; break;
			case Surface.ROTATION_180: degrees = 180; break;
			case Surface.ROTATION_270: degrees = 270; break;
		}

		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360;  // compensate the mirror
		} else {  // back-facing
			result = (info.orientation - degrees + 360) % 360;
		}
		camera.setDisplayOrientation(result);

	}

	/* starts or stops flashlight */
	public static void setFlashlight(Activity activity, int cameraId, Camera camera){
		try{
    		Camera.Parameters p = camera.getParameters();	
    		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
    		CameraInfo info = new Camera.CameraInfo();
    		Camera.getCameraInfo(cameraId, info);
    		try{
        		if(info.facing == Camera.CameraInfo.CAMERA_FACING_BACK){
        			if(sp.getBoolean(activity.getResources().getString(R.string.show_param2), false)) {
    	        		p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
    	       		}else{
    					p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
	    			}
    				camera.setParameters(p);
    			}
    		}catch(Exception e) {}
		}catch(RuntimeException re){
			return;
		}
	}	
}
