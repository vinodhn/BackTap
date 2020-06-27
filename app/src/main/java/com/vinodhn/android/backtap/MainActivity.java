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

    // Variable Setup
    public static SensorManager mSensorManager;
    public static Spinner mDoubleTapActionSpinner, mTripleTapActionSpinner;
    public static SharedPreferences mSharedPreferences;

    // TAG for any necessary Log messages.
    private final String TAG = "BackTap.MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up access to system sensors.
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Set up the drop down menus on main screen.
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

        // Set up access to application preferences and set drop downs to previously saved selections.
        mSharedPreferences = getSharedPreferences("com.vinodhn.android.backtap", Context.MODE_PRIVATE);
        mDoubleTapActionSpinner.setSelection(mSharedPreferences.getInt(getString(R.string.double_tap_action_id),0));
        mTripleTapActionSpinner.setSelection(mSharedPreferences.getInt(getString(R.string.triple_tap_action_id),0));

    }

    public void run(View v){
        // Start service and let user know service is up and running.
        Toast.makeText(this, "Started BackTap Service", Toast.LENGTH_SHORT).show();
        Intent serviceIntent = new Intent(this, TapListenerService.class);
        startForegroundService(serviceIntent);

    }

    public void stop(View v){
        // Stop service and let user know service has been stopped.
        Toast.makeText(this, "Stopped BackTap Service", Toast.LENGTH_SHORT).show();
        Intent serviceIntent = new Intent(this, TapListenerService.class);
        stopService(serviceIntent);
    }

    // Item selected handler for drop down menus.
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        // Essentially set up editor for application preferences file and edit on the fly.
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
