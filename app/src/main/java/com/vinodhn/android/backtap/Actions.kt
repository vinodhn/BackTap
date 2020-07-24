package com.vinodhn.android.backtap

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.provider.MediaStore

class Actions(context : Context?) {

    companion object {
        var mFlashlightState = false
        var mContext : Context? = null
    }

    init {
        Actions.Companion.mContext = context
    }

    fun switchFlashlight() {
        val cameraManager = Actions.Companion.mContext?.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = cameraManager.cameraIdList[0]

        try{
            if(!Actions.Companion.mFlashlightState){
                cameraManager.setTorchMode(cameraId, true)
            } else if (Actions.Companion.mFlashlightState) {
                cameraManager.setTorchMode(cameraId, false)
            }
        } catch (e:CameraAccessException){
            e.printStackTrace()
        }
    }

    fun openCamera() {
        val mLaunchCamera = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
        mLaunchCamera.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        Actions.Companion.mContext?.startActivity(mLaunchCamera)
    }

    fun openAssistant() {
        val mLaunchAssistant = Intent(Intent.ACTION_VOICE_COMMAND)
        mLaunchAssistant.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        Actions.Companion.mContext?.startActivity(mLaunchAssistant)
    }

}