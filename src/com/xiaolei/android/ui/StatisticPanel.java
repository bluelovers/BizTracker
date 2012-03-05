/**
 * 
 */
package com.xiaolei.android.ui;

import java.text.ParseException;
import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;

import com.xiaolei.android.BizTracker.R;
import com.xiaolei.android.entity.BizLogSchema;
import com.xiaolei.android.service.DataService;

/**
 * @author x
 * 
 */
public class StatisticPanel extends FragmentActivity {

	public static final String KEY_DATE = "Date";
	public static final String KEY_START_DATE = "StartDate";
	public static final String KEY_END_DATE = "EndDate";
	public static final String KEY_SEARCH_KEYWORD = "SearchKeyword";
	public static final String KEY_SHOW_FAVOURITE_LIST = "ShowFavouriteList";

	private Context mContext;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.statistic_panel);
		mContext = this;

		Bundle params = this.getIntent().getExtras();
		if (params != null) {
			if (params.containsKey(KEY_SHOW_FAVOURITE_LIST)) {
				boolean showFavList = params.getBoolean(
						KEY_SHOW_FAVOURITE_LIST, false);
				if (showFavList) {
					showFavouriteTransactionListChartAsync();
					showFavouriteTransactionListSummaryAsync();
				}
			}
			if (params.containsKey(KEY_DATE)) {
				long millionSeconds = params.getLong(KEY_DATE, -1);
				if (millionSeconds > 0) {
					Date date = new Date(millionSeconds);
					showDailyTransactionListChartAsync(date);
				}
			}
			if (params.containsKey(KEY_SEARCH_KEYWORD)) {
				String keyword = params.getString(KEY_SEARCH_KEYWORD);
				if (!TextUtils.isEmpty(keyword)) {
					showSearchResultChartAsync(keyword);
				}
			}
			if (params.containsKey(KEY_START_DATE)
					&& params.containsKey(KEY_END_DATE)) {
				long startDateMilliseconds = params.getLong(KEY_START_DATE, -1);
				long endDateMilliseconds = params.getLong(KEY_END_DATE, -1);

				if (startDateMilliseconds > 0 && endDateMilliseconds > 0) {
					Date startDate = new Date(startDateMilliseconds);
					Date endDate = new Date(endDateMilliseconds);

					showDateRangeTransactionListChart(startDate, endDate);
				}
			}
		}

		showEmptyChartMessage();
	}

	private void showFavouriteTransactionListSummaryAsync() {
		FragmentManager fragMan = getSupportFragmentManager();
		if (fragMan != null) {
			StatisticPanalFragment fragment = (StatisticPanalFragment) fragMan
					.findFragmentById(R.id.fragmentStatisticPanel);
			fragment.showFavouriteTransactionListSummaryDataAsync();
		}
	}

	private void showSearchResultChartAsync(String keyword) {
		showBusyIndicator();
		AsyncTask<String, Void, Cursor> task = new AsyncTask<String, Void, Cursor>() {

			@Override
			protected Cursor doInBackground(String... params) {
				Cursor cursor = DataService.GetInstance(mContext).searchBizLog(
						params[0]);

				return cursor;
			}

			@Override
			protected void onPostExecute(Cursor result) {
				buildLineChart(result);
			}
		};
		task.execute(keyword);
	}

	private void showDateRangeTransactionListChart(Date startDate, Date endDate) {
		showBusyIndicator();
		AsyncTask<Date, Void, Cursor> task = new AsyncTask<Date, Void, Cursor>() {

			@Override
			protected Cursor doInBackground(Date... dates) {
				Cursor result = null;
				if (dates != null && dates.length == 2) {
					Date startDate = dates[0];
					Date endDate = dates[1];
					result = DataService.GetInstance(mContext)
							.getTransactionListByDateRange(startDate, endDate);
				}

				return result;
			}

			@Override
			protected void onPostExecute(Cursor result) {
				buildLineChart(result);
			}
		};
		task.execute(startDate, endDate);
	}

	private void showDailyTransactionListChartAsync(Date date) {
		showBusyIndicator();
		AsyncTask<Date, Void, Cursor> task = new AsyncTask<Date, Void, Cursor>() {

			@Override
			protected Cursor doInBackground(Date... dates) {
				Cursor result = null;
				if (dates != null && dates.length > 0) {
					result = DataService.GetInstance(mContext).getBizLogByDay(
							dates[0]);
				}

				return result;
			}

			@Override
			protected void onPostExecute(Cursor result) {
				buildLineChart(result);
			}
		};
		task.execute(date);
	}

	private void showFavouriteTransactionListChartAsync() {
		showBusyIndicator();
		AsyncTask<Boolean, Void, Cursor> task = new AsyncTask<Boolean, Void, Cursor>() {

			@Override
			protected Cursor doInBackground(Boolean... params) {
				Cursor result = DataService.GetInstance(mContext)
						.getStarredBizLog();

				return result;
			}

			@Override
			protected void onPostExecute(Cursor result) {
				buildLineChart(result);
			}
		};
		task.execute();
	}

	private void showBusyIndicator() {
		FragmentManager fm = getSupportFragmentManager();
		if (fm != null) {
			LineChartFragment fragment = (LineChartFragment) fm
					.findFragmentById(R.id.fragmentChart);
			if (fragment != null) {
				fragment.showBusyIndicator();
			}
		}
	}

	private void showEmptyChartMessage() {
		FragmentManager fm = getSupportFragmentManager();
		if (fm != null) {
			LineChartFragment fragment = (LineChartFragment) fm
					.findFragmentById(R.id.fragmentChart);
			if (fragment != null) {
				fragment.showMessage(getString(R.string.no_data_for_chart));
			}
		}
	}

	private void buildLineChart(Cursor result) {
		if (result != null && !result.isClosed()) {
			FragmentManager fm = getSupportFragmentManager();
			if (fm != null) {
				LineChartFragment fragment = (LineChartFragment) fm
						.findFragmentById(R.id.fragmentChart);
				if (fragment != null) {
					try {
						try {
							fragment.addLines(result,
									getString(R.string.income),
									getString(R.string.expense),
									BizLogSchema.LastUpdateTime,
									BizLogSchema.Cost);
							fragment.showData();
						} finally {
							if (result != null && !result.isClosed()) {
								result.close();
								result = null;
							}
						}
					} catch (ParseException e) {
						fragment.showMessage(e.getMessage());
						e.printStackTrace();
					}
				}
			}
		}
	}
}
