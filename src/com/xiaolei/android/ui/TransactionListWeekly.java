/**
 * 
 */
package com.xiaolei.android.ui;

import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.xiaolei.android.BizTracker.DaysOfWeekAdapter;
import com.xiaolei.android.BizTracker.R;
import com.xiaolei.android.common.Utility;

/**
 * @author xiaolei
 * 
 */
public class TransactionListWeekly extends Activity implements OnItemClickListener {
	private ListView lv;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.week_days);

		TextView tvTitle = (TextView) findViewById(R.id.textViewTitle);
		tvTitle.setText(getString(R.string.this_week));

		lv = (ListView) findViewById(R.id.listViewWeekDays);
		lv.setOnItemClickListener(this);
		fillData();
	}

	private void fillData() {
		Date startDayOfThisWeek = Utility.getStartDayOfThisWeek();
		Date endDayOfThisWeek = Utility.addDays(startDayOfThisWeek, 6);
		DaysOfWeekAdapter listAdapter = new DaysOfWeekAdapter(this,
				startDayOfThisWeek, endDayOfThisWeek);
		lv.setAdapter(listAdapter);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		Date date = (Date) lv.getItemAtPosition(position);
		//Intent intent = new Intent(this, DayLog.class);
		Intent intent = new Intent(this, DailyTransactionList.class);
		intent.putExtra(DailyTransactionList.KEY_DATE, date);
		this.startActivityForResult(intent, DailyTransactionList.REQUEST_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == DailyTransactionList.REQUEST_CODE && resultCode == RESULT_OK) {
			setResult(RESULT_OK, new Intent());
			fillData();
		}
	}
}
