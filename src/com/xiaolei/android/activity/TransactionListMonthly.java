/**
 * 
 */
package com.xiaolei.android.activity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.xiaolei.android.BizTracker.MonthlyTransactionListPagerAdapter;
import com.xiaolei.android.BizTracker.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.TextView;

/**
 * @author xiaolei
 * 
 */
public class TransactionListMonthly extends Activity implements
		AdapterView.OnItemClickListener, OnPageChangeListener {

	private static final int REQUEST_CODE = TransactionListMonthly.class
			.getName().hashCode();
	private Date month;
	private Date startDayOfYear;
	private MonthlyTransactionListPagerAdapter pagerAdapter;
	private int currentPosition = 0;
	private TextView tvTitle;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.pager_monthly_transaction_list);

		month = new Date();
		try {
			month = (Date) this.getIntent().getExtras().get("date");
		} catch (Exception ex) {
		}
		startDayOfYear = new Date(month.getYear(), Calendar.JANUARY, 1);
		refreshTitle(month);

		ViewPager viewPager = (ViewPager) findViewById(R.id.viewPaperMonthlyTransactionList);
		if (viewPager != null) {
			viewPager.setOnPageChangeListener(this);
			pagerAdapter = new MonthlyTransactionListPagerAdapter(this, month);
			pagerAdapter.setOnItemClickListener(this);
			viewPager.setAdapter(pagerAdapter);
			viewPager.setCurrentItem(month.getMonth());
		}
	}

	private void refreshTitle(Date date) {
		tvTitle = (TextView) findViewById(R.id.textViewTitle);
		Date now = new Date();
		if (date.getYear() == now.getYear()) {
			if (date.getMonth() != now.getMonth()) {
				SimpleDateFormat format = new SimpleDateFormat("MMMM");
				tvTitle.setText(format.format(date));
			} else {
				tvTitle.setText(getString(R.string.this_month));
			}
		} else {
			tvTitle.setText(date.getYear() + 1900 + getString(R.string.year));
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(startDayOfYear);
		cal.add(Calendar.DAY_OF_MONTH, position);

		Intent intent = new Intent(this, PagerDailyTransactionList.class);
		intent.putExtra(PagerDailyTransactionList.KEY_DATE, cal.getTime());
		this.startActivityForResult(intent, TransactionListMonthly.REQUEST_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == TransactionListMonthly.REQUEST_CODE
				&& resultCode == RESULT_OK) {
			setResult(RESULT_OK, new Intent());
			if (pagerAdapter != null) {
				pagerAdapter.reloadView(currentPosition);
			}
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageSelected(int position) {
		currentPosition = position;

		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(startDayOfYear);
		cal.add(Calendar.MONTH, position);

		refreshTitle(cal.getTime());
	}
}
