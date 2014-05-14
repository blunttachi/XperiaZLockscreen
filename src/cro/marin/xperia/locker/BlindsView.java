package cro.marin.xperia.locker;

import java.util.ArrayList;

import cro.marin.xperia.locker.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.LinearLayout;
	

public class BlindsView extends LinearLayout {

	private static final boolean LOG_ON = true;
	private static String LOG_TAG; 
	private static final float CONFIG_MAX_ROTATIONX = 45f;
	private static final float CONFIG_MAX_ROTATIONY = 15f;
	private static final float CONFIG_CAMERA_DISTANCE_Z = -35;
	private static float mMaxAffectRadius;
	private static final float CONFIG_MIN_SCALING = 0.97f;
	private static final float CONFIG_MAX_YOFFSET = 16;
	private static final int CONFIG_BLINDSTROKE_BASECOLOR = Color.DKGRAY;
	private static final int CONFIG_BLINDSTROKE_ALPHA = 175;
	private static final int CONFIG_BLINDSTROKE_BEVEL_ANGLE = 45;
	private static float mConfigStrokeWidth;

	private Bitmap mUndistortedBitmap;
	private Canvas mUndistortedCanvas;
	private BitmapDrawable mBgDrawable;
	private Paint mBlindPaint, mBlindStrokePaint;
	private final Camera mCamera = new Camera();

	private ArrayList<BlindInfo> mBlindSet = null;
	private boolean mIsInBlindMode = false;

	public BlindsView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();

	}

	public BlindsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public BlindsView(Context context) {
		super(context);
		init();
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		if (LOG_ON) {
			Log.d(LOG_TAG, "dispatchDraw (dispatching draw calls to all children)");
		}
		drawCustomStuff(canvas);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		setupBlinds((int) getResources().getDimension(R.dimen.blindHeight));
		if (LOG_ON) {
			Log.d(LOG_TAG,"onLayout. Layout properties changed - blinds set rebuilt. New set contains " + mBlindSet.size() + " blinds");
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_MOVE:
			mIsInBlindMode = true;
			calculateBlindRotations(event.getX(), event.getY());
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			mIsInBlindMode = false;
			((Activity)getContext()).finish();
			break;
		default:
			
			break;

		}
		invalidate();
		return true;
	}

	private void init() {
		LOG_TAG = this.getClass().getSimpleName();

		mBlindPaint = new Paint();
		mBlindPaint.setStyle(Paint.Style.FILL);
		mBlindPaint.setAntiAlias(true);
		mBlindPaint.setFilterBitmap(true);

		mConfigStrokeWidth = getResources().getDimension(R.dimen.blindStrokeWidth);
		mBlindStrokePaint = new Paint();
		mBlindStrokePaint.setColor(CONFIG_BLINDSTROKE_BASECOLOR);
		mBlindStrokePaint.setAlpha(CONFIG_BLINDSTROKE_ALPHA);
		mBlindStrokePaint.setStrokeWidth(mConfigStrokeWidth);
		mBlindStrokePaint.setAntiAlias(true);
		mBlindStrokePaint.setFilterBitmap(true);

		mMaxAffectRadius = getResources().getDimension(R.dimen.touchEffectRadius);
	}

	private void drawCustomStuff(Canvas screenCanvas) {
		if (LOG_ON) {
			Log.d(LOG_TAG, "drawCustomStuff (doing the custom drawing of this ViewGroup)");
		}

		final boolean initBmpAndCanvas = (mIsInBlindMode && (!(mUndistortedBitmap != null && !mUndistortedBitmap.isRecycled())));

		if (!mIsInBlindMode || (mIsInBlindMode && initBmpAndCanvas)) {
	
			if (mIsInBlindMode && initBmpAndCanvas) {
				mUndistortedBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
				mUndistortedCanvas = new Canvas(mUndistortedBitmap);
			}

			Canvas canvasToDrawTo = mIsInBlindMode ? mUndistortedCanvas : screenCanvas;

			drawUndistorted(canvasToDrawTo);
		}
		if (mIsInBlindMode) {			
			drawBlinds(screenCanvas);
		}
	}

	private void drawUndistorted(Canvas canvas) {
		if (mBgDrawable != null) {
			mBgDrawable.draw(canvas);
		}
		super.dispatchDraw(canvas);
	}

	private void drawBlinds(Canvas canvas) {
	
		for (BlindInfo blind : mBlindSet) {
			drawBlind(blind, canvas);
		}
	}

	private void drawBlind(BlindInfo info, Canvas canvas) {
	
		final int width = info.getWidth();
		final int height = info.getHeight();
		final int coordX = info.getLeft();
		final int coordY = info.getTop();
		final float xRotation = info.getRotationX();
		final float yRotation = info.getRotationY();
		final float zRotation = info.getRotationZ();
		final float scale = info.getScale();
		final float yOffset = info.getYoffset();
		final boolean drawBottomStroke = info.getDrawStroke();

		canvas.save();
		mCamera.save();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
			mCamera.setLocation(0, 0, CONFIG_CAMERA_DISTANCE_Z);
		} else {
		}
		
		canvas.translate((coordX + (width / 2f)), (coordY + (height / 2f)));

		mCamera.rotateY(yRotation);
		mCamera.rotateX(xRotation);
		canvas.scale(scale, scale, 0f, 0f);
		canvas.translate(0f, yOffset);

		Matrix cameraMatrix = new Matrix();
		mCamera.getMatrix(cameraMatrix);
		canvas.concat(cameraMatrix);

		mBlindPaint.setColorFilter(calculateLight(xRotation));

		final Rect src = new Rect(coordX, coordY, (coordX + width), (coordY + height));
		final RectF dst = new RectF(-(width / 2f), -(height / 2f), width / 2f, height / 2f);
		canvas.drawBitmap(mUndistortedBitmap, src, dst, mBlindPaint);
		if (drawBottomStroke) {
			mBlindStrokePaint.setColorFilter(calculateLight(xRotation + CONFIG_BLINDSTROKE_BEVEL_ANGLE));
			canvas.drawLine(dst.left, (dst.bottom - mConfigStrokeWidth / 2f), dst.right, (dst.bottom - mConfigStrokeWidth / 2f), mBlindStrokePaint);
		}

		mCamera.restore();
		canvas.restore();

		if (LOG_ON) {
			Log.d(LOG_TAG, "Drew blind with size " + width + " by " + height + " px with rotation (" + xRotation + ", " + yRotation + ", " + zRotation + ") (x,y,z) at coordinates " + coordX + ", " + coordY);
		}
	}

	private void setupBlinds(int blindHeight) {

		if (blindHeight == 0) {
			throw new IllegalArgumentException("baseHeight must be >0");
		}

		ArrayList<BlindInfo> bi = new ArrayList<BlindInfo>();
		int accumulatedHeight = 0;
		do {
			bi.add(new BlindInfo(0, accumulatedHeight, getWidth(), accumulatedHeight + blindHeight));
			accumulatedHeight += blindHeight;
		} while (accumulatedHeight < getHeight());
		mBlindSet = bi;
	}

	private synchronized void calculateBlindRotations(float xPos, float yPos) {

		float currentBlindPivotY;
		float normalizedVerticalDistanceFromTouch;

		for (BlindInfo currentBlind : mBlindSet) {
			currentBlindPivotY = currentBlind.getTop() + (float) currentBlind.getHeight() / 2f;

			normalizedVerticalDistanceFromTouch = Math.abs((yPos - currentBlindPivotY) / mMaxAffectRadius);

			float xRotation = 0;
			float yRotation = 0;
			float scaling = 1f;
			float yOffset = 0f;
			boolean drawStroke = false;
			
			if (normalizedVerticalDistanceFromTouch <= 1f) {

				final double normalizedRotationX = Math
						.max(0d,
								(-Math.pow(((normalizedVerticalDistanceFromTouch - 0.55f) * 2f), 2) + 1));

				if ((currentBlindPivotY < yPos)) {
					xRotation = (float) -(CONFIG_MAX_ROTATIONX * normalizedRotationX);
				} else {
					xRotation = (float) (CONFIG_MAX_ROTATIONX * normalizedRotationX);
				}

				final float normalizedHorizontalDistanceFromPivot = ((xPos / getWidth()) - 0.5f) / 0.5f;
				final float linearDeclineFactor = 1 - normalizedVerticalDistanceFromTouch;
				yRotation = CONFIG_MAX_ROTATIONY * normalizedHorizontalDistanceFromPivot * linearDeclineFactor;

				scaling = 1f - (1f - normalizedVerticalDistanceFromTouch * normalizedVerticalDistanceFromTouch) * (1f - CONFIG_MIN_SCALING);

				yOffset = ((1f - normalizedVerticalDistanceFromTouch * normalizedVerticalDistanceFromTouch)) * CONFIG_MAX_YOFFSET;

				drawStroke = true;

			}
			currentBlind.setRotations(xRotation, yRotation, 0f);
			currentBlind.setScale(scaling);
			currentBlind.setYoffset(yOffset);
			currentBlind.setDrawStroke(drawStroke);
		}

	}

	private static final int AMBIENT_LIGHT = 55;

	private static final int DIFFUSE_LIGHT = 255;

	private static final float SPECULAR_LIGHT = 70;

	private static final float SHININESS = 255;

	private static final int MAX_INTENSITY = 0xFF;

	private static final float LIGHT_SOURCE_ANGLE = 38f;

	private LightingColorFilter calculateLight(float rotation) {
		rotation -= LIGHT_SOURCE_ANGLE;
		final double cosRotation = Math.cos(Math.PI * rotation / 180);
		int intensity = AMBIENT_LIGHT + (int) (DIFFUSE_LIGHT * cosRotation);
		int highlightIntensity = (int) (SPECULAR_LIGHT * Math.pow(cosRotation, SHININESS));

		if (intensity > MAX_INTENSITY) {
			intensity = MAX_INTENSITY;
		}
		if (highlightIntensity > MAX_INTENSITY) {
			highlightIntensity = MAX_INTENSITY;
		}

		final int light = Color.rgb(intensity, intensity, intensity);
		final int highlight = Color.rgb(highlightIntensity, highlightIntensity,
				highlightIntensity);

		return new LightingColorFilter(light, highlight);
	}

	public void setBackground(int id) {
		mBgDrawable = (BitmapDrawable) getResources().getDrawable(id);
		centerBgDrawable();
	}

	@Override
	public void setBackground(Drawable background) {
		mBgDrawable = (BitmapDrawable) background;
		centerBgDrawable();
	}

	private void centerBgDrawable() {
		if (mBgDrawable != null) {
			final DisplayMetrics dm = getResources().getDisplayMetrics();
			mBgDrawable.setTargetDensity(dm);
			mBgDrawable.setGravity(Gravity.CENTER);
			mBgDrawable.setBounds(0, 0, dm.widthPixels, dm.heightPixels);
		}
		postInvalidate();
	}

}