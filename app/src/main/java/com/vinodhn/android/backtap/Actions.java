package com.vinodhn.android.backtap;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
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

    public void openCamera(){
        Intent mLaunchCamera = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
        mLaunchCamera.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(mLaunchCamera);
    }

    public void openAssistant(){
        Intent mLaunchAssistant = new Intent(Intent.ACTION_VOICE_COMMAND);
        mLaunchAssistant.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(mLaunchAssistant);
    }

}
