package com.seiryuan.android.glui;

public class FontInfo {

    private float mWidth;
    private float mHeight;
    private float mTexU;
    private float mTexV;
    private int mRow;
    private int mColumn;

    public FontInfo(float w, float h, float u, float v, int row, int column) {
        mWidth = w;
        mHeight = h;
        mTexU = u;
        mTexV = v;
        mRow = row;
        mColumn = column;
    }

    public float width() {
        return mWidth;
    }

    public float height() {
        return mHeight;
    }

    public float texU() {
        return mTexU;
    }
    public float texV() {
        return mTexV;
    }

    public int row() {
        return mRow;
    }
    public int column() {
        return mColumn;
    }

}

