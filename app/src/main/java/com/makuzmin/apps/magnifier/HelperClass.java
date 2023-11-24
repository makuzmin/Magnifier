package com.makuzmin.apps.magnifier;

import android.content.*;
import android.content.pm.*;
import java.util.*;
import android.hardware.*;
import android.hardware.Camera.CameraInfo;

public class HelperClass
{
	
	public static final String TMP_FILE = "tempstor";
	public static final String STR_ROTATION = "rotation";
	public static final String STR_ISBACK = "backcamera";
	
	public static boolean checkCameraHardware(Context context) {
		if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
			// this device has a camera
			return true;
		} else {
			// no camera on this device
			return false;
		}
	}

	public static List<String> getCameraList(Context context){
		ArrayList<String> data = new ArrayList<String>();
		for(int n=0; n <= Camera.getNumberOfCameras() -1; n++){
			CameraInfo cInfo = new Camera.CameraInfo();
			Camera.getCameraInfo(n, cInfo);
			switch(cInfo.facing){
				case CameraInfo.CAMERA_FACING_FRONT:
					data.add(context.getResources().getString(R.string.cam_mirror_fr));
					break;
				case CameraInfo.CAMERA_FACING_BACK:
					data.add(context.getResources().getString(R.string.cam_magnifier_bk));
					break;
			}
		}
		return data;
	}

	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance(int n){
		Camera c = null;
		try {
			c = Camera.open(n); // attempt to get a Camera instance
		}
		catch (Exception e){
			// Camera is not available (in use or does not exist)
		}
		return c; // returns null if camera is unavailable
	}

	/* gets camera facing info*/
	public static boolean isBackCamera(int camNum)
	{
		CameraInfo camInfo = new Camera.CameraInfo();
		Camera.getCameraInfo(camNum, camInfo);

		if(camInfo.facing == CameraInfo.CAMERA_FACING_BACK) return true;
		return false;

	}

}
