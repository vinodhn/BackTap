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

    public volatile boolean mSpikeDetected = false;
    private SensorManager mSensorManager;
    final public float mThresholdZ = 2;
    final public float mThresholdX = 8;
    final public float mThresholdY = 8;
    final public int mUpdateFrequency = 100;
    private Context mContext;

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

    private final String TAG = "BackTap.TapListenerService";

    public void stopAccelerometerSensing(){
        mSensorManager.unregisterListener(this);
    }

    public void resumeAccelerometerSensing(){
        if(mSensorManager != null){
            Log.d(TAG, "SensorManager is not null.");
        }
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), 1000000/mUpdateFrequency);
    }

    public void triggerEvent(int taps){
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

        if(mCurrentZValue > mPreviousZValue && mDeltaZ > mThresholdZ && mDeltaX < mThresholdX && mDeltaY < mThresholdY && mCurrentXValue < 1 && mCurrentYValue < 10){
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
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        SharedPreferences mSharedPreferences = getSharedPreferences("com.vinodhn.android.backtap", Context.MODE_PRIVATE);
        mDoubleTapActionId = mSharedPreferences.getInt(getString(R.string.double_tap_action_id),0);
        mTripleTapActionId = mSharedPreferences.getInt(getString(R.string.triple_tap_action_id),0);

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

        CameraManager.TorchCallback torchCallback = new CameraManager.TorchCallback() {
            @Override
            public void onTorchModeChanged(@NonNull String cameraId, boolean enabled) {
                super.onTorchModeChanged(cameraId, enabled);
                actions.mFlashlightState = enabled;
            }
        };
        CameraManager manager = (CameraManager) getApplicationContext().getSystemService(Context.CAMERA_SERVICE);
        manager.registerTorchCallback(torchCallback, null);

        Log.d(TAG, "onCreate: Service Started");
        mSensorManager = MainActivity.mSensorManager;
        resumeAccelerometerSensing();

        return START_REDELIVER_INTENT;
    }

    private void createNotificationChannel(){
        NotificationChannel serviceChannel = new NotificationChannel("BackTapServiceChannel", "BackTap Service Channel", NotificationManager.IMPORTANCE_UNSPECIFIED);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(serviceChannel);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        stopAccelerometerSensing();
    }

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
