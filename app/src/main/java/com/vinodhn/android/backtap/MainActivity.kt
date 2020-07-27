package com.vinodhn.android.backtap

import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat

import android.os.Bundle
import android.provider.Settings
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceManager

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction().replace(R.id.settings_main, SettingsFragment()).commit()
        supportActionBar?.setTitle("BackTap Action Settings");
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val mDoubleTapActionPreference = findPreference<ListPreference>("double")
            val mTripleTapActionPreference = findPreference<ListPreference>("triple")
            val mSensibilityPreference = findPreference<Preference>("sensitivity")
            val mAccessibilityPreference = findPreference<Preference>("accessibilitySettings")
            val mAboutPreference = findPreference<Preference>("About")
            val mSharedPreference:SharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
            val mEditor:SharedPreferences.Editor = mSharedPreference.edit()

            when (mSharedPreference.getInt(getString(R.string.tap_sensitivity),2)) {
                0 -> mSensibilityPreference!!.summary = "Low"
                1 -> mSensibilityPreference!!.summary = "Low-Medium"
                2 -> mSensibilityPreference!!.summary = "Medium"
                3 -> mSensibilityPreference!!.summary = "Medium-High"
                4 -> mSensibilityPreference!!.summary = "High"
            }

            mDoubleTapActionPreference?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener {preference, newValue ->

                if(mDoubleTapActionPreference != null) {
                    mEditor.putInt(getString(R.string.double_tap_action_id), mDoubleTapActionPreference.findIndexOfValue(newValue.toString()))
                    mEditor.commit()
                }

                true
            }

            mTripleTapActionPreference?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener {preference, newValue ->

                if(mTripleTapActionPreference != null){
                    mEditor.putInt(getString(R.string.triple_tap_action_id), mTripleTapActionPreference.findIndexOfValue(newValue.toString()))
                    mEditor.commit()
                }

                true
            }

            mSensibilityPreference?.setOnPreferenceClickListener { preference ->

                val mBuilder:AlertDialog.Builder? = activity?.let { AlertDialog.Builder(it) }
                val mInflater = activity?.layoutInflater

                mBuilder!!.setView(mInflater!!.inflate(R.layout.sensibility_dialog, null))

                val mDialog:AlertDialog? = mBuilder?.create()
                mDialog?.show()

                val mSeekBar = mDialog?.findViewById<SeekBar>(R.id.sensitivitySeekbar)
                mSeekBar?.progress = mSharedPreference.getInt(getString(R.string.tap_sensitivity),2)
                when (mSeekBar?.progress) {
                    0 -> mSensibilityPreference.summary = "Low"
                    1 -> mSensibilityPreference.summary = "Low-Medium"
                    2 -> mSensibilityPreference.summary = "Medium"
                    3 -> mSensibilityPreference.summary = "Medium-High"
                    4 -> mSensibilityPreference.summary = "High"
                }

                mSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {}
                    override fun onStartTrackingTouch(seek: SeekBar) {}
                    override fun onStopTrackingTouch(seek: SeekBar) {
                        mEditor.putInt(getString(R.string.tap_sensitivity), seek.progress)
                        mEditor.commit()

                        when (mSeekBar?.progress) {
                            0 -> mSensibilityPreference.summary = "Low"
                            1 -> mSensibilityPreference.summary = "Low-Medium"
                            2 -> mSensibilityPreference.summary = "Medium"
                            3 -> mSensibilityPreference.summary = "Medium-High"
                            4 -> mSensibilityPreference.summary = "High"
                        }
                    }
                })

                false
            }



            mAccessibilityPreference?.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference ->

                val mLaunchSettings = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(mLaunchSettings)

                false
            }

            mAboutPreference?.setOnPreferenceClickListener { preference ->

                val mBuilder:AlertDialog.Builder? = activity?.let { AlertDialog.Builder(it) }
                mBuilder?.setMessage("A clone of the accessibility feature in iOS 14 and Android 11.\nCreated by Vinodh N.\nVersion 1.0 Alpha")
                mBuilder?.setTitle("About BackTap")
                val mDialog:AlertDialog? = mBuilder?.create()
                mDialog?.show()

                false
            }

        }
    }
}