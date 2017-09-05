package com.seiryuan.android.glui;

public class Button2 extends Primitive {

	public float mWidth, mHeight;
    private Sprite mSprOFF;
    private Sprite mSprON;
    private Sprite mIconOFF;
    private Sprite mIconON;
    private boolean mState = false;

    public Button2(GLUI glui, Primitive p) {
        super(glui, p);
        mPosX = 0;
        mPosY = 0;
        mSprOFF = new Sprite(glui, p);
        mSprON = new Sprite(glui, p);
        mIconOFF = new Sprite(glui, p);
        mIconON = new Sprite(glui, p);
    }

    public void setPosition(float px, float py) {
        mPosX = px;
        mPosY = py;
        mSprOFF.setPosition(px, py);
        mSprON.setPosition(px, py);
        mIconOFF.setPosition(px, py);
        mIconON.setPosition(px, py);
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

    public void setIconSize(float w, float h) {
        mIconON.setSize(w, h);
        mIconOFF.setSize(w, h);
    }

    public void setIconTexture(float u0, float v0, float u1, float v1) {
        mIconOFF.setTexture(u1, v1);
        mIconON.setTexture(u0, v0);
    }

    public void On() {
        mState = true;
    }

    public void Off() {
        mState = false;
    }

    // override
    public void draw() {
        if(mState) {
            mSprON.draw();
            mIconON.draw();
        }
        else {
            mSprOFF.draw();
            mIconOFF.draw();
        }
    }
}
