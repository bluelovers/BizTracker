/**
 * 
 */
package com.xiaolei.android.ui;

import java.util.Date;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.Window;
import android.widget.TextView;
import com.xiaolei.android.BizTracker.R;
import com.xiaolei.android.common.Utility;

/**
 * @author xiaolei
 * 
 */
public class TransactionList extends FragmentActivity {

	private TransactionList context;
	private Date date;
	private String searchKeyword = "";
	private String title = "";
	@SuppressWarnings("unused")
	private boolean showFullDateTime = false;
	private boolean getStarredBizLog = false;

	private Date startDate;
	private Date endDate;

	public static final String KEY_DATE = "date";
	public static final String KEY_SHOW_FULL_DATE = "showFullDateTime";
	public static final String KEY_SEARCH_KEYWORD = "searchKeyword";
	public static final String KEY_TITLE = "title";
	public static final String KEY_SHOW_STARRED_RECORDS = "getStarredBizLog";

	public static final String KEY_START_DATE = "StartDate";
	public static final String KEY_END_DATE = "EndDate";
	protected static final int REQUEST_CODE = 1223;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		context = this;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.daily_transaction_list);

		date = new Date();
		Date now = new Date();
		try {
			Intent intent = this.getIntent();
			if (intent != null) {
				Bundle extras = intent.getExtras();
				if (extras != null) {
					if (extras.containsKey(KEY_DATE)) {
						date = (Date) extras.get(KEY_DATE);
					}

					if (extras.containsKey(KEY_START_DATE)) {
						startDate = (Date) extras.get(KEY_START_DATE);
					}

					if (extras.containsKey(KEY_END_DATE)) {
						endDate = (Date) extras.get(KEY_END_DATE);
					}

					if (extras.containsKey(KEY_SHOW_FULL_DATE)) {
						showFullDateTime = extras
								.getBoolean(KEY_SHOW_FULL_DATE);
					}

					if (extras.containsKey(KEY_SEARCH_KEYWORD)) {
						searchKeyword = extras.getString(KEY_SEARCH_KEYWORD);
					}

					if (extras.containsKey(KEY_TITLE)) {
						title = extras.getString(KEY_TITLE);
					}

					if (extras.containsKey(KEY_SHOW_STARRED_RECORDS)) {
						getStarredBizLog = extras
								.getBoolean(KEY_SHOW_STARRED_RECORDS);
					}
				}
			}
		} catch (Exception ex) {
		}

		TextView tvTitle = (TextView) findViewById(R.id.textViewTitle);
		if (TextUtils.isEmpty(title)) {
			if (date.getYear() == now.getYear()
					&& date.getMonth() == now.getMonth()
					&& date.getDay() == now.getDay()) {
				tvTitle.setText(getString(R.string.today_biz));
			} else {
				tvTitle.setText(Utility.toLocalDateString(context, date));
			}
		} else {
			tvTitle.setText(title);
		}

		FragmentManager fragMan = this.getSupportFragmentManager();
		if (fragMan != null) {
			TransactionListFragment fragment = (TransactionListFragment) fragMan
					.findFragmentById(R.id.fragmentDailyTransactionList);
			if (fragment != null) {
				if (!TextUtils.isEmpty(searchKeyword)) {
					fragment.search(searchKeyword);
				} else if (getStarredBizLog) {
					fragment.showFavouriteTransactionList();
				} else if (startDate != null && endDate != null) {
					fragment.showDateRangeTransactionList(startDate, endDate);
				} else {
					fragment.showTransactionListByDate(date);
				}
			}
		}
	}
}
