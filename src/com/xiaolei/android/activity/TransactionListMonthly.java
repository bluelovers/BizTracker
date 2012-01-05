/**
 * 
 */
package com.xiaolei.android.activity;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.xiaolei.android.BizTracker.DaysOfMonthAdapter;
import com.xiaolei.android.BizTracker.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * @author xiaolei
 * 
 */
public class TransactionListMonthly extends Activity implements
		AdapterView.OnItemClickListener {

	private ListView lv;
	private Date month;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.days_of_month);

		month = new Date();
		try {
			month = (Date) this.getIntent().getExtras().get("date");
		} catch (Exception ex) {
		}

		TextView tvTitle = (TextView) findViewById(R.id.textViewTitle);
		Date now = new Date();
		if (month.getYear() == now.getYear()) {
			if (month.getMonth() != now.getMonth()) {
				SimpleDateFormat format = new SimpleDateFormat("MMMM");
				tvTitle.setText(format.format(month));
			} else {
				tvTitle.setText(getString(R.string.this_month));
			}
		} else {
			tvTitle.setText(month.getYear() + 1900 + getString(R.string.year));
		}

		lv = (ListView) findViewById(R.id.listViewDaysOfMonth);
		lv.setOnItemClickListener(this);

		fillData();
	}

	private void fillData() {
		DaysOfMonthAdapter listAdapter = new DaysOfMonthAdapter(this, month);
		lv.setAdapter(listAdapter);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		Date date = (Date) lv.getItemAtPosition(position);
		Intent intent = new Intent(this, PagerDailyTransactionList.class);
		intent.putExtra(PagerDailyTransactionList.KEY_DATE, date);
		this.startActivityForResult(intent,
				PagerDailyTransactionList.REQUEST_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == PagerDailyTransactionList.REQUEST_CODE
				&& resultCode == RESULT_OK) {
			setResult(RESULT_OK, new Intent());
			fillData();
		}
	}
}
