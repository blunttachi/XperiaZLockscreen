package cro.marin.xperia.locker.settings;


import cro.marin.xperia.locker.R;

import cro.marin.xperia.locker.KeyguardService;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.content.Context;
import android.content.Intent;

public class Settings extends PreferenceActivity implements
		OnPreferenceClickListener {

	public static class Keys {
		public static final String LOCKER_ENABLED = "locker_enabled";
	}

	private CheckBoxPreference mEnabled;
	protected String clock;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		addPreferencesFromResource(R.xml.settings);

		mEnabled = (CheckBoxPreference) findPreference(Keys.LOCKER_ENABLED);
		mEnabled.setOnPreferenceClickListener(this);
				
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (preference.equals(mEnabled)) {
			Context context = getBaseContext();
			Intent intent = new Intent(context, KeyguardService.class);
			if (mEnabled.isChecked()) {
				context.startService(intent);
			} else {
				context.stopService(intent);
			}
		}
		return false;
	}
}