/*
 * GLUI - OpenGL based GUI library
 * Primitive.java: basic primitive class
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

import java.util.ArrayList;
import android.opengl.GLES20;
import android.opengl.Matrix;

public class Primitive {

    protected GLUI mGLUI;          // reference to GLUI

    protected boolean mValid;
    protected boolean mVisible;

    protected Primitive mParent;    // parent Primitive
    protected ArrayList<Primitive> mChildren;    // child primitives

    protected int mType;            // primitive mType
    protected int mVertexPos;       // position in vertex array
    protected int mNumVertex;       // number of vertex

	protected float mPosX, mPosY;   // position (relative to mParent)
    protected float mPosWX, mPosWY; // position (world coordinate)
    protected float mRotZ;          // rotation (Z)
    protected float mScale;         // scale(X=Y=Z)
	protected float mColorR, mColorG, mColorB, mColorA;

    protected float mTgtColorR, mTgtColorG, mTgtColorB, mTgtColorA;
    protected float mTgtPosX, mTgtPosY;
    protected float mTgtScale;
    protected float mOrgColorR, mOrgColorG, mOrgColorB, mOrgColorA;
    protected float mOrgPosX, mOrgPosY;
    protected float mOrgScale;
    protected float mAnimFactor;
    protected int mAnimType;
    public static final int ANIM_LINEAR = 1;
    public static final int ANIM_SQUARE = 2;
    public static final int ANIM_CUBIC = 3;
    protected boolean mColorAnimValid;
    protected boolean mPosAnimValid;
    protected boolean mScaleAnimValid;

    protected float[] mTmpMatrix = new float[16];
	protected float[] mLocalMatrix = new float[16];
	protected float[] mViewMatrix = new float[16];

    public Primitive(GLUI glui) {
        mGLUI = glui;
        mParent = null;
        mChildren = new ArrayList<>();
        mValid = true;
        mVisible = true;
        mNumVertex = 0;
        mPosX = 0f;
        mPosY = 0f;
        mRotZ = 0f;
        mScale = 1.0f;
        mColorR = 1.0f;
        mColorG = 1.0f;
        mColorB = 1.0f;
        mColorA = 1.0f;
        mColorAnimValid = false;
        mPosAnimValid = false;
        mScaleAnimValid = false;
    }

	public Primitive(GLUI glui, Primitive p) {
        mGLUI = glui;
		if(p == null) {
			mParent = glui.world;
		}
		else {
			mParent = p;
		}
		mParent.addChild(this);
		mChildren = new ArrayList<>();
        mValid = true;
        mVisible = true;
        mNumVertex = 0;
        mPosX = 0;
        mPosY = 0;
        mColorR = 1.0f;
        mColorG = 1.0f;
        mColorB = 1.0f;
        mColorA = 1.0f;
        mColorAnimValid = false;
        mPosAnimValid = false;
        mScaleAnimValid = false;
	}

    public void enable() {
        mValid = true;
    }

    public void disable() {
        mValid = false;
    }

    public void show() {
        mVisible = true;
    }

    public void hide() {
        mVisible = false;
    }

    public void addChild(Primitive p) {
		mChildren.add(p);
	}

	public Primitive getParent() {
		return mParent;
	}

	public void setPosition(float x0, float y0) {
		mPosX = x0;
		mPosY = y0;
	}

	public void setColor(float r0, float g0, float b0, float alpha0) {
		mColorR = r0;
		mColorG = g0;
		mColorB = b0;
		mColorA = alpha0;
	}

	public void setRotation(float angle) {
		mRotZ = angle;
	}

	public void setViewMatrix() {
        // Calculate perspective trans Matrix
        /*
        Matrix.setIdentityM(mTmpMatrix, 0);
        if(mParent != mGLUI.world) Matrix.translateM(mTmpMatrix, 0, mParent.mPosX, mParent.mPosY, 0.0f);
        */
        Matrix.setIdentityM(mTmpMatrix, 0);
        mPosWX = mPosX;
        mPosWY = mPosY;
        if((mParent != null)&&(mParent != mGLUI.world)) {
            mPosWX += mParent.mPosWX;
            mPosWY += mParent.mPosWY;
        }
        Matrix.translateM(mTmpMatrix, 0, mPosWX, mPosWY, 0.0f); // Translastion (world coordinate)
        Matrix.rotateM(mLocalMatrix, 0, mTmpMatrix, 0, mRotZ, 0, 0, 1.0f); // Rotation (mRotZ)
        Matrix.multiplyMM(mViewMatrix, 0, mGLUI.mVPMatrix, 0, mLocalMatrix, 0);

        // Set parameters to shader
        GLES20.glUniformMatrix4fv(mGLUI.muMVPMatrixHandle, 1, false, mViewMatrix, 0);
    }

    public void setColorMatrix() {
        // Set color parameters to shader
        GLES20.glUniform4f(mGLUI.muColorHandle, mColorR, mColorG, mColorB, mColorA);
    }

    public void setTargetColor(float r, float g, float b, float a) {
        mTgtColorR = r;
        mTgtColorG = g;
        mTgtColorB = b;
        mTgtColorA = a;
        mColorAnimValid = true;
    }

    public void setTargetPosition(float x, float y) {
        mTgtPosX = x;
        mTgtPosY = y;
        mPosAnimValid = true;
    }

    public void startAnimation(int type) {
        if(mColorAnimValid) {
            mOrgColorR = mColorR;
            mOrgColorG = mColorG;
            mOrgColorB = mColorB;
            mOrgColorA = mColorA;
        }
        if(mPosAnimValid) {
            mOrgPosX = mPosX;
            mOrgPosY = mPosY;
        }
        mAnimFactor = 0;
        mAnimType = type;
    }

    /*
     *
     */
    public void updateChildren() {
        int len = mChildren.size();
        for(int i = 0; i < len; i++) {
            mChildren.get(i).update();
        }
    }

    public void update() {

        // nothing to do for disabled primitive
        if(!mValid) return;

        mAnimFactor += 0.1;
        float f1, f2;
        switch(mAnimType) {
            case ANIM_CUBIC:
                f2 = (1-mAnimFactor)*(1-mAnimFactor)*(1-mAnimFactor);
                f1 = 1.0f-f2;
                break;
            case ANIM_SQUARE:
                f2 = (1-mAnimFactor)*(1-mAnimFactor);
                f1 = 1.0f-f2;
                break;
            case ANIM_LINEAR:
            default:
                f1 = mAnimFactor;
                f2 = 1.0f-f1;
                break;
        }
        if(mColorAnimValid) {
            mColorR = f1*mTgtColorR+f2*mOrgColorR;
            mColorG = f1*mTgtColorG+f2*mOrgColorG;
            mColorB = f1*mTgtColorB+f2*mOrgColorB;
            mColorA = f1*mTgtColorA+f2*mOrgColorA;
        }

        if(mPosAnimValid) {
            mPosX = f1*mTgtPosX+f2*mOrgPosX;
            mPosY = f1*mTgtPosY+f2*mOrgPosY;
        }

        if(mAnimFactor >= 1.0) {
            mColorAnimValid = false;
            mPosAnimValid = false;
        }

        // Update child primitives
        updateChildren();
    }

    public void drawChildren() {
        int len = mChildren.size();
        for(int i = 0; i < len; i++) {
            mChildren.get(i).draw();
        }
    }

    /*
	 *
	 */
	public void draw() {

        // nothing to do for disabled primitive
        if(!mValid) return;
        if(!mVisible) return;

        setViewMatrix();
        setColorMatrix();

        // Draw Call
        if(mNumVertex > 0) {
            GLES20.glDrawArrays(mType, mVertexPos, mNumVertex);
            mGLUI.checkGlError("glDrawArrays");
        }

        // Draw child primitives
	    drawChildren();
	}
}
