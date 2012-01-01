/**
 * 
 */
package com.xiaolei.android.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.xiaolei.android.BizTracker.CurrencyAdapter;
import com.xiaolei.android.BizTracker.R;
import com.xiaolei.android.common.Utility;
import com.xiaolei.android.service.DataService;

/**
 * @author xiaolei
 * 
 */
public class CurrencySettings extends Activity implements OnItemClickListener {
	private CurrencySettings context;
	private long selectedExchangeRateId = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		context = this;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.currency);

		ListView lv = (ListView) findViewById(R.id.listViewData);
		lv.setOnItemClickListener(this);

		fillDataAsync();
	}

	private void fillDataAsync() {
		ViewSwitcher viewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcherMain);
		viewSwitcher.setDisplayedChild(0);

		AsyncTask<Boolean, Void, Cursor> task = new AsyncTask<Boolean, Void, Cursor>() {

			@Override
			protected Cursor doInBackground(Boolean... params) {
				Cursor cursor = DataService.GetInstance(context)
						.getAllActiveCurrency();

				return cursor;
			}

			@Override
			protected void onPostExecute(Cursor result) {
				if (result != null) {
					fillData(result);
				}
			}
		};
		task.execute();
	}

	private void fillData(Cursor cursor) {
		ListView lv = (ListView) findViewById(R.id.listViewData);
		if (lv.getAdapter() == null) {
			CurrencyAdapter adpt = new CurrencyAdapter(this, cursor);
			lv.setAdapter(adpt);
		} else {
			CurrencyAdapter adapter = (CurrencyAdapter) lv.getAdapter();
			adapter.changeCursor(cursor);
		}

		ViewSwitcher viewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcherMain);
		viewSwitcher.setDisplayedChild(1);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int index, long id) {
		TextView tvCurrencyName = (TextView) view
				.findViewById(R.id.textViewCurrencyName);
		String currencyName = tvCurrencyName.getText().toString();
		selectedExchangeRateId = id;

		AlertDialog dialog = Utility.showDialog(this,
				R.layout.exchange_rate_editor,
				android.R.drawable.ic_dialog_info, currencyName,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						AlertDialog dlg = (AlertDialog) dialog;
						EditText txtExchangeRate = (EditText) dlg
								.findViewById(R.id.editTextExchangeRate);
						String text = txtExchangeRate.getText().toString();
						if (!txtExchangeRate.isEnabled()) {
							dlg.dismiss();
							return;
						}

						try {
							double exchangeRate = Double.parseDouble(text);
							if (exchangeRate <= 0) {
								Toast.makeText(
										context,
										context.getString(R.string.exchange_rate_validation_error),
										Toast.LENGTH_SHORT).show();
							} else {
								DataService.GetInstance(context)
										.updateExchangeRateById(
												selectedExchangeRateId,
												exchangeRate);
								fillDataAsync();
							}
						} catch (Exception ex) {
						}
					}
				}, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}, null);

		TextView tvExchangeRate = (TextView) view
				.findViewById(R.id.textViewExchangeRate);
		EditText txtExchangeRate = (EditText) dialog
				.findViewById(R.id.editTextExchangeRate);
		txtExchangeRate.setText(tvExchangeRate.getText().toString());
		if (currencyName.equalsIgnoreCase("US Dollar (USD)")) {
			txtExchangeRate.setEnabled(false);
		}
	}
}
