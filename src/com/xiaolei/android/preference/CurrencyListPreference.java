/**
 * 
 */
package com.xiaolei.android.preference;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONException;

import android.content.Context;
import android.database.Cursor;
import android.preference.ListPreference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.Toast;

import com.xiaolei.android.BizTracker.R;
import com.xiaolei.android.common.CurrencyNamesHelper;
import com.xiaolei.android.entity.CurrencySchema;
import com.xiaolei.android.service.DataService;

/**
 * @author xiaolei
 * 
 */
public class CurrencyListPreference extends ListPreference {

	public CurrencyListPreference(Context context) {
		super(context);
		prepareDataSource();
	}

	public CurrencyListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		prepareDataSource();
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);

		if (positiveResult) {
			refreshTitle();
		}
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		super.onSetInitialValue(restoreValue, defaultValue);
		refreshTitle();
	}

	private void prepareDataSource() {
		refreshTitle();

		CurrencyNamesHelper currencyNamesHelper = null;
		try {
			currencyNamesHelper = CurrencyNamesHelper.getInstance(getContext());
		} catch (IOException e) {
			Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		} catch (JSONException e) {
			Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}

		Cursor cursor = DataService.GetInstance(getContext())
				.getAllExchangeRate();
		if (cursor == null || cursor.getCount() == 0) {
			return;
		}

		if (cursor.moveToFirst()) {
			ArrayList<CharSequence> codes = new ArrayList<CharSequence>();
			ArrayList<CharSequence> names = new ArrayList<CharSequence>();
			do {
				String code = cursor.getString(cursor
						.getColumnIndex(CurrencySchema.Code));
				String name = cursor.getString(cursor
						.getColumnIndex(CurrencySchema.Name));
				if (currencyNamesHelper != null) {
					name = currencyNamesHelper.getLocalizedCurrencyName(code,
							name);
				}
				codes.add(code);
				names.add(String.format("%s (%s)", name, code));
			} while (cursor.moveToNext());

			CharSequence[] entries = new CharSequence[names.size()];
			CharSequence[] entryValues = new CharSequence[codes.size()];
			codes.toArray(entryValues);
			names.toArray(entries);

			this.setEntries(entries);
			this.setEntryValues(entryValues);

			cursor.close();
			cursor = null;
		}
	}

	private void refreshTitle() {
		String currencyCode = this.getPersistedString("");
		if (!TextUtils.isEmpty(currencyCode)) {
			setTitle(getContext().getString(R.string.default_currency) + "("
					+ currencyCode + ")");
		}
	}
}
