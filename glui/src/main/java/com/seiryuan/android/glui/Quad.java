/*
 * GLUI - OpenGL based GUI library
 * Quad.java: free shaped quadrangle primitive class
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

public class Quad extends Primitive {

    private float mTexU, mTexV; // texture coordinate of left-upper coner (unit=texel)

	public Quad(GLUI glui, Primitive p) {
        super(glui, p);
        mType = GLES20.GL_TRIANGLE_STRIP;
        mNumVertex = 4;
        mVertexPos = mGLUI.allocVertex(4);
        mPosX = 0f;
		mPosY = 0f;
        mTexU = 0;
        mTexV = 0;
	   	mColorR = 1.0f;
    	mColorG = 1.0f;
    	mColorB = 1.0f;
    	mColorA = 1.0f;
	}

    public void setTexture(float u0, float v0) {
        mTexU = u0;
        mTexV = v0;
    }

    public void setShape(float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3) {
        mGLUI.setVertexXY(mVertexPos, x0, y0);
        mGLUI.setVertexUV(mVertexPos, mTexU/mGLUI.mTextureSize, mTexV/mGLUI.mTextureSize);
        mGLUI.setVertexXY(mVertexPos+1, x1, y1);
        mGLUI.setVertexUV(mVertexPos+1, (mTexU+x1-x0)/mGLUI.mTextureSize, (mTexV+y1-y0)/mGLUI.mTextureSize);
        mGLUI.setVertexXY(mVertexPos+2, x2, y2);
        mGLUI.setVertexUV(mVertexPos+2, (mTexU+x2-x0)/mGLUI.mTextureSize, (mTexV+y2-y0)/mGLUI.mTextureSize);
        mGLUI.setVertexXY(mVertexPos+3, x3, y3);
        mGLUI.setVertexUV(mVertexPos+3, (mTexU+x3-x0)/mGLUI.mTextureSize, (mTexV+y3-y0)/mGLUI.mTextureSize);
    }
}