/*
 * GLUI - OpenGL based GUI library
 * NinePatch.java: 9patch stprite primitive class
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

public class NinePatch extends Primitive {

	private float mWidth, mHeight;
	private float mTexU, mTexV;
	private float mTexWidth, mTexHeight;
	private float mConstWidth, mConstHeight;

	private float[][] uc = new float[4][4];
	private float[][] vc = new float[4][4];    
	private float[][] xc = new float[4][4];    
	private float[][] yc = new float[4][4];    

	public NinePatch(GLUI glui, Primitive p) {
		super(glui, p);
		mType = GLES20.GL_TRIANGLE_STRIP;
		mNumVertex = 24; //3strip x 8vertices
		mVertexPos = mGLUI.allocVertex(24);
		mPosX = 0;
		mPosY = 0;
		mTexU = 0;
		mTexV = 0;
		mColorR = 1.0f;
		mColorG = 1.0f;
		mColorB = 1.0f;
		mColorA = 1.0f;
	}

	public NinePatch(GLUI glui, Primitive p, float x, float y, float w, float h,
					 float u, float v, float tw, float th, float cw, float ch) {
		super(glui, p);
		mType = GLES20.GL_TRIANGLE_STRIP;
		mNumVertex = 24; //3strip x 8vertices
		mVertexPos = mGLUI.allocVertex(24);
		mPosX = x;
		mPosY = y;
		mWidth = w;
		mHeight = h;
		mColorR = 1.0f;
		mColorG = 1.0f;
		mColorB = 1.0f;
		mColorA = 1.0f;
		setTexture(u, v, tw, th, cw, ch);
		update();
	}

    public void setSize(float w, float h) {
        mWidth = w;
        mHeight = h;
    }

    public void setTexture(float u, float v, float tw, float th, float cw, float ch) {
        mTexU = u;
        mTexV = v;
        mTexWidth = tw;
        mTexHeight = th;
        mConstWidth = cw;
        mConstHeight = ch;
    }

    public void update() {
		int i, j;
		
		for(i = 0; i < 4; i++) {
			for(j = 0; j < 4; j++) {
				switch(i) {
				case 0: 
					uc[i][j] = mTexU;
					xc[i][j] = -mWidth /2f;
					break;
				case 1: 
					uc[i][j] = mTexU +(mTexWidth - mConstWidth)/2f;
					xc[i][j] = -mWidth /2f+(mTexWidth - mConstWidth)/2f;
					break;
				case 2: 
					uc[i][j] = mTexU + mTexWidth -(mTexWidth - mConstWidth)/2f;
					xc[i][j] = mWidth /2f-(mTexWidth - mConstWidth)/2f;
					break;
				case 3:
					uc[i][j] = mTexU + mTexWidth;
					xc[i][j] = mWidth /2f;
					break;
				}
				switch(j) {
				case 0:
					vc[i][j] = mTexV;
					yc[i][j] = -mHeight /2f;
					break;
				case 1:
					vc[i][j] = mTexV +(mTexHeight - mConstHeight)/2f;
					yc[i][j] = -mHeight /2f+(mTexHeight - mConstHeight)/2f;
					break;
				case 2:
					vc[i][j] = mTexV + mTexHeight -(mTexHeight - mConstHeight)/2f;
					yc[i][j] = mHeight /2f-(mTexHeight - mConstHeight)/2f;
					break;
				case 3:
					vc[i][j] = mTexV + mTexHeight;
					yc[i][j] = mHeight /2f;
					break;
				}
			}
		}
		
    	int pos = mVertexPos;
		for(i = 0; i < 3; i++) {
			for(j = 0; j < 4; j++) {
				mGLUI.setVertexXY(pos, xc[i][j], yc[i][j]);
                mGLUI.setVertexUV(pos, uc[i][j]/mGLUI.mTextureSize, vc[i][j]/mGLUI.mTextureSize);
                pos++;
				mGLUI.setVertexXY(pos, xc[i+1][j], yc[i+1][j]);
                mGLUI.setVertexUV(pos, uc[i+1][j]/mGLUI.mTextureSize, vc[i+1][j]/mGLUI.mTextureSize);
                pos++;
			}
		}
	}

	@Override
	public void draw() {

        // nothing to do for disabled primitive
        if(!mValid) return;

        setViewMatrix();
        setColorMatrix();

        // Draw Call for 3strips
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, mVertexPos, 8);
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, mVertexPos+8, 8);
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, mVertexPos+16, 8);
		mGLUI.checkGlError("glDrawArrays");

        // Draw child primitives
		drawChildren();
    }
}