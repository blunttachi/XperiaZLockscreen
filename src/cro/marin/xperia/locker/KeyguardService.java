package cro.marin.xperia.locker;

import cro.marin.xperia.locker.settings.Settings;

import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

@SuppressWarnings("deprecation")
public class KeyguardService extends Service {

	private KeyguardLock mKeyguardLock;
	private boolean mEnabled;

	@Override
	public void onStart(Intent intent, int startId) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		mEnabled = sp.getBoolean(Settings.Keys.LOCKER_ENABLED, false);
		if (!mEnabled) {
			stopSelf();
			return;
		}
		
		mKeyguardLock = ((KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE)).newKeyguardLock("Xperia Locker");
		mKeyguardLock.disableKeyguard();

		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		intentFilter.setPriority(32000);
		registerReceiver(mReceiver, intentFilter);
	}

	@Override
	public void onDestroy() {
		if (!mEnabled)
			return;

		unregisterReceiver(mReceiver);
		mKeyguardLock.reenableKeyguard();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			TelephonyManager ts = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
			int callState = ts.getCallState();
			if (callState == TelephonyManager.CALL_STATE_IDLE) {
				context.startActivity(new Intent(context, XperiaLockscreen.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
			}
		}

	};

}