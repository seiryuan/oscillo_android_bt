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

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.seiryuan.android.glui.*;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

import static java.lang.Math.sin;


class GLUIRenderer implements GLSurfaceView.Renderer {
    // for Debugging
    private static final String TAG = "OscilloScope";
    private static final boolean D = false;

    private MainActivity m;
    private GLUI glui;

    private Primitive splash;
    private Sprite logo;
    private TextDisp title1;
    private TextDisp title2;
    private TextDisp version;
    private TextDisp copyright;

    private Primitive scope;
    private LineStrip scope_value;
    private NinePatch scope_frame;
    private Line scope_grid;
    private Line scope_cursor;
    private NumberDisp scope_digit_v[];
    private NumberDisp scope_digit_h[];

    private Primitive control;
    private NinePatch control_frame;
    private Line control_separator;
    private TextDisp control_text1;
    private TextDisp control_text2;
    private TextDisp control_text3;
    private TextDisp control_text4;
    private TextDisp control_text5;
    private TextDisp control_text6;
    private TextDisp control_text7;

    private TextDisp control_run_mode;
    private TextDisp control_trig_mode;
    private TextDisp control_frequency;

    private Button button_stop;
    private Button button_run;
    private Button button_single;
    private Button button_free;
    private Button button_plus;
    private Button button_minus;
    private Button button_500uSec;
    private Button button_1mSec;
    private Button button_5mSec;
    private Button button_5Sec;
    private Button button_1Sec;
    private Button button_500mSec;
    private Button button_ch0;
    private Button button_ch1;
    private Button button_ch2;
    private Button button_ch3;
    private Button button_gpio_on;
    private Button button_gpio_off;


    GLUIRenderer(Context context, Activity activity) {

        m = (MainActivity)activity;

        //GLUI全体の初期化
        glui = new GLUI(context);

        //描画領域サイズ設定
        glui.setWindowMode(GLUI.WINDOW_MANUAL); // Portrait/Landscapeのそれぞれに解像度を指定
        glui.setFrameSize(1200, 1800, 2040, 1060); // Portrait-W,H, Landscape-W,H (単位はpixel)

        //画面表示モードの設定
        glui.setAutoScale(true); // 端末の解像度に自動調整=ON

        //画面背景色
        glui.setBGColor(0.4f, 0.4f, 0.4f);

        //テクスチャサイズ(正方形の一辺pixel値, リソース名)
        glui.setTextureSize(1024, com.seiryuan.android.oscilloscope_bt.R.raw.texture);

        //文字表示フォントの初期化（Width, Height, U, V)
        FontInfo font1 = new FontInfo(22f, 40f, 0f, 0f, 6, 16);
        FontInfo font2 = new FontInfo(14f, 24f, 704f, 240f, 6, 16);

        //数値表示フォントの初期化（Width, Height, U, V)
        FontInfo numfont1 = new FontInfo(32, 48, 640f, 140f, 1, 12);
        FontInfo numfont2 = new FontInfo(40, 60, 543f, 80f, 1, 12);
        FontInfo numfont3 = new FontInfo(50, 80, 372f, 0f, 1, 12);
        FontInfo numfont4 = new FontInfo(16, 24, 832f, 188f, 1, 12);

        // Splash top (dummy)
        splash = new Primitive(glui, null);
        splash.setPosition(0, -150);

        // Splash Logo
        logo = new Sprite(glui, splash);
        logo.setPosition(0f, 0f);
        logo.setSize(512, 204);
        logo.setTexture(0f, 240f);

        // Splash Title
        title1 = new TextDisp(glui, splash, font1, -130, 200, "OscilloScope");
        title2 = new TextDisp(glui, splash, font1, -180, 240, "bluetooth edition");
        version = new TextDisp(glui, splash, font1, -120, 300, "version "+m.mVersionName);
        copyright = new TextDisp(glui, splash, font2, -240, 380, "Copyright(C) 2016,2017  Workshop SeiRyuAn");

        // Init Scope
        scope = new Primitive(glui, null);
        scope.setPosition(0f, -390f);

        scope_frame = new NinePatch(glui, scope, 0, 0, 960f, 960f, 512f, 240f, 56f, 56f,30f, 30f);
        scope_frame.update();

        scope_grid = new Line(glui, scope, 44);
        scope_grid.setTexture(594, 256); // White pixel
        scope_grid.setColor(0.3f, 0.3f, 0.3f, 1.0f);
        for(int i = 0; i < 11; i++) {
            scope_grid.setVertex(i*2, i*96-480,  -480f);
            scope_grid.setVertex(i*2+1, i*96-480,  480f);
        }
        for(int i = 0; i < 11; i++) {
            scope_grid.setVertex(i*2+22, -480f, i*96-480);
            scope_grid.setVertex(i*2+23, 480f, i*96-480);
        }

        scope_digit_v = new NumberDisp[11];
        scope_digit_h = new NumberDisp[11];
        for(int i = 0; i < 11; i++) {
            scope_digit_h[i] = new NumberDisp(glui, scope, numfont4, -500+i*96, 496, 3);
            scope_digit_h[i].setValue(2, GLUI.ZEROSUPPRESS ,i);
            scope_digit_v[i] = new NumberDisp(glui, scope, numfont4, -544, 464-i*96, 3);
            scope_digit_v[i].setSignedValue(2, GLUI.ZEROSUPPRESS, i-5);
        }

        scope_value = new LineStrip(glui, scope, MainActivity.BUFFER_LEN);
        scope_value.setTexture(594, 256); // White pixel
        scope_value.setColor(0.5f, 1.0f, 0.5f, 1.0f);

        scope_cursor = new Line(glui, scope, 2);
        scope_cursor.setTexture(594, 256); // White pixel
        scope_cursor.setColor(0.8f, 1.0f, 0.8f, 1.0f);

        // frame for Buttons
        control = new Primitive(glui, null);
        control.setPosition(0f, 525f);

        control_frame = new NinePatch(glui, control, 0, 0, 960f, 720f, 640f/*576f*/, 304f, 56f, 56f, 30f, 30f);

        control_separator = new Line(glui, control, 8);
        control_separator.setTexture(594, 256); // White pixel
        control_separator.setColor(0.4f, 0.4f, 0.4f, 1.0f);
        control_separator.setPosition(0, 0);
        control_separator.setVertex(0, -460,  -240f);
        control_separator.setVertex(1, 460,  -240f);
        control_separator.setVertex(2, -460,  -120f);
        control_separator.setVertex(3, 460,  -120f);
        control_separator.setVertex(4, -460,  120f);
        control_separator.setVertex(5, 460,  120f);
        control_separator.setVertex(6, -460,  240f);
        control_separator.setVertex(7, 460,  240f);

        // Run state Buttons
        control_text1 = new TextDisp(glui, control, font1, -460, -350, "OPERATION");
        control_run_mode = new TextDisp(glui, control, font1, -400, -300, 8);
        button_stop = new Button(glui, control, -40, -300, 160, 96, 0, 444, 160, 444);
        button_stop.Off();
        button_run = new Button(glui, control, +160, -300, 160, 96, 320, 444, 480, 444);
        button_run.On();
        button_single = new Button(glui, control, 360, -300, 160, 96, 640, 444, 800, 444);
        button_single.Off();

        // Trigger Buttons
        control_text2 = new TextDisp(glui, control, font1, -460, -230, "TRIGGER");
        control_trig_mode = new TextDisp(glui, control, font1, -400, -180, 8);
        button_free = new Button(glui, control, -40, -180, 160, 96, 0, 540, 160, 540);
        button_free.On();
        button_plus = new Button(glui, control, 160, -180, 160, 96, 320, 540, 480, 540);
        button_plus.Off();
        button_minus = new Button(glui, control, 360, -180, 160, 96, 640, 540, 800, 540);
        button_minus.Off();

        // Sampling Rate Buttons
        control_text3 = new TextDisp(glui, control, font1, -460, -110, "HORIZONTAL");
        control_text4 = new TextDisp(glui, control, font2, -280, -70, "fast mode --");
        control_text5 = new TextDisp(glui, control, font2, -280, +50, "slow mode --");
        control_frequency = new TextDisp(glui, control, font1, -400, -60, 8);
        button_5mSec = new Button(glui, control, -40, -60, 160, 96, 0, 636, 160, 636);
        button_5mSec.Off();
        button_1mSec = new Button(glui, control, 160, -60, 160, 96, 320, 636, 480, 636);
        button_1mSec.On();
        button_500uSec = new Button(glui, control, 360, -60, 160, 96, 640, 636, 800, 636);
        button_500uSec.Off();
        button_5Sec = new Button(glui, control, -40, +60, 160, 96, 0, 732, 160, 732);
        button_5Sec.Off();
        button_1Sec = new Button(glui, control, 160, +60, 160, 96, 320, 732, 480, 732);
        button_1Sec.Off();
        button_500mSec = new Button(glui, control, 360, 60, 160, 96, 640, 732, 800, 732);
        button_500mSec.Off();

        // ADC-Ch select buttons
        control_text6 = new TextDisp(glui, control, font1, -460, +130, "INPUT CH.");
        button_ch0 = new Button(glui, control, -60, 180, 120, 96, 0, 922, 120, 922);
        button_ch0.On();
        button_ch1 = new Button(glui, control, 84, 180, 120, 96, 240, 922, 360, 922);
        button_ch1.Off();
        button_ch2 = new Button(glui, control, 228, 180, 120, 96, 480, 922, 600, 922);
        button_ch2.Off();
        button_ch3 = new Button(glui, control, 372, 180, 120, 96, 720, 922, 840, 922);
        button_ch3.Off();

        // GPIO control buttons
        control_text7 = new TextDisp(glui, control, font1, -460, +250, "GPIO output");
        button_gpio_on = new Button(glui, control, -40, 300, 160, 96, 0, 828, 160, 828);
        button_gpio_on.On();
        button_gpio_off = new Button(glui, control, 160, 300, 160, 96, 320, 828, 480, 828);
        button_gpio_off.On();
    }

    //
    @Override
    public void onDrawFrame(GL10 glUnused) {

        // フレーム毎の描画初期化
        glui.initDraw();

        // 状態遷移＆データ更新
        if((!m.demoMode)&&(!m.mDeviceConnected)) {
            splash.enable();
            scope.disable();
            control.disable();
        }
        else {
            updateItem();
            splash.disable();
            scope.enable();
            control.enable();
        }

        // 登録されたすべてのPrimitiveを描画
        glui.world.draw();

    }

    private void updateItem() {

        // 画面の向きをレイアウトに反映
        if(glui.getDispMode() == GLUI.DISP_PORTRAIT) {
            scope.setPosition(0f, -390f);
            control.setPosition(0f, 525f);
        }
        else {
            scope.setPosition(-500f, -15f);
            control.setPosition(540f, 0f);
        }

        //update scope value
        if(m.mDeviceConnected) {
            int n = m.mBluetoothComm.getBuffLength();
            for (int i = 0; i < n; i++) {
                float x = (float) i * 2 - n;
                float y = -(float) m.mBluetoothComm.getBuffData(i) / 1.06f + 480f;
                scope_value.setVertex(i, x, y);
            }
            //update cursor position
            if(m.mBluetoothComm.getXferMode() == BluetoothComm.MODE_CONTINUOUS) {
                float pos = (float)(m.mBluetoothComm.getBuffIndex()*2-n);
                scope_cursor.setVertex(0, pos,  -480f);
                scope_cursor.setVertex(1, pos,  480f);
                scope_cursor.enable();
            }
            else {
                scope_cursor.disable();
            }
        }
        else if(m.demoMode) { //DemoModeで正弦波を表示
            for (int i = 0; i < m.BUFFER_LEN; i++) {
                float x = (float) i * 2 - m.BUFFER_LEN;
                float y = (float)sin((float)i/15f)*300f;
                scope_value.setVertex(i, x, y);
            }
            scope_cursor.disable();
        }
    }

    //
    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        glui.onSurfaceChanged(m.mView, width, height);
    }

    //
    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        glui.onSurfaceCreated();
    }

    //
    void onTouchEvent(MotionEvent event) {
        float x, y;

        switch ( event.getAction() ) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                x = glui.scaleAdjustX((int) event.getX());
                y = glui.scaleAdjustY((int) event.getY());

                if(button_stop.checkRegion(x, y)) {
                    button_stop.On();
                    button_run.Off();
                    button_single.Off();
                    m.sendMessage("R0\n");
                    control_run_mode.setText("STOP");
                }
                else if(button_run.checkRegion(x, y)) {
                    button_stop.Off();
                    button_run.On();
                    button_single.Off();
                    m.sendMessage("R1\n");
                    control_run_mode.setText("RUN");
                }
                else if(button_single.checkRegion(x, y)) {
                    button_stop.Off();
                    button_run.Off();
                    button_single.On();
                    m.sendMessage("R2\n");
                    control_run_mode.setText("SINGLE");
                }
                else if(button_free.checkRegion(x, y)) {
                    button_free.On();
                    button_plus.Off();
                    button_minus.Off();
                    m.sendMessage("TF\n");
                    control_trig_mode.setText("FREE");
                }
                else if(button_plus.checkRegion(x, y)) {
                    button_free.Off();
                    button_plus.On();
                    button_minus.Off();
                    m.sendMessage("TP\n");
                    control_trig_mode.setText("POSITIVE");
                }
                else if(button_minus.checkRegion(x, y)) {
                    button_free.Off();
                    button_plus.Off();
                    button_minus.On();
                    m.sendMessage("TN\n");
                    control_trig_mode.setText("NEGATIVE");
                }
                else if(button_5mSec.checkRegion(x, y)) {
                    button_500mSec.Off();
                    button_1Sec.Off();
                    button_5Sec.Off();
                    button_5mSec.On();
                    button_1mSec.Off();
                    button_500uSec.Off();
                    m.sendMessage("B0\n");
                    control_frequency.setText("5msec");
                }
                else if(button_1mSec.checkRegion(x, y)) {
                    button_500mSec.Off();
                    button_1Sec.Off();
                    button_5Sec.Off();
                    button_5mSec.Off();
                    button_1mSec.On();
                    button_500uSec.Off();
                    m.sendMessage("B1\n");
                    control_frequency.setText("1msec");
                }
                else if(button_500uSec.checkRegion(x, y)) {
                    button_500mSec.Off();
                    button_1Sec.Off();
                    button_5Sec.Off();
                    button_5mSec.Off();
                    button_1mSec.Off();
                    button_500uSec.On();
                    m.sendMessage("B2\n");
                    control_frequency.setText("500usec");
                }
                else if(button_5Sec.checkRegion(x, y)) {
                    button_500mSec.Off();
                    button_1Sec.Off();
                    button_5Sec.On();
                    button_5mSec.Off();
                    button_1mSec.Off();
                    button_500uSec.Off();
                    m.sendMessage("C0\n");
                    control_frequency.setText("5sec");
                }
                else if(button_1Sec.checkRegion(x, y)) {
                    button_500mSec.Off();
                    button_1Sec.On();
                    button_5Sec.Off();
                    button_5mSec.Off();
                    button_1mSec.Off();
                    button_500uSec.Off();
                    m.sendMessage("C1\n");
                    control_frequency.setText("1sec");
                }
                else if(button_500mSec.checkRegion(x, y)) {
                    button_500mSec.On();
                    button_1Sec.Off();
                    button_5Sec.Off();
                    button_5mSec.Off();
                    button_1mSec.Off();
                    button_500uSec.Off();
                    m.sendMessage("C2\n");
                    control_frequency.setText("0.5sec");
                }
                else if(button_ch0.checkRegion(x, y)) {
                    button_ch0.On();
                    button_ch1.Off();
                    button_ch2.Off();
                    button_ch3.Off();
                    m.sendMessage("A2\n"); //Ch.0 = ADC2
                }
                else if(button_ch1.checkRegion(x, y)) {
                    button_ch0.Off();
                    button_ch1.On();
                    button_ch2.Off();
                    button_ch3.Off();
                    m.sendMessage("A3\n"); //Ch.1 = ADC3
                }
                else if(button_ch2.checkRegion(x, y)) {
                    button_ch0.Off();
                    button_ch1.Off();
                    button_ch2.On();
                    button_ch3.Off();
                    m.sendMessage("A9\n"); //Ch.2 = ADC9
                }
                else if(button_ch3.checkRegion(x, y)) {
                    button_ch0.Off();
                    button_ch1.Off();
                    button_ch2.Off();
                    button_ch3.On();
                    m.sendMessage("AA\n"); //Ch.3 = ADC10
                }
                else if(button_gpio_on.checkRegion(x, y)) {
                    button_gpio_on.On();
                    button_gpio_off.Off();
                    m.sendMessage("G1\n"); //Ch.3 = ADC10
                }
                else if(button_gpio_off.checkRegion(x, y)) {
                    button_gpio_on.Off();
                    button_gpio_off.On();
                    m.sendMessage("G0\n"); //Ch.3 = ADC10
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
        }
    }
}
