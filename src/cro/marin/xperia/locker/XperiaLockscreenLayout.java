package cro.marin.xperia.locker;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import cro.marin.xperia.locker.R;

import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class XperiaLockscreenLayout extends RelativeLayout{

	private LinearLayout mContentLayout;
	private Calendar mCalendar;
	private ContentObserver mFormatChangeObserver;
	private boolean mAttached;
	private Context context = getContext();
    private String datev;
	private TextView mDateView;
	private TextView hint;
	
	private final Handler mHandler = new Handler();
	private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
				mCalendar = Calendar.getInstance();
			}
			mHandler.post(new Runnable() {
				public void run() {
					updateContent();
				}
			});
		}
	};

	public XperiaLockscreenLayout(Context context) {
		super(context);
	
		final LayoutInflater inflater = LayoutInflater.from(context);
		mContentLayout = (LinearLayout) inflater.inflate(R.layout.keyguard_xzlockscreen, null, true);
		addView(mContentLayout, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		
		setBg();
		
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Regular.ttf");
		mDateView = (TextView) mContentLayout.findViewById(R.id.date);
		mDateView.setTypeface(tf);
		hint = (TextView) findViewById(R.id.hintText);
		hint.setTypeface(tf);
		mCalendar = Calendar.getInstance();
		
		setFocusable(true);
		setFocusableInTouchMode(true);
		setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);

	}
	private void setBg() {
		WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
		Drawable wallpaper = wallpaperManager.getDrawable();
		wallpaper.setDither(true);
		
		final BlindsView blindsView = (BlindsView) findViewById(R.id.blindsview);
		blindsView.setBackground(wallpaper);
	}
	
	@SuppressLint({ "SimpleDateFormat", "DefaultLocale" })
	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();

		if (mAttached)
			return;
		mAttached = true;

		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_TIME_TICK);
		filter.addAction(Intent.ACTION_TIME_CHANGED);
		filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		getContext().registerReceiver(mIntentReceiver, filter);

		mFormatChangeObserver = new FormatChangeObserver();
		getContext().getContentResolver().registerContentObserver(Settings.System.CONTENT_URI, true, mFormatChangeObserver);
		updateContent();
	}

	private class FormatChangeObserver extends ContentObserver {
		public FormatChangeObserver() {
			super(new Handler());
		}

		@Override
		public void onChange(boolean selfChange) {
			updateContent();
		}
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();

		if (!mAttached)
			return;
		mAttached = false;

		getContext().unregisterReceiver(mIntentReceiver);
		getContext().getContentResolver().unregisterContentObserver(
				mFormatChangeObserver);
	}

	private void updateContent() {
		mCalendar.setTimeInMillis(System.currentTimeMillis());
        SimpleDateFormat date = new SimpleDateFormat("EEE, d. MMMM");
        datev = date.format(mCalendar.getTime());
		mDateView.setText(datev);
	}
}
