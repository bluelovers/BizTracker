package com.xiaolei.android.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.xiaolei.android.BizTracker.CurrencyAdapter;
import com.xiaolei.android.BizTracker.R;
import com.xiaolei.android.common.Utility;
import com.xiaolei.android.entity.CurrencySchema;
import com.xiaolei.android.service.DataService;

public class ExchangeRateListFragment extends Fragment implements
		OnItemClickListener, OnClickListener {

	private String updateAPIUrlTemplate = "http://openexchangerates.org/latest.json";
	private String errorMessage = "";

	private String PROPERTY_timestamp = "timestamp";
	private String PROPERTY_base = "base";
	private String PROPERTY_rates = "rates";
	private String USD = "USD";

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		this.fillDataAsync();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View result = inflater.inflate(R.layout.exchange_rate_list_fragment,
				container);
		if (result != null) {
			ListView lv = (ListView) result.findViewById(R.id.listViewData);
			lv.setOnItemClickListener(this);
		}

		return result;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

	}

	@Override
	public void onDestroyView() {

		super.onDestroyView();

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		TextView tvCurrencyName = (TextView) view
				.findViewById(R.id.textViewCurrencyName);
		String currencyName = tvCurrencyName.getText().toString();
		final long selectedExchangeRateId = id;

		AlertDialog dialog = Utility.showDialog(getActivity(),
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
										getActivity(),
										getString(R.string.exchange_rate_validation_error),
										Toast.LENGTH_SHORT).show();
							} else {
								DataService.GetInstance(getActivity())
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

		Utility.requestInputMethod(dialog);
	}

	private void fillDataAsync() {
		showWaitCursor();

		AsyncTask<Boolean, Void, Cursor> task = new AsyncTask<Boolean, Void, Cursor>() {

			@Override
			protected Cursor doInBackground(Boolean... params) {
				Cursor cursor = DataService.GetInstance(getActivity())
						.getAllExchangeRate();

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

	private void showWaitCursor() {
		ViewFlipper viewSwitcher = (ViewFlipper) getView().findViewById(
				R.id.viewFlipperMain);
		viewSwitcher.setDisplayedChild(0);
	}

	private void fillData(Cursor cursor) {
		ListView lv = (ListView) getView().findViewById(R.id.listViewData);
		if (lv.getAdapter() == null) {
			CurrencyAdapter adpt = new CurrencyAdapter(getActivity(), cursor);
			lv.setAdapter(adpt);
		} else {
			CurrencyAdapter adapter = (CurrencyAdapter) lv.getAdapter();
			adapter.changeCursor(cursor);
		}

		hideWaitCursor();
	}

	private void hideWaitCursor() {
		ViewFlipper viewSwitcher = (ViewFlipper) getView().findViewById(
				R.id.viewFlipperMain);
		viewSwitcher.setDisplayedChild(1);
	}

	private void hideWaitCursorAndShowError(String errorMessage) {
		ViewFlipper viewSwitcher = (ViewFlipper) getView().findViewById(
				R.id.viewFlipperMain);
		viewSwitcher.setDisplayedChild(2);
		TextView tv = (TextView) getView().findViewById(
				R.id.textViewUpdateExchangeRateListError);
		if (tv != null) {
			String text = getActivity().getString(
					R.string.fail_to_online_update_exchange_rate);
			if (!TextUtils.isEmpty(errorMessage)) {
				text = text + "\n" + errorMessage;
			}
			tv.setText(text);
		}
	}

	public void updateAllExchangeRateOnlineAsync() {

		AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(Void... params) {
				boolean success = false;

				StringBuilder jsonText = new StringBuilder();
				HttpClient client = new DefaultHttpClient();
				String url = updateAPIUrlTemplate;
				HttpGet httpGet = new HttpGet(url);
				try {
					HttpResponse response = client.execute(httpGet);
					StatusLine statusLine = response.getStatusLine();
					int statusCode = statusLine.getStatusCode();
					if (statusCode == 200) {
						HttpEntity entity = response.getEntity();
						InputStream content = entity.getContent();
						BufferedReader reader = new BufferedReader(
								new InputStreamReader(content));
						String line;
						while ((line = reader.readLine()) != null) {
							jsonText.append(line);
						}

						JSONObject result = new JSONObject(jsonText.toString());

						String timestamp = result.getString(PROPERTY_timestamp);
						String base = result.getString(PROPERTY_base);
						String ratesString = result.getString(PROPERTY_rates);
						Date updateTime = new Date(
								Long.parseLong(timestamp) * 1000);

						if (USD.equalsIgnoreCase(base)
								&& !TextUtils.isEmpty(ratesString)) {
							JSONObject rates = new JSONObject(ratesString);
							if (rates != null) {
								Cursor cursor = DataService.GetInstance(
										getActivity()).getAllExchangeRate();
								if (cursor != null) {

									try {
										if (cursor.moveToFirst()) {
											do {
												String currencyCode = cursor
														.getString(cursor
																.getColumnIndex(CurrencySchema.Code));
												if (!TextUtils
														.isEmpty(currencyCode)
														&& rates.has(currencyCode)) {
													double newExchangeRate = rates
															.getDouble(currencyCode);
													DataService
															.GetInstance(
																	getActivity())
															.updateExchangeRateByCurrencyCode(
																	currencyCode,
																	newExchangeRate,
																	updateTime);
													Log.i("Update Exchange Rate",
															String.format(
																	"%s: %f",
																	currencyCode,
																	newExchangeRate));
												}

											} while (cursor.moveToNext());
										}
									} finally {
										cursor.close();
										cursor = null;
									}
								}

								success = true;
							}
						}
					}
				} catch (ClientProtocolException e) {
					errorMessage = e.getMessage();
				} catch (IOException e) {
					errorMessage = e.getMessage();
				} catch (JSONException e) {
					errorMessage = e.getMessage();
				} catch (Exception e) {
					errorMessage = e.getMessage();
				}

				return success;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				hideWaitCursor();
				if (result == true) {
					fillDataAsync();
				} else {
					hideWaitCursorAndShowError(errorMessage);
				}
			}
		};

		errorMessage = "";
		showWaitCursor();
		task.execute();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.buttonUpdateExchangeRateListError:
			updateAllExchangeRateOnlineAsync();
			break;
		default:
			break;
		}
	}
}
