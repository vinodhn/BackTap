package com.vinodhn.android.backtap

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Camera
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraManager
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import androidx.preference.PreferenceManager
import java.util.*
import kotlin.math.abs
import kotlin.math.absoluteValue

class TapListenerAccessibilityService : AccessibilityService(), SensorEventListener {

    // Setting up variables

    private var mSensorManager : SensorManager? = null
    // Edit the following 4 variables to edit sensitivity in different ways
    var mThresholdZ : Float? = 1f
    val mThresholdX = 8f
    val mThresholdY = 8f
    val mUpdateFrequency = 100

    var mPreviousXValue = 0f
    var mCurrentXValue = 0f
    var mDeltaX = 0f

    var mPreviousYValue = 0f
    var mCurrentYValue = 0f
    var mDeltaY = 0f

    var mPreviousZValue = 0f
    var mCurrentZValue = 0f
    var mDeltaZ : Float? = 0f

    var mTimer: Timer? = null
    var mTapsDetected = 0

    var actions : Actions? = null

    var mDoubleTapActionId : Int? = null
    var mTripleTapActionId : Int? = null

    private var mSharedPreferences : SharedPreferences? = null

    private val TAG = "BackTap.TapListenerAccessibilityService"


    override fun onAccessibilityEvent(p0: AccessibilityEvent?) { }

    override fun onInterrupt() { stopAccelerometerSensing() }

    override fun onServiceConnected() {
        super.onServiceConnected()

        // First get access to the SharedPreferences to set the action IDs
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        mDoubleTapActionId = mSharedPreferences?.getInt(getString(R.string.double_tap_action_id),0)
        mTripleTapActionId = mSharedPreferences?.getInt(getString(R.string.triple_tap_action_id),0)
        val tapSens = mSharedPreferences?.getInt(getString(R.string.tap_sensitivity),2)
        when (tapSens) {
            0 -> mThresholdZ = 2f
            1 -> mThresholdZ = 1.5f
            2 -> mThresholdZ = 1f
            3 -> mThresholdZ = 0.875f
            4 -> mThresholdZ = 0.75f
        }

        // Set up access to our actions, flashlight, and accelerometer
        actions = Actions(this)

        val torchCallback : CameraManager.TorchCallback = object : CameraManager.TorchCallback() {
            override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
                super.onTorchModeChanged(cameraId, enabled)
                Actions.mFlashlightState = enabled
            }
        }

        val manager = applicationContext.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        manager.registerTorchCallback(torchCallback, null)

        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        resumeAccelerometerSensing()

    }

    // Stop reading sensor values once the service stops
    fun stopAccelerometerSensing(){ mSensorManager!!.unregisterListener(this) }

    fun resumeAccelerometerSensing(){ mSensorManager!!.registerListener(this, mSensorManager?.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), 1000000/mUpdateFrequency)}

    // Call on the actions based on number of taps detected
    fun triggerEvent(taps : Int) {
        if(taps == 2){
            triggerAction(mDoubleTapActionId!!)
        } else if (taps == 3){
            triggerAction(mTripleTapActionId!!)
        }
        mTapsDetected = 0
    }

    // Based on the given action ID, call of different actions
    fun triggerAction(mActionId : Int) {
        when (mActionId) {
            0 -> actions!!.switchFlashlight()
            1 -> performGlobalAction(GLOBAL_ACTION_HOME)
            2 -> performGlobalAction(GLOBAL_ACTION_BACK)
            3 -> performGlobalAction(GLOBAL_ACTION_RECENTS)
            4 -> performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT)
            5 -> actions!!.openCamera()
            6 -> actions!!.openAssistant()
        }
    }

    // If the user ever changes the actions while service is running, we want to make sure we have
    // the right IDs
    fun updateParams() {
        mDoubleTapActionId = mSharedPreferences!!.getInt(getString(R.string.double_tap_action_id),0)
        mTripleTapActionId = mSharedPreferences!!.getInt(getString(R.string.triple_tap_action_id),0)
        val tapSens = mSharedPreferences?.getInt(getString(R.string.tap_sensitivity),2)
        when (tapSens) {
            0 -> mThresholdZ = 2f
            1 -> mThresholdZ = 1.5f
            2 -> mThresholdZ = 1f
            3 -> mThresholdZ = 0.875f
            4 -> mThresholdZ = 0.75f
        }
    }

    // Called automatically when the accelerometer data changes
    override fun onSensorChanged(p0: SensorEvent?) {
        // Read the sensor values
        mPreviousXValue = mCurrentXValue
        mCurrentXValue = abs(p0!!.values[0])
        mDeltaX = mCurrentXValue - mPreviousXValue

        mPreviousYValue = mCurrentYValue
        mCurrentYValue = abs(p0!!.values[1])
        mDeltaY = mCurrentYValue - mPreviousYValue

        mPreviousZValue = mCurrentZValue
        mCurrentZValue = abs(p0!!.values[2])
        mDeltaZ = mCurrentZValue - mPreviousZValue

        // Update sensitivity threshold and action IDs
        updateParams()

        // Make sure that the movement is only within the Z axis and when the phone is upright
        if (mCurrentZValue > mPreviousZValue && mDeltaZ!! > mThresholdZ!! && mDeltaX < mThresholdX && mDeltaY < mThresholdY && mCurrentXValue < 1 && mCurrentYValue < 10 && mCurrentYValue > 0) {
            Log.d(TAG, "onSensorChanged: Tap Detected ***** " + mTapsDetected)
            Log.d(TAG, "onSensorChanged: SENSITIVITY: " + mThresholdZ)
            if(mTapsDetected == 0){
                mTimer = Timer()
                mTimer!!.schedule(object : TimerTask() {
                    override fun run() {
                        triggerEvent(mTapsDetected)
                    }
                },700)
            }
            mTapsDetected++
        }

    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) { }
}