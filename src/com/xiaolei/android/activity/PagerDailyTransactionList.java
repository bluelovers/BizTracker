/**
 * 
 */
package com.xiaolei.android.activity;

import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.TextSwitcher;

import com.xiaolei.android.BizTracker.BizTracker;
import com.xiaolei.android.BizTracker.DailyTransactionListPagerAdapter;
import com.xiaolei.android.BizTracker.R;
import com.xiaolei.android.common.Utility;
import com.xiaolei.android.listener.OnCostValueChangedListener;
import com.xiaolei.android.service.DataService;

/**
 * @author xiaolei
 * 
 */
public class PagerDailyTransactionList extends Activity implements OnPageChangeListener,
		OnCostValueChangedListener, OnItemClickListener, OnClickListener {

	private Context context;
	private Date date = new Date();
	private Date minDate = new Date();
	private Date currentDate = new Date();
	private String title = "";
	private int pageCount = 365;
	private ViewPager viewPager;
	private int currentPosition = -1;
	private DailyTransactionListPagerAdapter adapter;
	// private TransactionListPagerAdapter adapter;

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

		context = this;

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

		minDate = Utility.addDays(date, -pageCount);
		adapter = new DailyTransactionListPagerAdapter(this, minDate, pageCount);
		// adapter = new TransactionListPagerAdapter(this, minDate, pageCount);
		adapter.addCostValueChangedListener(this);
		adapter.setOnItemClickListener(this);

		viewPager.setAdapter(adapter);

		viewPager.setCurrentItem(adapter.getCount() - 1);
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
		currentDate = Utility.addDays(minDate, position + 1);
		String newTitle = DateUtils.formatDateTime(this, currentDate.getTime(),
				DateUtils.FORMAT_SHOW_DATE);
		setTitle(newTitle);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_for_biz_log, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.itemAppendLog:
			newTransaction();

			return true;
		case R.id.itemRemoveTodayLog:
			removeTransactions();

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void removeTransactions() {
		Utility.showConfirmDialog(this, getString(R.string.reset_today_cost),
				String.format(getString(R.string.confirm_reset_today_cost),
						DateUtils.formatDateTime(context,
								currentDate.getTime(),
								DateUtils.FORMAT_SHOW_DATE)),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						DataService.GetInstance(context).resetHistoryByDate(
								currentDate);

					}
				});
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
			case PagerDailyTransactionList.REQUEST_CODE:
				if (adapter != null) {
					adapter.reloadView(currentPosition);
				}

				this.setResult(RESULT_OK);
				break;
			}
		}
	}

	@Override
	public void OnCostValueChanged() {
		this.setResult(RESULT_OK);
	}

	@SuppressWarnings("unused")
	private void openPhotoGallery(long bizLogId) {
		Intent intent = new Intent();
		intent.setClass(this, PhotoViewer.class);
		intent.putExtra(PhotoViewer.TRANSACTION_ID, bizLogId);
		this.startActivityForResult(intent, REQUEST_CODE);
	}

	private void openTransactionDetails(long bizLogId) {
		Intent intent = new Intent();
		intent.setClass(this, TransactionDetails.class);
		intent.putExtra(TransactionDetails.TRANSACTION_ID, bizLogId);
		this.startActivityForResult(intent, REQUEST_CODE);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position,
			long id) {

		openTransactionDetails(id);
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
}
