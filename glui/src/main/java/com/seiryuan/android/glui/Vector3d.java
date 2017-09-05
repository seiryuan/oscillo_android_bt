package com.seiryuan.android.glui;

public class Vector3d {
        
        public float x, y, z;
        
        public Vector3d(){
        	x = y = z = 0;
        }
        
        public Vector3d( float px, float py, float pz ){
        	x = px;
        	y = py;
        	z = pz;
        }
        
        public void setXYZ(float px, float py, float pz) {
        	x = px;
        	y = py;
        	z = pz;
        }

        public void setVect(Vector3d v) {
        	x = v.x;
        	y = v.y;
        	z = v.z;
        }
        
        public float getX() { return x; }
        public float getY() { return y; }
        float getZ() { return z; }

        //大きさを取得する
        public float length() {
                return (float)Math.sqrt( x*x + y*y + z*z );
        }
        
        //ベクタを加算
        public void addVect(Vector3d v) {
        	x = x + v.x;
        	y = y + v.y;
        	z = z + v.z;
        }

        //ベクタを減算
        public void subVect(Vector3d v) {
        	x = x - v.x;
        	y = y - v.y;
        	z = z - v.z;
        }

        //ベクタにスカラを乗算除算
        public void mul(float d) {
        	x = x*d;
        	y = y*d;
        	z = z*d;
        }
        public void div(float d) {
        	x = x/d;
        	y = y/d;
        	z = z/d;
        }

        //正規化する
        public void normalize() {
        	float len = length();
        	x = x/len;
        	y = y/len;
        	z = z/len;
        }
        //内積を取得する
        public static float dotProduct(Vector3d v1, Vector3d v2){
        	return v1.x*v2.x + v1.y*v2.y + v1.z*v2.z;
        }
        
        //引数２ベクタの外積を設定する
        public void outerProduct(Vector3d v1, Vector3d v2){
        	x = v1.y*v2.z - v1.z*v2.y;
        	y = v1.z*v2.x - v1.x*v2.z;
        	z = v1.x*v2.y - v1.y*v2.x;
        }
        
       
}