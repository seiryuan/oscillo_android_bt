/*
 * GLUI - OpenGL based GUI library
 * GLUI.java: main class
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

package com.seiryuan.android.glui;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

public class GLUI {

	//for Debug message
	private String TAG = "GLUI";

    // Top of primitive tree
    public Primitive world;

    //フレームバッファサイズ(描画座標系＝仮想ピクセル)
    private int mFrameWidthP;
    private int mFrameHeightP;
    private int mFrameWidthL;
    private int mFrameHeightL;

    private int mViewWidth;
    private int mViewHeight;
    private int mDispMode;
    public static final int DISP_LANDSCAPE = 1;
    public static final int DISP_PORTRAIT = 2;

    // 画面解像度の自動調整
    private boolean mAutoFit;

    // portrait/landscale切り替え対応
    private int mWindowMode;
    public static final int WINDOW_AUTO = 1; // portrait/landscaleでW/H入れ替え
    public static final int WINDOW_MANUAL = 2; // portrait/landscaleそれぞれ指定
    public static final int WINDOW_RIGID = 3; // portrait/landscaleで変化なし

    //
    private float global_ratio_x;
    private float global_offset_x;
    private float global_ratio_y;
    private float global_offset_y;

    //背景色 (RGB: 0f-1.0f)
    private float mBgColorR;
    private float mBgColorG;
    private float mBgColorB;

    //描画コンテキスト
    private Context mContext;

    //シェーダー
    private int mProgram;

    //シェーダーパラメーターのHandler
    int muMVPMatrixHandle;
    int muColorHandle;
    private int maPositionHandle;
    private int maTextureHandle;

    //視点マトリクス
    private float[] mViewMatrix;
    private float[] mProjMatrix;
    float[] mVPMatrix = new float[16];


    //VertexBufferの定義
    private FloatBuffer mVertexArray;
    private static final int FLOAT_SIZE_BYTES = 4;	//Float値のByte数=4
    private static final int VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES; //1頂点の要素数×5Byte
    private static final int VERTICES_ARRAY_SIZE = 5000 * VERTICES_DATA_STRIDE_BYTES; //200頂点
    private static final int VERTICES_DATA_POS_OFFSET = 0; //頂点内のxyz値のオフセット=0
    private static final int VERTICES_DATA_UV_OFFSET = 3; //頂点内のUV値のオフセット=3

    //テクスチャ
    public float mTextureSize; //テクスチャサイズ（正方形の辺：pixel）
    private int mTextureID; // texture buffer for OpenGL
    private int mTextureRes; // resource ID for raw texture

    //フォント属性の定義値
    public final static int ZEROPADDING = 0;
    public final static int ZEROSUPPRESS = 1;

    public GLUI(Context context) {

        mContext = context;

        // Init Vertex Buffer
        mVertexArray = ByteBuffer.allocateDirect(VERTICES_ARRAY_SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();
        //mVertexArray.put(mTriangleVerticesData1).mVertexPos(0);
        mVertexArray.position(0);

        mViewMatrix = new float[16];
        mProjMatrix = new float[16];

        world = new Primitive(this);

        mWindowMode = WINDOW_RIGID;
        mAutoFit = false;
    }

    public void setAutoScale(boolean fit) {
        mAutoFit = fit;
    }

    public void setWindowMode(int mode) {
        mWindowMode = mode;
    }

    public int getDispMode() {
        return mDispMode;
    }

    public void setFrameSize(int w, int h) {
		mFrameWidthP = w;
		mFrameHeightP = h;
        mFrameWidthL = w;
        mFrameHeightL = h;
	}

    public void setFrameSize(int pw, int ph, int lw, int lh) {
        mFrameWidthP = pw;
        mFrameHeightP = ph;
        mFrameWidthL = lw;
        mFrameHeightL = lh;
    }

    public void setBGColor(float r, float g, float b) {
        mBgColorR = r;
        mBgColorG = g;
        mBgColorB = b;
    }

    public void setTextureSize(int size, int Res) {
		mTextureSize = size;
		mTextureRes = Res;
	}

	public int getVertexPosition() {
        return mVertexArray.position()/5;
	}

    public int allocVertex(int n) {
        int pos = mVertexArray.position()/5;
        for(int i = 0; i < n*5; i++) {
            mVertexArray.put(0f);
        }
        return pos;
    }
/*
	void addVertex(float x, float y, float z, float u, float v) {
		mVertexArray.put(x);
		mVertexArray.put(y);
		mVertexArray.put(z);
		mVertexArray.put(u);
		mVertexArray.put(v);
	}
*/
    public void setVertexXY(int index, float x, float y) {
		mVertexArray.put(index*5, x);
		mVertexArray.put(index*5+1, y);
        mVertexArray.put(index*5+2, 0f);
	}

    public void setVertexXYZ(int index, float x, float y, float z) {
        mVertexArray.put(index*5, x);
        mVertexArray.put(index*5+1, y);
        mVertexArray.put(index*5+2, z);
    }

    public void setVertexUV(int index, float u, float v) {
        mVertexArray.put(index*5+3, u);
        mVertexArray.put(index*5+4, v);
    }

	public void initDraw() {
    	//
    	GLES20.glClearColor(mBgColorR, mBgColorG, mBgColorB, 1.0f);
        GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glUseProgram(mProgram);
        checkGlError("glUseProgram");

        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnable(GLES20.GL_BLEND);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureID);

        // VertexArray内の座標値(PositionHandle)をOGLに設定
        mVertexArray.position(VERTICES_DATA_POS_OFFSET);
        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false, VERTICES_DATA_STRIDE_BYTES, mVertexArray);
        checkGlError("glVertexAttribPointer maPosition");
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        checkGlError("glEnableVertexAttribArray maPositionHandle");

        // VertexArray内のUV値(TextureHandle)をOGLに設定
        mVertexArray.position(VERTICES_DATA_UV_OFFSET);
        GLES20.glVertexAttribPointer(maTextureHandle, 2, GLES20.GL_FLOAT, false, VERTICES_DATA_STRIDE_BYTES, mVertexArray);
        checkGlError("glVertexAttribPointer maTextureHandle");
        GLES20.glEnableVertexAttribArray(maTextureHandle);
        checkGlError("glEnableVertexAttribArray maTextureHandle");

	}

	/*
	 * Update display geometry and create Viewport-Matrix
	 */
    public void onSurfaceChanged(GLSurfaceView view, int view_width, int view_height) {
        float fb_aspect; // aspect ratio of GLUI-ViewPort
        float view_aspect; // aspect ratio of display area
        int fb_width, fb_height; // FB size(w,h) in pixel
        int fw, fh; //Actual FB-w,h (corresponding Portrait/Landscape)

        Rect rectInGlobal = new Rect();
        view.getGlobalVisibleRect(rectInGlobal);
        mViewWidth = view_width;
        mViewHeight = view_height;
        view_aspect = (float)view_height/(float)view_width;
        if(view_aspect > 1.0) {
            mDispMode = DISP_PORTRAIT;
            fw = mFrameWidthP;
            fh = mFrameHeightP;
        }
        else {
            mDispMode = DISP_LANDSCAPE;
            fw = mFrameWidthL;
            fh = mFrameHeightL;
        }
        fb_aspect = (float)fh/(float)fw;

        if(mAutoFit) {
            if(view_aspect> fb_aspect) {
                fb_width = view_width;
                fb_height = (int)((float) view_width *fb_aspect);
            }
            else {
                fb_width = (int)((float) view_height /fb_aspect);
                fb_height = view_height;
            }
        }
        else {
            fb_width = fw;
            fb_height = fh;
        }

        //Global->ViewPort translate factor
        global_ratio_x = (float)fw/(float)fb_width;
        global_offset_x = ((float) view_width - fb_width)/2f;
        global_ratio_y = (float)fh/(float)fb_height;
        global_offset_y = ((float) view_height - fb_height)/2f;

        final float view_size = (float)fw/8f;
        GLES20.glViewport((view_width - fb_width)/2, (view_height - fb_height)/2, fb_width, fb_height);
        Matrix.frustumM(mProjMatrix, 0, -view_size, view_size, -fb_aspect*view_size, fb_aspect*view_size, 1.0f, 7);
        Matrix.setLookAtM(mViewMatrix, 0, 0.0f, 0.0f, -4.0f, 0f, 0f, 0f, 0f, -1.0f, 0.0f);
        Matrix.multiplyMM(mVPMatrix, 0, mProjMatrix, 0, mViewMatrix, 0);
    }

	public void onSurfaceCreated() {

		// Compile and Attach Shader programs
	    mProgram = createProgram(mVertexShader, mFragmentShader);
	    if (mProgram == 0) {
	        return;
	    }

	    // Initialize VertexAttribute
	    maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
	    checkGlError("glGetAttribLocation aPosition");
	    if (maPositionHandle == -1) {
	        throw new RuntimeException("Could not get attrib location for aPosition");
	    }
	    maTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
	    checkGlError("glGetAttribLocation aTextureCoord");
	    if (maTextureHandle == -1) {
	        throw new RuntimeException("Could not get attrib location for aTextureCoord");
	    }

	    // Initialize UniformVariable
	    muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
	    checkGlError("glGetUniformLocation uMVPMatrix");
	    if (muMVPMatrixHandle == -1) {
	        throw new RuntimeException("Could not get uniform location for uMVPMatrix");
	    }
	    muColorHandle = GLES20.glGetUniformLocation(mProgram, "uColor");
	    checkGlError("glGetUniformLocation uColor");
	    if (muColorHandle == -1) {
	        throw new RuntimeException("Could not get uniform location for uColor");
	    }

	    // Create texture
	    int[] textures = new int[1];
	    GLES20.glGenTextures(1, textures, 0);
	    mTextureID = textures[0];
	    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureID);
	    GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
	    GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
	    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
	    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

	    // Create bitmap from PNGfile
        InputStream is = mContext.getResources().openRawResource(mTextureRes);
        Bitmap bitmap;
        try {
            bitmap = BitmapFactory.decodeStream(is);
        } finally {
            try {
                is.close();
            } catch(IOException e) {
                // Ignore.
            }
        }
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();

	}

    //指定されたイメージファイルを読み込んでCanvasの所定位置に描画
    /*private static void loadBitmap(Canvas canvas, int mPosX, int mPosY, String fname) {
        String filePath;
        Bitmap bitmap;
        filePath = Environment.getExternalStorageDirectory() + fname;
        bitmap = BitmapFactory.decodeFile(filePath);
        canvas.drawBitmap(bitmap, mPosX, mPosY, (Paint) null);
        bitmap.recycle();
    }*/

    /**
     * シェーダプログラムをソースから生成してリンクする
     * @param shaderType shaderのタイプ
     * @param source shaderのソースコード
     * @return program 生成したシェーダオブジェクト
     */
    private int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                Log.e(TAG, "Could not compile shader " + shaderType + ":");
                Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

    /**
     * シェーダプログラムをソースから生成してリンクする
     * @param vertexSource vertex shaderのソースコード
     * @param fragmentSource fragment shaderのソースコード
     * @return program 生成したシェーダオブジェクト
     */
    private int createProgram(String vertexSource, String fragmentSource) {

    	// Compile Vertex Shader
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }

        // Compile Fragment Shader
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (fragmentShader == 0) {
            return 0;
        }

        int program = GLES20.glCreateProgram();
        if (program != 0) {
            GLES20.glAttachShader(program, vertexShader);
            checkGlError("glAttachShader");
            GLES20.glAttachShader(program, fragmentShader);
            checkGlError("glAttachShader");
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.e(TAG, "Could not link program: ");
                Log.e(TAG, GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }
        return program;
    }


/** FUNCTION: checkGlError
 *
 */
    public void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            //Log.e(TAG, op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }

    /**
     * 画面のスケール値を元に、仮想座標値から実スクリーン座標値に変換する
     * @param org_x 補正するX座標値
     * @return 補正後のX座標値
     */
    public float scaleAdjustX(int org_x) {
        float w;
        if(mDispMode == DISP_PORTRAIT) {
            w = mFrameWidthP;
        }
        else {
            w = mFrameWidthL;
        }
        return ((float)org_x-global_offset_x)* global_ratio_x - w/2f;
    }

    /**
     * 画面のスケール値を元に、仮想座標値から実スクリーン座標値に変換する
     * @param org_y 補正するY座標値
     * @return 補正後のY座標値
     */
    public float scaleAdjustY(int org_y) {
        float h;
        if(mDispMode == DISP_PORTRAIT) {
            h = mFrameHeightP;
        }
        else {
            h = mFrameHeightL;
        }
        return ((float)org_y-global_offset_y)* global_ratio_y - h/2f;
    }

    /**
     * vertex shader source code
     */
    private static final String mVertexShader =
        "uniform mat4 uMVPMatrix;\n" +
        "attribute vec4 aPosition;\n" +
        "attribute vec2 aTextureCoord;\n" +
        "varying vec2 vTextureCoord;\n" +
        "void main() {\n" +
        "  gl_Position = uMVPMatrix * aPosition;\n" +
        "  vTextureCoord = aTextureCoord;\n" +
        "}\n";

    /**
     * fragment shader source code
     * テクスチャRGBA×UniformRGBA →出力
     */
    private static final String mFragmentShader =
        "precision mediump float;\n" +
        "varying vec2 vTextureCoord;\n" +
        "uniform sampler2D sTexture;\n" +
        "uniform vec4 uColor;\n" +
        "vec4 tmpColor;\n" +
        "void main() {\n" +
        "  tmpColor = texture2D(sTexture, vTextureCoord);\n" +
        "  gl_FragColor.rgb = tmpColor.rgb*uColor.rgb*uColor.a;\n" +
        "  gl_FragColor.a = tmpColor.a*uColor.a;\n" +
        "}\n";

}
