/**
 * 
 */
package com.xiaolei.android.ui;

import java.util.Calendar;
import java.util.Date;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextSwitcher;

import com.xiaolei.android.BizTracker.BizTracker;
import com.xiaolei.android.BizTracker.DailyTransactionListFragmentPagerAdapter;
import com.xiaolei.android.BizTracker.R;
import com.xiaolei.android.common.Utility;
import com.xiaolei.android.listener.OnNotifyDataChangedListener;

/**
 * @author xiaolei
 * 
 */
public class DailyTransactionList extends FragmentActivity implements
		OnPageChangeListener, OnClickListener, OnNotifyDataChangedListener {

	private Date date = new Date();
	private Date currentDate = new Date();
	private Date minDate = new Date();
	private String title = "";
	private int pageCount = 365;
	private int currentPosition = 0;
	private ViewPager viewPager;
	private DailyTransactionListFragmentPagerAdapter adapter;

	public static final String KEY_DATE = "date";
	public static final String KEY_SHOW_FULL_DATE = "showFullDateTime";
	public static final String KEY_TITLE = "title";
	public static final String KEY_SHOW_STARRED_RECORDS = "getStarredBizLog";
	public static final String KEY_PAGE_COUNT = "pageCount";
	public static final int REQUEST_CODE = 2;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.pager_daily_log);

		try {
			Intent intent = this.getIntent();
			if (intent != null) {
				Bundle extras = intent.getExtras();
				if (extras != null) {
					if (extras.containsKey(KEY_DATE)) {
						date = (Date) extras.get(KEY_DATE);
						currentDate = date;
					}

					if (extras.containsKey(KEY_TITLE)) {
						title = extras.getString(KEY_TITLE);
					}

					if (extras.containsKey(KEY_PAGE_COUNT)) {
						pageCount = extras.getInt(KEY_PAGE_COUNT);
						if (pageCount <= 0) {
							pageCount = 1;
						}
					}
				}
			}
		} catch (Exception ex) {
		}

		if (TextUtils.isEmpty(title)) {
			String newTitle = DateUtils.formatDateTime(this, date.getTime(),
					DateUtils.FORMAT_SHOW_DATE);
			setTitle(newTitle);
		}

		ImageButton btnNewTransaction = (ImageButton) findViewById(R.id.imageButtonNewTransaction);
		if (btnNewTransaction != null) {
			btnNewTransaction.setOnClickListener(this);
		}

		viewPager = (ViewPager) findViewById(R.id.viewPaperDailyLog);
		viewPager.setOnPageChangeListener(this);

		minDate = Utility.addDays(date, -pageCount + 1);
		adapter = new DailyTransactionListFragmentPagerAdapter(
				this.getSupportFragmentManager(), minDate);
		adapter.setOnNotifyDataChangedListener(this);
		adapter.setPageCount(pageCount);

		viewPager.setAdapter(adapter);
		viewPager.setCurrentItem(pageCount - 1);
	}

	private void setTitle(String title) {
		TextSwitcher textSwitcher = (TextSwitcher) findViewById(R.id.textSwitcherCaption);
		if (textSwitcher != null) {
			textSwitcher.setText(title);
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
		currentDate = Utility.addDays(minDate, position);
		String newTitle = DateUtils.formatDateTime(this, currentDate.getTime(),
				DateUtils.FORMAT_SHOW_DATE);
		setTitle(newTitle);
	}

	private void newTransaction() {
		Intent intent = new Intent(this, BizTracker.class);

		if (currentDate != null) {
			Date now = new Date();
			Calendar cal = Calendar.getInstance();
			cal.setTime(currentDate);

			// Use current time
			cal.set(Calendar.HOUR_OF_DAY, now.getHours());
			cal.set(Calendar.MINUTE, now.getMinutes());
			cal.set(Calendar.SECOND, now.getSeconds());
			date = cal.getTime();

			intent.putExtra(BizTracker.KEY_UPDATE_DATE, date);
			this.startActivityForResult(intent, BizTracker.REQUEST_CODE);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case BizTracker.REQUEST_CODE:
			case DailyTransactionList.REQUEST_CODE:
				reload();
				this.setResult(RESULT_OK);
				break;
			}
		}
	}

	private void reload() {
		if (adapter != null) {
			TransactionListFragment fragment = (TransactionListFragment) adapter
					.getFragmentAtPosition(currentPosition);
			if (fragment != null) {
				fragment.reload();
			}
		}
	}

	@Override
	public void onClick(View v) {
		if (v == null) {
			return;
		}
		switch (v.getId()) {
		case R.id.imageButtonNewTransaction:
			newTransaction();
			break;
		default:
			break;
		}
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onNotifyDataChanged(Object sender) {
		this.setResult(RESULT_OK);
	}
}
