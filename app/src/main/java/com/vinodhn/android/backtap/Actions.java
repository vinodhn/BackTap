package com.vinodhn.android.backtap;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.Date;
import java.util.List;

public class Actions {

    // Variable set up.s
    public static boolean mFlashlightState = false;
    public static Context mContext;

    // TAG for log messages.
    private final String TAG = "BackTap.Actions";

    // Get application context to enable various actions.
    public Actions(Context context){
        mContext = context;
    }

    // Call on necessary methods based on action ID passed from Tap Listener Service.
    public void triggerAction(int mActionId){
        switch(mActionId){
            case 0:
                switchFlashlight();
                break;
            case 1:
                takeScreenshot();
                break;
            case 2:
                openCamera();
                break;
        }
    }

    public void switchFlashlight(){
        // If flashlight is off
        if(!mFlashlightState){
            // Get access to camera system and then turn on flashlight.
            CameraManager cameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
            try {
                String cameraId = cameraManager.getCameraIdList()[0];

                cameraManager.setTorchMode(cameraId, true);
            }catch (CameraAccessException e){
                e.printStackTrace();
            }
        }
        // If flashlight is on
        else {
            // Get access to camera system and then turn off flashlight.
            CameraManager cameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
            try {
                String cameraId = cameraManager.getCameraIdList()[0];

                cameraManager.setTorchMode(cameraId, false);
            }catch (CameraAccessException e){
                e.printStackTrace();
            }
        }
    }

    public void takeScreenshot(){
        // Currently does not function as name implies.
        Date mCurrentTime = new Date();
        DateFormat.format("yyyy-MM-dd_hh:mm:ss", mCurrentTime);
        Log.d(TAG, "triggerAction: takeScreenshot");
    }

    public void openCamera(){
        // Search phone for correct package
        Intent mPackageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> mCameraList = mContext.getPackageManager().queryIntentActivities(mPackageIntent, 0);
        ResolveInfo mCameraPackage = mCameraList.get(0);

        // And launch said package.
        Intent mLaunchCamera = mContext.getPackageManager().getLaunchIntentForPackage(mCameraPackage.activityInfo.packageName);
        mLaunchCamera.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(mLaunchCamera);

    }

}
