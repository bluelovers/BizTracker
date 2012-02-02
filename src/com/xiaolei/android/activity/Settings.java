package com.xiaolei.android.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import com.xiaolei.android.BizTracker.R;
import com.xiaolei.android.common.Utility;
import com.xiaolei.android.entity.Parameter;
import com.xiaolei.android.entity.ParameterUtils;
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
		
		migrateOldPreferences();
	}

	/**
	 * Migrate the old parameter values from sqlite DB to SharedPreference.
	 */
	private void migrateOldPreferences() {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);
		if (pref != null) {
			if (pref.getBoolean(PreferenceKeys.IsMigrated, false) == false) {
				try {
					// Migrate volume value
					int volume = ParameterUtils.getIntParameterValue(this,
							Parameter.VOLUME, -1);
					if (volume != -1) {
						Editor editor = pref.edit();
						if (editor != null) {
							editor.putInt(PreferenceKeys.Volume, volume);
							editor.commit();
						}
					}

					// Migrate default currency code
					String defaultCurrencyCode = ParameterUtils
							.getParameterValue(this,
									PreferenceKeys.DefaultCurrencyCode, "");
					if (!TextUtils.isEmpty(defaultCurrencyCode)) {
						Editor editor = pref.edit();
						if (editor != null) {
							editor.putString(
									PreferenceKeys.DefaultCurrencyCode,
									defaultCurrencyCode);
							editor.commit();
						}
					}

					// Do not migrate the password, because the encrypt method
					// is changed.
				} catch (Exception ex) {
					Log.e("BizTracker", ex.getMessage());
				} finally {
					Editor editor = pref.edit();
					if (editor != null) {
						editor.putBoolean(PreferenceKeys.IsMigrated, true);
						editor.commit();
					}
				}
			}
		}
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
