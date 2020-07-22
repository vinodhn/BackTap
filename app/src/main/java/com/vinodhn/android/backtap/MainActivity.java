package com.vinodhn.android.backtap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceFragmentCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new MainActivity.SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("BackTap Action Settings");
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            final ListPreference mDoubleTapActionPref = findPreference("double");
            final ListPreference mTripleTapActionPref = findPreference("triple");
            final Preference mAccessibilityPref = findPreference("accessibilitySettings");
            final Preference mAboutPreference = findPreference("About");
            final Preference mDonatePreference = findPreference("Donate");
            SharedPreferences mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            final SharedPreferences.Editor mEditor = mSharedPrefs.edit();

            if(mDoubleTapActionPref != null){
                mDoubleTapActionPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        mEditor.putInt(getString(R.string.double_tap_action_id), mDoubleTapActionPref.findIndexOfValue(newValue.toString()));
                        mEditor.commit();
                        return true;
                    }
                });
            }

            if(mTripleTapActionPref != null){
                mTripleTapActionPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        mEditor.putInt(getString(R.string.triple_tap_action_id), mTripleTapActionPref.findIndexOfValue(newValue.toString()));
                        mEditor.commit();
                        return true;
                    }
                });
            }

            if(mAccessibilityPref != null){
                mAccessibilityPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Intent mLaunchSettings = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                        startActivity(mLaunchSettings);
                        return false;
                    }
                });
            }

            if(mAboutPreference != null){
                mAboutPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
                        mBuilder.setMessage("A clone of the accessibility feature in iOS 14 and Android 11.\nCreated by Vinodh N.\nVersion 1.0 Alpha").setTitle("About BackTap");
                        AlertDialog mDialog = mBuilder.create();
                        mDialog.show();
                        return false;
                    }
                });
            }

            if(mDonatePreference != null){
                mDonatePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Toast.makeText(getContext(), "Donate coming soon.", Toast.LENGTH_LONG).show();
                        return false;
                    }
                });
            }

        }
    }

}
