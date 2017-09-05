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

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
//import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * This is the main Activity that displays the current chat session.
 */
public class MainActivity extends Activity {
    // for Debugging
    private static final String TAG = "OscilloScope";
    private static final boolean D = false; // Set 'true' for debug message

    public String mVersionName;

    //
    public GLUISurfaceView mView;
    private ActionBar mActionBar;

    // bluetooth Connection Info
    public BluetoothComm mBluetoothComm = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    public boolean mDeviceConnected = false;
    public String mConnectedDeviceName = null;

    // Message types sent from the BluetoothComm Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ_COMPLETE = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 2;
    private static final int REQUEST_ENABLE_BT = 3;
    private static final int REQUEST_SETTING = 5;

    // App Preferences
    public class Preference {
        boolean AutoConnect;
        String DeviceAddr;
    }
    public Preference mPref;

    public boolean demoMode = false;

    // Screen brightness Control
    private PowerManager.WakeLock mWL;

    //
    public static final int BUFFER_LEN = 480;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(D) Log.e(TAG, "+++ ON CREATE +++");

        // VersionNameの取得（splash画面での表示用）
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);
            mVersionName    = packageInfo.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        // Viewの初期化
        mView = new GLUISurfaceView(getApplication(), this);
        setContentView(mView);

        // アクションバーのステータス表示を初期化
        mActionBar = getActionBar();
        if(mActionBar != null) mActionBar.setSubtitle(R.string.title_not_connected);

        // Preferenceを初期化（初回起動時のみ初期値を設定、後は保存値の復帰）
        mPref = new Preference();
        setPreference();

        // PowerManager取得（アプリ実行中はスリープしないように設定するため）
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        mWL = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Tag");

        // Bluetooth adapterが存在するかチェック
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");

        // BluetoothがOFFになっていたらON要求（→ダイアログで確認させる）結果はonActivityResult()で受ける
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // すでにONだったらBluetooth通信スレッドを初期化
        } else {
            if (mBluetoothComm == null) setupBTSession();
        }

        // PreferenceでAuto Connectionになっている場合は直ちに接続開始
        if((mPref.AutoConnect)&&(mPref.DeviceAddr != null)) {
            try {
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mPref.DeviceAddr);
                connectDevice(device);
            } catch (IllegalArgumentException e) {
                Log.d(TAG, "Invalid AutoConnect Address.");
            }
        }

        //スリープ禁止
        mWL.acquire();
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");
        mView.onResume();
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if(D) Log.e(TAG, "- ON PAUSE -");
        mView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(D) Log.e(TAG, "-- ON STOP --");
        disconnectDevice();
        //free WakeLock
        mWL.release();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(D) Log.e(TAG, "--- ON DESTROY ---");
        demoMode = false;
    }

    // Handler process from BluetoothComm Thread
    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothComm.STATE_CONNECTED:
                            mActionBar.setSubtitle(getString(R.string.title_connected_to, mConnectedDeviceName));
                            mDeviceConnected = true;
                            break;
                        case BluetoothComm.STATE_CONNECTING:
                            mActionBar.setSubtitle(R.string.title_connecting);
                            break;
                        case BluetoothComm.STATE_NONE:
                            mActionBar.setSubtitle(R.string.title_not_connected);
                            mDeviceConnected = false;
                            break;
                    }
                    break;
                case MESSAGE_WRITE: // not use
                    break;
                case MESSAGE_READ_COMPLETE: // not use
                    break;
                case MESSAGE_DEVICE_NAME: // show the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "+mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    //
    public void setupBTSession() {
        Log.d(TAG, "setupBTSession()");
        mBluetoothComm = new BluetoothComm(mHandler);
        mBluetoothComm.initBuffer(BUFFER_LEN);
    }

    // デバイスにデータ送信
    public void sendMessage(String message) {
        // コネクションが確立されている事をチェック
        if (mBluetoothComm.getState() != BluetoothComm.STATE_CONNECTED) {
            return;
        }
        if (message.length() > 0) {
            byte[] send = message.getBytes();
            mBluetoothComm.write(send);
        }
    }

    //
    private void setPreference() {

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        Editor e = pref.edit();
        if (!pref.getBoolean("Launched", false)) { // 初回起動時チェック →初期値を設定
            mPref.AutoConnect = false;
            e.putBoolean(PreferenceActivity.PREF_KEY_AUTO_CONNECT, mPref.AutoConnect);
            mPref.DeviceAddr = null;
            e.putString(PreferenceActivity.PREF_KEY_DEVICE_ADDR, mPref.DeviceAddr);
            e.putBoolean("Launched", true);
            e.apply();
        }
        else {  // 初回起動以外は保存されているPreference値を復帰
            updatePreference();
        }
    }


    //
    private void saveDeviceAddr(String addr) {
        if(addr == null) return;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Editor e = sharedPreferences.edit();

        e.putString(PreferenceActivity.PREF_KEY_DEVICE_ADDR, addr);
        e.apply();
    }

    //
    private void updatePreference() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        mPref.AutoConnect = pref.getBoolean(PreferenceActivity.PREF_KEY_AUTO_CONNECT, mPref.AutoConnect);
        mPref.DeviceAddr = pref.getString(PreferenceActivity.PREF_KEY_DEVICE_ADDR, mPref.DeviceAddr);
    }

    // connect request to bluetooth device
    private void connectDevice(BluetoothDevice device) {
        mBluetoothComm.connect(device);
    }

    // disconnect from bluetooth device
    private void disconnectDevice() {
        if (mBluetoothComm != null) {
            mBluetoothComm.disconnect();
        }
        mDeviceConnected = false;
    }

    //
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    saveDeviceAddr(address);
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    connectDevice(device);
                }
                break;
            case REQUEST_SETTING:
                updatePreference();
                break;
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    setupBTSession();
                } else {
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    //
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    //
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent serverIntent = null;
        switch (item.getItemId()) {
            case R.id.connect:
                serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                return true;
            case R.id.disconnect:
                disconnectDevice();
                return true;
            case R.id.force_exec:
                demoMode = true; // Turn into demo mode (without connection)
                return true;
            case R.id.setup_app:
                serverIntent = new Intent(this, PreferenceActivity.class);
                startActivityForResult(serverIntent, REQUEST_SETTING);
                return true;
        }
        return false;
    }
}
