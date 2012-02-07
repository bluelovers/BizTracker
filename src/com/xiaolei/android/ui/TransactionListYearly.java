/**
 * 
 */
package com.xiaolei.android.ui;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.xiaolei.android.BizTracker.R;
import com.xiaolei.android.BizTracker.YearlyTransactionListAdapter;
import com.xiaolei.android.BizTracker.YearlyTransactionListPagerAdapter;

/**
 * @author xiaolei
 * 
 */
public class TransactionListYearly extends Activity implements
		OnItemClickListener, OnPageChangeListener {

	@SuppressWarnings("unused")
	private Date currentYear = new Date();
	private TextView tvTitle;
	private Date now = new Date();
	private Date startDate = new Date();
	private int MAX_PAGE_COUNT = 100;
	private int currentPosition = MAX_PAGE_COUNT - 1;
	private YearlyTransactionListPagerAdapter pagerAdapter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.pager_yearly_transaction_list);

		tvTitle = (TextView) findViewById(R.id.textViewTitle);
		tvTitle.setText(getString(R.string.this_year));

		ViewPager viewPager = (ViewPager) findViewById(R.id.viewPaperYearlyTransactionList);
		if (viewPager != null) {
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTime(now);
			cal.set(Calendar.MONTH, Calendar.JANUARY);
			cal.set(Calendar.DAY_OF_MONTH, 1);
			cal.add(Calendar.YEAR, -MAX_PAGE_COUNT + 1);
			startDate = cal.getTime();

			viewPager.setOnPageChangeListener(this);
			pagerAdapter = new YearlyTransactionListPagerAdapter(this, now,
					MAX_PAGE_COUNT);
			pagerAdapter.setOnItemClickListener(this);
			viewPager.setAdapter(pagerAdapter);
			viewPager.setCurrentItem(MAX_PAGE_COUNT - 1);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position,
			long id) {
		ListView lv = (ListView) arg0;
		if (lv != null) {
			YearlyTransactionListAdapter adpt = (YearlyTransactionListAdapter) lv
					.getAdapter();
			if (adpt != null) {
				Date selectedDate = (Date) adpt.getItem(position);
				if (selectedDate != null) {
					Intent intentDaysOfMonth = new Intent(this,
							TransactionListMonthly.class);
					intentDaysOfMonth.putExtra("date", selectedDate);
					this.startActivityForResult(intentDaysOfMonth, 0);
				}
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == 0 && resultCode == RESULT_OK) {
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
		cal.setTime(startDate);
		cal.add(Calendar.YEAR, position);
		currentYear = cal.getTime();

		if (tvTitle != null) {
			tvTitle.setText(cal.get(Calendar.YEAR) + getString(R.string.year));
		}
	}
}
