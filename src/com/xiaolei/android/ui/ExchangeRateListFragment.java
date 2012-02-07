package com.xiaolei.android.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class ExchangeRateListFragment extends Fragment implements
		OnItemClickListener {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		this.fillDataAsync();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View result = inflater.inflate(R.layout.exchange_rate_list_fragment, container);
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
		ViewSwitcher viewSwitcher = (ViewSwitcher) getView().findViewById(
				R.id.viewSwitcherMain);
		viewSwitcher.setDisplayedChild(0);

		AsyncTask<Boolean, Void, Cursor> task = new AsyncTask<Boolean, Void, Cursor>() {

			@Override
			protected Cursor doInBackground(Boolean... params) {
				Cursor cursor = DataService.GetInstance(getActivity())
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
		ListView lv = (ListView) getView().findViewById(R.id.listViewData);
		if (lv.getAdapter() == null) {
			CurrencyAdapter adpt = new CurrencyAdapter(getActivity(), cursor);
			lv.setAdapter(adpt);
		} else {
			CurrencyAdapter adapter = (CurrencyAdapter) lv.getAdapter();
			adapter.changeCursor(cursor);
		}

		ViewSwitcher viewSwitcher = (ViewSwitcher) getView().findViewById(
				R.id.viewSwitcherMain);
		viewSwitcher.setDisplayedChild(1);
	}
}
