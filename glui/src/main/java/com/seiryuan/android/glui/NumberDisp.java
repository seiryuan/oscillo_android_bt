/*
 * GLUI - OpenGL based GUI library
 * NumberDisp.java: numeric value display primitive class
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

import android.opengl.GLES20;

public class NumberDisp extends Primitive {

	private int mLength;
	private int mMaxLen;
	private boolean mStatic;
	private FontInfo mFont;

	private int position;
	
	public NumberDisp(GLUI glui, Primitive p, FontInfo f, float x, float y, int len) {
		super(glui, p);
		mType = GLES20.GL_TRIANGLE_STRIP;
		mFont = f;
		mPosX = x;
		mPosY = y;
		mColorR = 1.0f;
		mColorG = 1.0f;
		mColorB = 1.0f;
		mColorA = 1.0f;
		mLength = 0;
		mMaxLen = len;
		mNumVertex = mMaxLen * 6; // vertecis*4 + dummy*2 (to form single strip)
		mVertexPos = mGLUI.allocVertex(mNumVertex);
	}

	public void setValue(int digit, int fmt, int value) {
        mLength = digit;
        float w = mFont.width(); // width in pixel
		float h = mFont.height(); // height in pixel
		float uw = w / mGLUI.mTextureSize; // with in normalized value
		float vh = h / mGLUI.mTextureSize; // height in normalized value
		int pos = mVertexPos;
		float x0 = 0;
		float y0 = 0;

		int v1;
        boolean first = true;

		if(value < 0) value = Math.abs(value);
		for(int i = 0; i < digit; i++) {
			v1 = (value/(int)Math.pow(10, (digit-i-1)))%10;
			if((fmt == GLUI.ZEROPADDING)||(v1 != 0)||(first == false)||(i == digit-1)) {
				float u0 = (mFont.texU()+v1*w)/mGLUI.mTextureSize;
				float v0 = mFont.texV()/mGLUI.mTextureSize;

                mGLUI.setVertexUV(pos, u0, v0);
                mGLUI.setVertexXY(pos, x0, y0);
                mGLUI.setVertexUV(pos+1, u0+uw, v0);
                mGLUI.setVertexXY(pos+1, x0+w, y0);
                mGLUI.setVertexUV(pos+2, u0, v0+vh);
                mGLUI.setVertexXY(pos+2, x0, y0+h);
                mGLUI.setVertexUV(pos+3, u0+uw, v0+vh);
                mGLUI.setVertexXY(pos+3, x0+w, y0+h);
                mGLUI.setVertexXY(pos+4, x0+w, y0+h);
                mGLUI.setVertexXY(pos+5, x0+w, y0);
                pos += 6;
                first = false;
			}
            x0 += w;
		}
	}

    public void setSignedValue(int digit, int fmt, int value) {
        mLength = digit;
        float w = mFont.width(); // width in pixel
        float h = mFont.height(); // height in pixel
        float uw = w / mGLUI.mTextureSize; // with in normalized value
        float vh = h / mGLUI.mTextureSize; // height in normalized value
        int pos = mVertexPos;
        float x0 = 0;
        float y0 = 0;
        float u0, v0;
        int v1;
        boolean first = true;

        for(int i = 0; i < digit; i++) {
            v1 = (Math.abs(value)/(int)Math.pow(10, (digit-i-1)))%10;
            if((fmt == GLUI.ZEROPADDING)||(v1 != 0)||(first == false)||(i == digit-1)) {
                if(first) {
                    if(value < 0) {
                        u0 = (mFont.texU() + 11 * w) / mGLUI.mTextureSize;
                        v0 = mFont.texV() / mGLUI.mTextureSize;
                        mGLUI.setVertexUV(pos, u0, v0);
                        mGLUI.setVertexXY(pos, x0, y0);
                        mGLUI.setVertexUV(pos + 1, u0 + uw, v0);
                        mGLUI.setVertexXY(pos + 1, x0 + w, y0);
                        mGLUI.setVertexUV(pos + 2, u0, v0 + vh);
                        mGLUI.setVertexXY(pos + 2, x0, y0 + h);
                        mGLUI.setVertexUV(pos + 3, u0 + uw, v0 + vh);
                        mGLUI.setVertexXY(pos + 3, x0 + w, y0 + h);
                        mGLUI.setVertexXY(pos + 4, x0 + w, y0 + h);
                        mGLUI.setVertexXY(pos + 5, x0 + w, y0);
                        pos += 6;
                        value = Math.abs(value);
                    }
                    x0 += w;
                }
                u0 = (mFont.texU()+v1*w)/mGLUI.mTextureSize;
                v0 = mFont.texV()/mGLUI.mTextureSize;
                mGLUI.setVertexUV(pos, u0, v0);
                mGLUI.setVertexXY(pos, x0, y0);
                mGLUI.setVertexUV(pos+1, u0+uw, v0);
                mGLUI.setVertexXY(pos+1, x0+w, y0);
                mGLUI.setVertexUV(pos+2, u0, v0+vh);
                mGLUI.setVertexXY(pos+2, x0, y0+h);
                mGLUI.setVertexUV(pos+3, u0+uw, v0+vh);
                mGLUI.setVertexXY(pos+3, x0+w, y0+h);
                mGLUI.setVertexXY(pos+4, x0+w, y0+h);
                mGLUI.setVertexXY(pos+5, x0+w, y0);
                pos += 6;
                first = false;
            }
            x0 += w;
        }
    }

    public void draw() {

        // nothing to do for disabled primitive
        if(!mValid) return;

        setViewMatrix();
        setColorMatrix();

        for(int i = 0; i < mLength; i++) {
            GLES20.glDrawArrays(mType, mVertexPos+i*6, 4);
            mGLUI.checkGlError("glDrawArrays");
        }

        // Draw child primitives
        drawChildren();
    }
}