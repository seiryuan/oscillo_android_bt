/*
 * Oscilloscope - Bluetooth version
 *
 * Copyright (C) 2016,2017 Masayoshi Tanaka @ Workshop SeiRyuAn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.seiryuan.android.oscilloscope_bt;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;

public class PreferenceActivity extends Activity {
    public static final String PREF_KEY_AUTO_CONNECT = "auto_connect";
    public static final String PREF_KEY_DEVICE_ADDR = "device_addr";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new CustomPreferenceFragment()).commit();
    }
    
    public static class CustomPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preference_screen_sample);
            
            SwitchPreference prefAutoConnect = (SwitchPreference)findPreference(PREF_KEY_AUTO_CONNECT);
            if(prefAutoConnect.isChecked()) {
            	prefAutoConnect.setSummary(prefAutoConnect.getSwitchTextOn());
            }
            else {
            	prefAutoConnect.setSummary(prefAutoConnect.getSwitchTextOff());                   	
            }

            EditTextPreference prefDeviceAddr = (EditTextPreference)findPreference(PREF_KEY_DEVICE_ADDR);
            prefDeviceAddr.setSummary(prefDeviceAddr.getText());
        }

        @Override
        public void onResume() {
            super.onResume();
            SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
            sharedPreferences.registerOnSharedPreferenceChangeListener(onPreferenceChangeListenter);
        }
        
        @Override
        public void onPause() {
            super.onPause();
            SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(onPreferenceChangeListenter);
        }
        
        private OnSharedPreferenceChangeListener onPreferenceChangeListenter = new OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals(PREF_KEY_AUTO_CONNECT)) {
                    SwitchPreference pref = (SwitchPreference)findPreference(key);
                    if(pref.isChecked()) {
                    	pref.setSummary(pref.getSwitchTextOn());
                    }
                    else {
                    	pref.setSummary(pref.getSwitchTextOff());                   	
                    }
                }
            }
        };
    }
}
