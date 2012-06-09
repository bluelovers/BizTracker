package com.xiaolei.android.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.xiaolei.android.BizTracker.R;

public final class CurrencyNamesHelper {
	private static CurrencyNamesHelper mInstance;
	private Hashtable<String, String> mNames;

	private static Context mContext;

	private CurrencyNamesHelper() throws IOException, JSONException {
		mNames = new Hashtable<String, String>();
		String UTF8 = "utf8"; 
		InputStream fileStream = mContext.getResources().openRawResource(
				R.raw.currency_names);
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				fileStream, UTF8));

		StringBuilder jsonResult = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			jsonResult.append(line);
		}
		fileStream.close();
		reader.close();

		String currentLanguageCode = Utility.getCurrentLanguageCode();
		String jsonText = jsonResult.toString();
		
		if(!jsonText.substring(0, 1).equals('[')){
			jsonText = jsonText.substring(1);
		}
		JSONArray languages = new JSONArray(jsonText);
		if (languages != null && languages.length() > 0) {
			for (int i = 0; i < languages.length(); i++) {
				JSONObject item = languages.getJSONObject(i);
				if (item != null && item.has("language_code")) {
					String languageCode = item.getString("language_code");
					if (languageCode.equalsIgnoreCase(currentLanguageCode)) {
						JSONObject currencyNames = item
								.getJSONObject("currency_names");
						if (currencyNames != null) {
							Iterator<?> keys = currencyNames.keys();
							while (keys.hasNext()) {
								String key = keys.next().toString();
								mNames.put(key, currencyNames.getString(key));
							}
						}

						break;
					}
				}
			}
		}
	}

	public static CurrencyNamesHelper getInstance(Context context)
			throws IOException, JSONException {
		if (mInstance == null) {
			mContext = context;
			mInstance = new CurrencyNamesHelper();

		}
		return mInstance;
	}

	/**
	 * Get the currency name of the current device language by currency code. If
	 * no mapped name, returns the defaultValue.
	 * 
	 * @param currencyCode
	 * @param defaultValue
	 * @return
	 */
	public String getLocalizedCurrencyName(String currencyCode,
			String defaultValue) {
		String result = defaultValue;
		if (mNames != null && !TextUtils.isEmpty(currencyCode)
				&& mNames.containsKey(currencyCode)) {
			result = mNames.get(currencyCode);
		}

		return result;
	}
}
