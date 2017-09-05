/*
 * GLUI - OpenGL based GUI library
 * Button.java: button primitive class
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

public class Button extends Primitive {

	private float mWidth, mHeight;
    private Sprite mSprOFF;
    private Sprite mSprON;
    private boolean mState;

    public Button(GLUI glui, Primitive p) {
        super(glui, p);
    	mPosX = 0;
    	mPosY = 0;
        mSprOFF = new Sprite(glui, this);
        mSprON = new Sprite(glui, this);
        On();
    }

    public Button(GLUI glui, Primitive p, float x, float y, float w, float h, float u0, float v0, float u1, float v1) {
        super(glui, p);
        mPosX = 0;
        mPosY = 0;
        mSprOFF = new Sprite(glui, this);
        mSprON = new Sprite(glui, this);
        setPosition(x, y);
        setSize(w, h);
        setTexture(u0, v0, u1, v1);
        On();
    }

    @Override
    public void setPosition(float px, float py) {
        mPosX = px;
        mPosY = py;
    }

    @Override
    public void setColor(float r, float g, float b, float alpha) {
        mColorR = r;
        mColorG = g;
        mColorB = b;
        mColorA = alpha;
        mSprOFF.setColor(r, g, b, alpha);
        mSprON.setColor(r, g, b, alpha);
    }

    public void setSize(float w, float h) {
        mWidth = w;
        mHeight = h;
        mSprOFF.setSize(w, h);
        mSprON.setSize(w, h);
    }

	public void setTexture(float u0, float v0, float u1, float v1) {
        mSprOFF.setTexture(u1, v1);
        mSprON.setTexture(u0, v0);
    }

    public void On() {
        mState = true;
        mSprON.enable();
        mSprOFF.disable();
    }

    public void Off() {
        mState = false;
        mSprOFF.enable();
        mSprON.disable();
    }

    public boolean getState() {
        return mState;
    }

    public boolean checkRegion(float x, float y) {
        float sx = mPosX;
        float sy = mPosY;
        float sw = mWidth;
        float sh = mHeight;

        Primitive p = this;
        while(p.mParent != null) {
            p = p.mParent;
            sx += p.mPosX;
            sy += p.mPosY;
        }

        return (x > sx - sw / 2f) && (x < sx + sw / 2f) && (y > sy - sh / 2f) && (y < sy + sh / 2f);
    }

}
