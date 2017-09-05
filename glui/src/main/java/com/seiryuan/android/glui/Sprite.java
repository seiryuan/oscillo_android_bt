/*
 * GLUI - OpenGL based GUI library
 * Sprite.java: 2D sprite primitive class
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

public class Sprite extends Primitive {

	private float mTexU, mTexV; // texture coordinate of left-upper coner (unit=texel)
	private float mWidth, mHeight;

	public Sprite(GLUI glui, Primitive p) {
		super(glui, p);
		mType = GLES20.GL_TRIANGLE_STRIP;
		mNumVertex = 4;
		mVertexPos = mGLUI.allocVertex(4);
		mTexU = 0;
		mTexV = 0;
	}

    public Sprite(GLUI glui, Primitive p, float x, float y, float w, float h, float u, float v) {
        super(glui, p);
        mType = GLES20.GL_TRIANGLE_STRIP;
        mNumVertex = 4;
        mVertexPos = mGLUI.allocVertex(4);
        mPosX = x;
        mPosY = y;
        setSize(w, h);
        setTexture(u, v);
    }

    public void setTexture(float u0, float v0) {
        mTexU = u0;
        mTexV = v0;

        mGLUI.setVertexUV(mVertexPos, mTexU/mGLUI.mTextureSize, mTexV/mGLUI.mTextureSize);
        mGLUI.setVertexUV(mVertexPos+1, (mTexU+mWidth)/mGLUI.mTextureSize, mTexV/mGLUI.mTextureSize);
        mGLUI.setVertexUV(mVertexPos+2, mTexU/mGLUI.mTextureSize, (mTexV+mHeight)/mGLUI.mTextureSize);
        mGLUI.setVertexUV(mVertexPos+3, (mTexU+mWidth)/mGLUI.mTextureSize, (mTexV+mHeight)/mGLUI.mTextureSize);
    }

    public void setSize(float w, float h) {
        mWidth = w;
        mHeight = h;

        mGLUI.setVertexXY(mVertexPos, -mWidth/2, -mHeight/2);
        mGLUI.setVertexXY(mVertexPos+1, mWidth/2, -mHeight/2);
        mGLUI.setVertexXY(mVertexPos+2, -mWidth/2, mHeight/2);
        mGLUI.setVertexXY(mVertexPos+3, mWidth/2, mHeight/2);
    }
}
