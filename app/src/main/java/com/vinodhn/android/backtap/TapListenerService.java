package com.vinodhn.android.backtap;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import static java.lang.Math.abs;
import static java.lang.Math.log;

public class TapListenerService extends Service implements SensorEventListener {

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

    // Tag for any necessary log messages.
    private final String TAG = "BackTap.TapListenerService";

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
            actions.triggerAction(mDoubleTapActionId);
        } else if(taps >=3){
            actions.triggerAction(mTripleTapActionId);
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
                        triggerEvent(mTapsDetected);
                    }
                }, 700);
            }
            mTapsDetected++;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        // This is called when the service is first called.

        // Read application preferences and set action ID's to what the user has set them to.
        SharedPreferences mSharedPreferences = getSharedPreferences("com.vinodhn.android.backtap", Context.MODE_PRIVATE);
        mDoubleTapActionId = mSharedPreferences.getInt(getString(R.string.double_tap_action_id),0);
        mTripleTapActionId = mSharedPreferences.getInt(getString(R.string.triple_tap_action_id),0);

        // Set up notification to start foreground service to avoid being killed by the system's battery and RAM management.
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, "BackTapServiceChannel")
                .setContentTitle("BackTap Service Notification")
                .setContentText("This ensures the BackTap services remains running.")
                .setSmallIcon(R.drawable.ic_tap)
                .setContentIntent(pendingIntent).build();

        startForeground(1, notification);

        actions = new Actions(getApplicationContext());

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
        mSensorManager = MainActivity.mSensorManager;
        resumeAccelerometerSensing();

        return START_REDELIVER_INTENT;
    }

    // Helps with creating the notification to avoid being killed.
    private void createNotificationChannel(){
        NotificationChannel serviceChannel = new NotificationChannel("BackTapServiceChannel", "BackTap Service Channel", NotificationManager.IMPORTANCE_UNSPECIFIED);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(serviceChannel);
    }

    // When the user decides to stop the service, this gets called.
    @Override
    public void onDestroy(){
        super.onDestroy();
        stopAccelerometerSensing();
    }

    // When user clears app from recent apps screen, we need to restart the service in the background to keep sensing.x
    @Override
    public void onTaskRemoved(Intent rootIntent){
        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
        restartServiceIntent.setPackage(getPackageName());

        PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 100, restartServicePendingIntent);

        super.onTaskRemoved(rootIntent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
