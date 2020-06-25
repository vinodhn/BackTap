package com.vinodhn.android.backtap;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

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

    public void stopAccelerometerSensing(){
        mSensorManager.unregisterListener(this);
    }

    public void resumeAccelerometerSensing(){
        if(mSensorManager != null){
            Log.d("SENSOR", "-------------------NOT NULL");
        }
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), 1000000/mUpdateFrequency);
    }

    public void triggerEvent(int taps){
        Log.d("AYYYYY", "triggerEvent: ******KNOCK*****");
        Log.d("KNOCKS", "run: " + mTapsDetected);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, "BackTapServiceChannel")
                .setContentTitle("Tap Event Triggered")
                .setContentText("Taps detected: " + taps)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(pendingIntent).build();

        NotificationManagerCompat.from(this).notify(1, notification);

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
            Log.d("BackTap", "onSensorChanged: tap szn");
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

        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, "BackTapServiceChannel")
                .setContentTitle("BackTap Foreground Service")
                .setContentText("Keeps backtap service running")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(pendingIntent).build();

        startForeground(1, notification);

        Log.d("BackTap", "onCreate: Service Started");
        Toast.makeText(this.getApplicationContext(), "SERVICE TOAST TEST", Toast.LENGTH_SHORT).show();
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
        Toast.makeText(getApplicationContext(), "stopped", Toast.LENGTH_SHORT).show();
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
