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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class BluetoothComm {
    // for Debugging
    private static final String TAG = "BTCOMM";
    private static final boolean D = false;

    // Unique UUID for SPP profile
    private static final UUID SPP_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Bluetooth connection info
    private final BluetoothAdapter mAdapter;
    private BluetoothDevice mDevice;
    private BluetoothSocket mSocket;

    // Current connection state
    private int mState;
    public static final int STATE_NONE = 0;       // 未接続
    public static final int STATE_CONNECTING = 2; // 接続試行中
    public static final int STATE_CONNECTED = 3;  // 接続状態

    // SubThreads
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    // Handler to MainActivity
    private final Handler mHandler;

    /// -APPLICATION SPECIFIC- Definitions start here ///
    private int[][] mBuff; // Data Rx Buffer (double buffer)
    private int mBuffLength;
    private int mBuffIndex;
    private int mBuffActive; //indicates back_buffer(=updating)
    private int mXferMode;  // Data transfer mode
    public static final int MODE_BURST = 0;
    public static final int MODE_CONTINUOUS = 1;

    /**
     * Constructor.
     * Bluetooth通信セッションを新規作成
     * @param handler UI Activityにメッセージを送信するためのハンドラ
     */
    public BluetoothComm(Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mDevice = null;
        mSocket = null;
        mState = STATE_NONE;
        mHandler = handler;
    }

    /**
     * 通信ステート変化をMainActivityに通知
     * @param state  An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;
        mHandler.obtainMessage(MainActivity.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * MainActivityからの要求で通信ステートを取得
     */
    public synchronized int getState() {
        return mState;
    }

    /**
     * デバイスへの接続を試行 (ConnectThreadを新規生成)
     * @param device  接続するBluetoothDevice
     */
    public synchronized void connect(BluetoothDevice device) {
        if (D) Log.d(TAG, "connect to: " + device);

        mDevice = device;
        // スレッドがすでに存在する場合は停止
        if (mConnectThread != null) mConnectThread = null;
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // 新規にスレッド生成
        mConnectThread = new ConnectThread();
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    /**
     * デバイスとの接続を切断し、スレッドを停止
     */
    public synchronized void disconnect() {
        if (D) Log.d(TAG, "disconnect");

        // terminate all Thread
        if (mConnectThread != null) {
            mConnectThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if(mSocket != null) {
            try {
                mSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
            mSocket = null;
        }
        mDevice = null;
        setState(STATE_NONE);
    }

    /**
     * デバイスにデータを送信 (Connected Thread呼び出し)
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {

        ConnectedThread r;
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        r.write(out);
    }

    /**
     * Toast表示要求をMainActivityに発行する
     */
    private void sendToastMsg(String str) {
        Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.TOAST, str);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    /// -APPLICATION SPECIFIC- Public Functions start here ///

    /*
     * 受信バッファの初期化
     */
    public void initBuffer(int buflen) {
        mBuffLength = buflen;
        mBuff = new int[2][mBuffLength];
        mBuffActive = 0;
        mBuffIndex = 0;

        // for debug : set initial data to 0V
        for(int i = 0; i < mBuffLength; i++) {
            mBuff[0][i] = 512;
            mBuff[1][i] = 512;
        }

        mXferMode = MODE_BURST;
    }

    /**
     * SPP通信バッファのデータの取得
     * @param index バッファ内の位置
     * @return バッファ内のindex番目のデータ
     */
    public int getBuffData(int index ) {
        return mBuff[1- mBuffActive][index];
    }

    /**
     * SPP通信バッファの大きさを取得
     * @return バッファバイト数
     */
    public int getBuffLength() {
        return mBuffLength;
    }

    /**
     * SPP通信バッファの現在位置を取得
     * @return 現在のインデックス値
     */
    public int getBuffIndex() {
        return mBuffIndex;
    }

    /**
     * SPP通信バッファの現在のモードを取得
     * @return 現在のモード（MODE_BURSTまたはMODE_）
     */
    public int getXferMode() {
        return mXferMode;
    }

    /*********************************************
     * Connect Thread description
     *********************************************/
    private class ConnectThread extends Thread {

        public ConnectThread() {
            BluetoothSocket socket = null;
            try {
                socket = mDevice.createInsecureRfcommSocketToServiceRecord(SPP_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket create() failed", e);
            }
            mSocket = socket;
        }

        public void run() {
            Log.i(TAG, "BEGIN ConnectThread." );
            setName("ConnectThread");

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            try {
                mSocket.connect();
            } catch (IOException e) {
                try {
                    mSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() " + " socket during connection failure", e2);
                }
                mSocket = null;
                sendToastMsg("Unable to connect device");
                setState(STATE_NONE);
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothComm.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            mConnectedThread = new ConnectedThread();
            mConnectedThread.start();

            // Send the name of the connected device back to the UI Activity
            Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_DEVICE_NAME);
            Bundle bundle = new Bundle();
            bundle.putString(MainActivity.DEVICE_NAME, mDevice.getName());
            msg.setData(bundle);
            mHandler.sendMessage(msg);
            setState(STATE_CONNECTED);

        }
    }

    /*********************************************
     * Connected Thread description
     *********************************************/
    private class ConnectedThread extends Thread {
        private final InputStream mInputStream;
        private final OutputStream mOutputStream;
        private boolean mRunning = true;

        /**
         * constructor
         */
        public ConnectedThread() {
            Log.d(TAG, "create ConnectedThread.");
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = mSocket.getInputStream();
                tmpOut = mSocket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }
            mInputStream = tmpIn;
            mOutputStream = tmpOut;
        }

        /**
         * Thread本体
         * ---APPLICATION SPECIFIC---
         * bluetoothSocketの入力ストリームからデータを受信し、
         * アプリケーション依存処理を行う
         */
        public void run() {
            Log.i(TAG, "BEGIN ConnectedThread");

            int tmp_high = 0;
            byte[] buffer = new byte[4096]; // Temporary buffer for Input Stream
            while (mRunning) {
                try {
                    int count = mInputStream.read(buffer); // Read 1block (not a line)
                    for(int i = 0; i < count; i++) {
                        switch(buffer[i]&0xe0) {
                            case 0x00: // High Byte (common)
                                tmp_high = buffer[i] & 0x1f;
                                break;
                            case 0x20: // Burst-Low byte
                                mBuff[mBuffActive][mBuffIndex] = (tmp_high << 5) + (buffer[i] & 0x1f);
                                mBuffIndex++;
                                if (mBuffIndex >= mBuffLength) mBuffIndex = 0; // check overflow ->discard
                                mXferMode = MODE_BURST;
                                break;
                            case 0x40: // Burst-EOR
                                mBuff[mBuffActive][mBuffIndex] = (tmp_high << 5) + (buffer[i] & 0x1f);
                                mBuffActive = 1 - mBuffActive; //swap buffer
                                mBuffIndex = 0;
                                mXferMode = MODE_BURST;
                                mHandler.obtainMessage(MainActivity.MESSAGE_READ_COMPLETE, 0, -1).sendToTarget();
                                break;
                            case 0x60: // Continuous-Low byte
                                mBuff[1- mBuffActive][mBuffIndex] = (tmp_high << 5) + (buffer[i] & 0x1f);
                                mBuffIndex++;
                                if (mBuffIndex >= mBuffLength) mBuffIndex = 0; // check overflow ->discard
                                mXferMode = MODE_CONTINUOUS;
                                break;
                            case 0x80: // Continuous-EOS
                                mBuff[1- mBuffActive][mBuffIndex] = (tmp_high << 5) + (buffer[i] & 0x1f);
                                mBuffIndex++;
                                if (mBuffIndex >= mBuffLength) mBuffIndex = 0; // check overflow ->discard
                                mXferMode = MODE_CONTINUOUS;
                                mHandler.obtainMessage(MainActivity.MESSAGE_READ_COMPLETE, 0, -1).sendToTarget();
                                break;
                            case 0xa0: // Continuous-EOR
                                mBuff[1- mBuffActive][mBuffIndex] = (tmp_high << 5) + (buffer[i] & 0x1f);
                                mBuffIndex = 0;
                                mXferMode = MODE_CONTINUOUS;
                                mHandler.obtainMessage(MainActivity.MESSAGE_READ_COMPLETE, 0, -1).sendToTarget();
                                break;
                            case 0xc0: // Force Discard
                                mBuffIndex = 0;
                                break;
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Bluetooth disconnected", e);
                    sendToastMsg("Connection was lost");
                    setState(STATE_NONE);
                    mRunning = false;
                    break;
                }
            }
        }

        /**
         * bluetoothDeviceに対してバイト列を送信するスレッド内関数
         * bluetoothComm.write()から呼ばれる
         * @param buffer 送信データ
         */
        public void write(byte[] buffer) {
            try {
                mOutputStream.write(buffer);
                mOutputStream.flush();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        /**
         * bluetooth Socketの通信が完了するまで待ってからrun()のメインループを停止させる
         */
        public void cancel() {
            try {
                Thread.sleep(750); //wait 0.75sec (until complete flushing)
            } catch (InterruptedException e) {
            }
            mRunning = false;
        }
    }
}
