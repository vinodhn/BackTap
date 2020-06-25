package com.vinodhn.android.backtap;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    public static SensorManager mSensorManager;
    public static Spinner mDoubleTapActionSpinner, mTripleTapActionSpinner;
    public static SharedPreferences mSharedPreferences;

    private final String TAG = "BackTap.MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        mDoubleTapActionSpinner = findViewById(R.id.DoubleTapActionSpinner);
        mTripleTapActionSpinner = findViewById(R.id.TripleTapActionSpinner);

        ArrayAdapter<CharSequence> mDoubleTapActionAdapter = ArrayAdapter.createFromResource(this, R.array.actions_array, R.layout.spinner_item);
        ArrayAdapter<CharSequence> mTripleTapActionAdapter = ArrayAdapter.createFromResource(this, R.array.actions_array, R.layout.spinner_item);

        mDoubleTapActionAdapter.setDropDownViewResource(R.layout.spinner_item);
        mTripleTapActionAdapter.setDropDownViewResource(R.layout.spinner_item);

        mDoubleTapActionSpinner.setAdapter(mDoubleTapActionAdapter);
        mTripleTapActionSpinner.setAdapter(mTripleTapActionAdapter);

        mDoubleTapActionSpinner.setOnItemSelectedListener(this);
        mTripleTapActionSpinner.setOnItemSelectedListener(this);

        mSharedPreferences = getSharedPreferences("com.vinodhn.android.backtap", Context.MODE_PRIVATE);

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

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        switch(adapterView.getId()){
            case R.id.DoubleTapActionSpinner:
                mEditor.putInt(getString(R.string.double_tap_action_id), i);
                break;
            case R.id.TripleTapActionSpinner:
                mEditor.putInt(getString(R.string.triple_tap_action_id), i);
                break;
        }
        mEditor.commit();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        Toast.makeText(getApplicationContext(), "Please select a new option.", Toast.LENGTH_SHORT).show();
    }
}
