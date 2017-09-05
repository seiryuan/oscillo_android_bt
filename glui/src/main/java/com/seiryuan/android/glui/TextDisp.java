/*
 * GLUI - OpenGL based GUI library
 * TextDisp.java: static text display primitive class
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

public class TextDisp extends Primitive {

    private int mLength;
    private int mMaxLen;
    private boolean mStatic;
	private FontInfo mFont;

    // constructor for static text mode
	public TextDisp(GLUI glui, Primitive p, FontInfo f, float x, float y, String s) {
        super(glui, p);
        mType = GLES20.GL_TRIANGLE_STRIP;
        mFont = f;
        mPosX = x;
        mPosY = y;
        mColorR = 1.0f;
        mColorG = 1.0f;
        mColorB = 1.0f;
        mColorA = 1.0f;
        mLength = s.length();
        mMaxLen = mLength;
        mNumVertex = mLength * 6; // vertecis*4 + dummy*2 (to form single strip)
        mVertexPos = mGLUI.allocVertex(mNumVertex);
        mStatic = true;

        setVertex(s);
    }

    // constructor for non-static mode
    public TextDisp(GLUI glui, Primitive p, FontInfo f, float x, float y, int max) {
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
        mNumVertex = 0;
        mMaxLen = max;
        mVertexPos = mGLUI.allocVertex(mMaxLen*6); // vertecis*4 + dummy*2 (to form single strip)
        mStatic = false;
    }

    public void setText(String s) {
        // you can not change text in static mode
        if(mStatic) return;

        // text length must be shorter than mMaxLen
        int len = s.length();
        if(len > mMaxLen) return;
        mLength = len;
        mNumVertex = mLength*6;
        setVertex(s);
    }

    private void setVertex(String s) {
        float w = mFont.width(); // width in pixel
        float h = mFont.height(); // height in pixel
        float uw = w / mGLUI.mTextureSize; // with in normalized value
        float vh = h / mGLUI.mTextureSize; // height in normalized value
        int pos = mVertexPos;
        float x0 = 0;
        float y0 = 0;

        // create vertex info for every character
		for(int i = 0; i < mLength; i++) {
			int ch = (int)s.charAt(i)-32;
			float u0 = (mFont.texU()+(ch%mFont.column())*w)/mGLUI.mTextureSize;
			float v0 = (mFont.texV()+(ch/mFont.column())*h)/mGLUI.mTextureSize;

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
            x0 += w;
            pos += 6;
		}
    }
}