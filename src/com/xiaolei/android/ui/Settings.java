package com.xiaolei.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.view.Window;
import android.widget.Toast;

import com.xiaolei.android.BizTracker.R;
import com.xiaolei.android.common.Utility;
import com.xiaolei.android.preference.PreferenceHelper;
import com.xiaolei.android.preference.PreferenceKeys;

public class Settings extends PreferenceActivity implements
		OnPreferenceClickListener {
	private String FEEDBACK_EMAIL = "xiaolei.android.feedback@gmail.com";
	public static int REQUEST_CODE = Settings.class.hashCode();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.config);

		String activeUserConfigFileName = PreferenceHelper
				.getActiveUserConfigFileName(this);
		PreferenceManager pm = this.getPreferenceManager();
		if(pm != null){
			pm.setSharedPreferencesName(activeUserConfigFileName);
		}

		this.addPreferencesFromResource(R.xml.config);

		init();
	}

	private void init() {
		Preference pref = this.findPreference(PreferenceKeys.ExchangeRate);
		if (pref != null) {
			pref.setOnPreferenceClickListener(this);
		}
		pref = this.findPreference(PreferenceKeys.Feedback);
		if (pref != null) {
			pref.setOnPreferenceClickListener(this);
		}
		pref = this.findPreference(PreferenceKeys.LocateInMarket);
		if (pref != null) {
			pref.setOnPreferenceClickListener(this);
		}

		PreferenceHelper.migrateOldPreferencesIfNeed(this);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (PreferenceKeys.ExchangeRate.equals(preference.getKey())) {
			showExchangeRateView();
		} else if (PreferenceKeys.Feedback.equals(preference.getKey())) {
			sendFeedback();
		} else if (PreferenceKeys.LocateInMarket.equals(preference.getKey())) {
			gotoAndroidMarket();
		}
		return true;
	}

	private void gotoAndroidMarket() {
		Utility.goToAndroidMarket(this);
	}

	private void showExchangeRateView() {
		Intent intent = new Intent(this, CurrencySettings.class);
		this.startActivityForResult(intent, 0);
	}

	private void sendFeedback() {
		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		emailIntent.setType("message/rfc822");
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
				new String[] { FEEDBACK_EMAIL });
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
				getString(R.string.feedback));
		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "");

		// this.startActivity(Intent.createChooser(emailIntent,
		// getString(R.string.feedback)));
		try {
			this.startActivity(emailIntent);
		} catch (Exception ex) {
			Toast.makeText(this,
					getString(R.string.no_email_send_app_installed),
					Toast.LENGTH_SHORT).show();
		}
	}
}
