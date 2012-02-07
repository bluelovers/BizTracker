package com.xiaolei.android.ui;

import java.util.Date;
import java.util.GregorianCalendar;

import android.R.drawable;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.ViewSwitcher;

import com.xiaolei.android.BizTracker.FunctionTypesAdapter;
import com.xiaolei.android.BizTracker.R;
import com.xiaolei.android.common.Utility;

public class FunctionTypes extends Activity implements
		AdapterView.OnItemClickListener, OnEditorActionListener {

	private FunctionTypes context;
	private FunctionTypesAdapter listAdapter;
	private ListView lv;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		context = this;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.function_types);

		lv = (ListView) findViewById(R.id.listViewFunctionTypes);
		lv.setOnItemClickListener(this);

		EditText txtKeyword = (EditText) findViewById(R.id.editTextKeyword);
		txtKeyword.setOnEditorActionListener(this);

		fillData();
	}

	private void fillData() {
		listAdapter = new FunctionTypesAdapter(this);
		lv.setAdapter(listAdapter);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int index, long arg3) {
		switch (index) {
		case 0:
			// Intent intent = new Intent(this, DayLog.class);
			Intent intent = new Intent(this, PagerDailyTransactionList.class);
			intent.putExtra("date", new Date());
			this.startActivityForResult(intent, 0);

			break;
		case 1:
			Intent intentWeekDays = new Intent(this, TransactionListWeekly.class);
			this.startActivityForResult(intentWeekDays, 0);
			break;
		case 2:
			Intent intentDaysOfMonth = new Intent(this, TransactionListMonthly.class);
			intentDaysOfMonth.putExtra("date", new Date());
			this.startActivityForResult(intentDaysOfMonth, 0);
			break;
		case 3:
			Intent intentYear = new Intent(this, TransactionListYearly.class);
			this.startActivityForResult(intentYear, 0);
			break;
		case 4:
			showSearchChooser();
			break;
		case 5:
			showStarredBizLog();
			break;
		case 6:
			Intent intentProjectManager = new Intent(this, ProjectManager.class);
			this.startActivity(intentProjectManager);
			break;
		default:
			break;
		}
	}

	private void showStarredBizLog() {
		Intent intent = new Intent(this, TransactionListDaily.class);
		intent.putExtra(TransactionListDaily.KEY_TITLE, getString(R.string.starred_biz_log));
		intent.putExtra(TransactionListDaily.KEY_SHOW_FULL_DATE, true);
		intent.putExtra(TransactionListDaily.KEY_SHOW_STARRED_RECORDS, true);
		this.startActivityForResult(intent, 0);
	}

	private void showSearchChooser() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(drawable.ic_dialog_info);
		builder.setTitle(R.string.choose_search_type);
		builder.setItems(
				new String[] { context.getString(R.string.search_by_date),
						context.getString(R.string.search_by_keyword) },
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0:
							showDatePicker();
							break;
						case 1:
							showSeachView();
							break;
						}
					}
				});

		builder.show();
	}
	
	private void showSeachView(){
		Intent intent = new Intent(this, TransactionSearchResult.class);
		//intent.putExtra(PagerDayLog.KEY_TITLE, getString(R.string.search));
		//intent.putExtra(PagerDayLog.KEY_SHOW_FULL_DATE, true);
		//intent.putExtra(PagerDayLog.KEY_SEARCH_MODE, true);
		this.startActivityForResult(intent, TransactionSearchResult.REQUEST_CODE);
	}

	private void showDatePicker() {
		Utility.showDialog(this, R.layout.go_to_date,
				getString(R.string.biz_date),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						AlertDialog alertDialog = (AlertDialog) dialog;
						DatePicker datePicker = (DatePicker) alertDialog
								.findViewById(R.id.datePickerGoToDate);
						datePicker.clearFocus();

						GregorianCalendar cal = new GregorianCalendar(
								datePicker.getYear(), datePicker.getMonth(),
								datePicker.getDayOfMonth());

						Date date = cal.getTime();
						Intent intent = new Intent(context, PagerDailyTransactionList.class);
						intent.putExtra(PagerDailyTransactionList.KEY_DATE, date);
						context.startActivityForResult(intent,
								PagerDailyTransactionList.REQUEST_CODE);

						dialog.dismiss();
					}
				}, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			fillData();
		}
	}

	@Override
	public boolean onSearchRequested() {
		this.showSeachView();

		return true;
	}

	@Override
	public boolean onEditorAction(TextView textView, int arg1, KeyEvent evt) {
		if (evt.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
			ViewSwitcher v = (ViewSwitcher) findViewById(R.id.viewSwitcherTitleBar);
			v.setDisplayedChild(0);

			String searchKeyword = textView.getText().toString();

			Intent intent = new Intent(this, TransactionListDaily.class);
			intent.putExtra("title", searchKeyword);
			intent.putExtra("searchKeyword", searchKeyword);
			this.startActivityForResult(intent, 0);

			EditText etKeyword = (EditText) findViewById(R.id.editTextKeyword);
			etKeyword.setText("");

			return true;
		}
		return false;
	}
}
