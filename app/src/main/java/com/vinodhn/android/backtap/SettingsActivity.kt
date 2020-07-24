package com.vinodhn.android.backtap

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle("BackTap Action Settings")
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.accessibility_preferences, rootKey)
            val mDoubleTapActionPreference = findPreference<ListPreference>("double")
            val mTripleTapActionPreference = findPreference<ListPreference>("triple")
            val mAboutPreference = findPreference<Preference>("About")
            val mDonatePreference = findPreference<Preference>("Donate")
            val mSharedPreference: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
            val mEditor: SharedPreferences.Editor = mSharedPreference.edit()

            mDoubleTapActionPreference?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->

                if(mDoubleTapActionPreference != null) {
                    mEditor.putInt(getString(R.string.double_tap_action_id), mDoubleTapActionPreference.findIndexOfValue(newValue.toString()))
                    mEditor.commit()
                }

                true
            }

            mTripleTapActionPreference?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->

                if(mTripleTapActionPreference != null){
                    mEditor.putInt(getString(R.string.triple_tap_action_id), mTripleTapActionPreference.findIndexOfValue(newValue.toString()))
                    mEditor.commit()
                }

                true
            }

            mAboutPreference?.setOnPreferenceClickListener { preference ->

                val mBuilder: AlertDialog.Builder? = activity?.let { AlertDialog.Builder(it) }
                mBuilder?.setMessage("A clone of the accessibility feature in iOS 14 and Android 11.\nCreated by Vinodh N.\nVersion 1.0 Alpha")
                mBuilder?.setTitle("About BackTap")
                val mDialog: AlertDialog? = mBuilder?.create()
                mDialog?.show()

                false
            }

            mDonatePreference?.setOnPreferenceClickListener { preference ->
                Toast.makeText(context, "Donate coming soon", Toast.LENGTH_LONG).show()
                false
            }

        }
    }
}