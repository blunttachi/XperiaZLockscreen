package cro.marin.xperia.locker;

import android.graphics.Rect;

public class BlindInfo {
	private final Rect mBounds;
	private float mRotationX, mRotationY, mRotationZ;
	private float mScale = 1f;
	private float mYoffset = 0f;
	private boolean mDrawStroke = false;

	public BlindInfo(int l, int t, int r, int b) {
		mBounds = new Rect(l, t, r, b);
	}

	public int getHeight() {
		return mBounds.height();
	}

	public int getWidth() {
		return mBounds.width();
	}

	public int getLeft() {
		return mBounds.left;
	}

	public int getRight() {
		return mBounds.right;
	}

	public int getTop() {
		return mBounds.top;
	}

	public int getBottom() {
		return mBounds.bottom;
	}

	public void setRotations(float xRotation, float yRotation, float zRotation) {
		mRotationX = xRotation;
		mRotationY = yRotation;
		mRotationZ = zRotation;
	}

	public float getRotationX() {
		return mRotationX;
	}

	public float getRotationY() {
		return mRotationY;
	}

	public float getRotationZ() {
		return mRotationZ;
	}

	public void setScale(float s) {
		mScale = s;
	}

	public float getScale() {
		return mScale;
	}

	public void setYoffset(float offset) {
		mYoffset = offset;
	}

	public float getYoffset() {
		return mYoffset;
	}

	public void setDrawStroke(boolean d) {
		mDrawStroke = d;
	}

	public boolean getDrawStroke() {
		return mDrawStroke;
	}
}
