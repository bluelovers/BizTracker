/**
 * 
 */
package com.xiaolei.android.activity;

import java.util.Date;

import com.xiaolei.android.BizTracker.MonthsOfYearAdapter;
import com.xiaolei.android.BizTracker.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

/**
 * @author xiaolei
 * 
 */
public class TransactionListYearly extends Activity implements OnItemClickListener {

	private MonthsOfYearAdapter listAdapter;
	private ListView lv;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.days_of_month);

		TextView tvTitle = (TextView) findViewById(R.id.textViewTitle);
		tvTitle.setText(getString(R.string.this_year));

		lv = (ListView) findViewById(R.id.listViewDaysOfMonth);
		lv.setOnItemClickListener(this);
		
		fillData();
	}

	private void fillData() {
		Date now = new Date();
		
		listAdapter = new MonthsOfYearAdapter(this, now);
		lv.setAdapter(listAdapter);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position,
			long id) {
		Date date = (Date) listAdapter.getItem(position);
		Intent intentDaysOfMonth = new Intent(this, TransactionListMonthly.class);
		intentDaysOfMonth.putExtra("date", date);
		this.startActivityForResult(intentDaysOfMonth, 0);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == 0 && resultCode == RESULT_OK) {
			setResult(RESULT_OK, new Intent());
			fillData();
		}
	}
}
