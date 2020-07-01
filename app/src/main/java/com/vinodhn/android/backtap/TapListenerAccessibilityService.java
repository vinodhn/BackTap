package com.vinodhn.android.backtap;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import static java.lang.Math.abs;

public class TapListenerAccessibilityService extends AccessibilityService implements SensorEventListener {

    // Variable set up
    private SensorManager mSensorManager;
    final public float mThresholdZ = 2;
    final public float mThresholdX = 8;
    final public float mThresholdY = 8;
    final public int mUpdateFrequency = 100;

    // Need 3 for each axis.
    private float mPreviousZValue = 0;
    private float mCurrentZValue = 0;
    private float mDeltaZ = 0;

    private float mPreviousXValue = 0;
    private float mCurrentXValue = 0;
    private float mDeltaX = 0;

    private float mPreviousYValue = 0;
    private float mCurrentYValue = 0;
    private float mDeltaY = 0;

    public Timer mTimer;
    public int mTapsDetected = 0;

    private Actions actions;
    public int mDoubleTapActionId, mTripleTapActionId;
    private SharedPreferences mSharedPreferences;

    // Tag for any necessary log messages.
    private final String TAG = "BackTap.TapListenerAccessibilityService";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

    }

    @Override
    public void onInterrupt() {
        stopAccelerometerSensing();
    }
    
    @Override
    public void onServiceConnected(){
        super.onServiceConnected();
        Log.d(TAG, "onServiceConnected: Service connected");
        Toast.makeText(this, "BackTap Service Connected.", Toast.LENGTH_SHORT).show();
        // This is called when the service is first called.

        // Read application preferences and set action ID's to what the user has set them to.
        mSharedPreferences = getSharedPreferences("com.vinodhn.android.backtap", Context.MODE_PRIVATE);
        mDoubleTapActionId = mSharedPreferences.getInt(getString(R.string.double_tap_action_id),0);
        mTripleTapActionId = mSharedPreferences.getInt(getString(R.string.triple_tap_action_id),0);

        actions = new Actions(this);

        // Set up access to the flashlight.
        CameraManager.TorchCallback torchCallback = new CameraManager.TorchCallback() {
            @Override
            public void onTorchModeChanged(@NonNull String cameraId, boolean enabled) {
                super.onTorchModeChanged(cameraId, enabled);
                actions.mFlashlightState = enabled;
            }
        };
        CameraManager manager = (CameraManager) getApplicationContext().getSystemService(Context.CAMERA_SERVICE);
        manager.registerTorchCallback(torchCallback, null);

        // Gain accesss to sensors and start service.
        Log.d(TAG, "onCreate: Service Started");
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        resumeAccelerometerSensing();
    }

    // Stop reading data from accelerometer.
    public void stopAccelerometerSensing(){
        mSensorManager.unregisterListener(this);
    }

    // Start or resume reading data from accelerometer.
    public void resumeAccelerometerSensing(){
        // Make sure that we have access to sensors first.
        if(mSensorManager != null){
            Log.d(TAG, "SensorManager is not null.");
        }
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), 1000000/mUpdateFrequency);
    }

    // Call on user selected event based on saved action IDs.
    public void triggerEvent(int taps){
        // Just lets us know that it has detected a knock.
        Log.d(TAG, "triggerEvent: ******KNOCK*****");
        Log.d(TAG, "run: " + mTapsDetected);

        if(taps == 2){
            triggerAction(mDoubleTapActionId);
        } else if(taps >=3){
            triggerAction(mTripleTapActionId);
        }

        mTapsDetected = 0;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // This is where the accelerometer data is parsed and has calculations done on.
        // First we must calculate the delta's in the three axes.

        mPreviousXValue = mCurrentXValue;
        mCurrentXValue = abs(sensorEvent.values[0]);
        mDeltaX = mCurrentXValue - mPreviousXValue;

        mPreviousYValue = mCurrentYValue;
        mCurrentYValue = abs(sensorEvent.values[1]);
        mDeltaY = mCurrentYValue - mPreviousYValue;

        mPreviousZValue = mCurrentZValue;
        mCurrentZValue = abs(sensorEvent.values[2]);
        mDeltaZ = mCurrentZValue - mPreviousZValue;

        // These just make sure the phone is in the right orientation and are hardcoded with a certain sensitivity.
        if(mCurrentZValue > mPreviousZValue && mDeltaZ > mThresholdZ && mDeltaX < mThresholdX && mDeltaY < mThresholdY && mCurrentXValue < 1 && mCurrentYValue < 10 && mCurrentYValue > 0){
            Log.d(TAG, "onSensorChanged: tap detected");
            if(mTapsDetected == 0){
                mTimer = new Timer();
                mTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        updateActionId();
                        triggerEvent(mTapsDetected);
                    }
                }, 700);
            }
            mTapsDetected++;
        }
    }

    // Call on necessary methods based on action ID passed from Tap Listener Service.
    public void triggerAction(int mActionId){
        switch(mActionId){
            case 0:
                actions.switchFlashlight();
                break;
            case 1:
                performGlobalAction(GLOBAL_ACTION_HOME);
                break;
            case 2:
                performGlobalAction(GLOBAL_ACTION_BACK);
                break;
            case 3:
                performGlobalAction(GLOBAL_ACTION_RECENTS);
                break;
            case 4:
                performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT);
                break;
            case 5:
                actions.openCamera();
                break;
            case 6:
                actions.openAssistant();
                break;
        }
    }

    public void updateActionId(){
        // Read application preferences and update action ID's to what the user has set them to.
        mDoubleTapActionId = mSharedPreferences.getInt(getString(R.string.double_tap_action_id),0);
        mTripleTapActionId = mSharedPreferences.getInt(getString(R.string.triple_tap_action_id),0);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}
}
