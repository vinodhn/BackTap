package com.vinodhn.android.backtap;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static SensorManager mSensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

    }

    public void run(View v){

        Toast.makeText(this, "Started Service", Toast.LENGTH_SHORT).show();
        Intent serviceIntent = new Intent(this, TapListenerService.class);
        startForegroundService(serviceIntent);

    }

    public void stop(View v){

        Toast.makeText(this, "Stopped Service", Toast.LENGTH_SHORT).show();
        Intent serviceIntent = new Intent(this, TapListenerService.class);
        stopService(serviceIntent);
    }
}
