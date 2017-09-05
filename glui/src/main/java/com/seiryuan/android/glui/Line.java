/*
 * GLUI - OpenGL based GUI library
 * Line.java: line primitive class
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

public class Line extends Primitive {
	private float mTexU, mTexV;

	public Line(GLUI glui, Primitive p, int n) {
		super(glui, p);
		mType = GLES20.GL_LINES;
		mNumVertex = n;
		mVertexPos = mGLUI.allocVertex(n);
		mPosX = 0f;
		mPosY = 0f;
		mColorR = 1.0f;
		mColorG = 1.0f;
		mColorB = 1.0f;
		mColorA = 1.0f;
		mTexU = 0f;
		mTexV = 0f;
	}

	public void setTexture(float u0, float v0) {
		mTexU = u0;
		mTexV = v0;
		for(int i = 0; i < mNumVertex; i++) {
			mGLUI.setVertexUV(mVertexPos+i,  mTexU/mGLUI.mTextureSize,  mTexV/mGLUI.mTextureSize);
		}
	}
	
	public void setVertex(int n, float x0, float y0) {
		mGLUI.setVertexXY(mVertexPos+n,  x0,  y0);
	}

}