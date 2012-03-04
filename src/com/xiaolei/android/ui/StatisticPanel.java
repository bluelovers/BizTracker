/**
 * 
 */
package com.xiaolei.android.ui;

import java.text.ParseException;
import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.xiaolei.android.BizTracker.R;
import com.xiaolei.android.common.DataSourceType;
import com.xiaolei.android.entity.BizLogSchema;
import com.xiaolei.android.service.DataService;

/**
 * @author x
 * 
 */
public class StatisticPanel extends FragmentActivity {

	public static final String KEY_DATA_SOURCE_TYPE = "DataSourceType";
	private DataSourceType dataSourceType = DataSourceType.Unknown;
	private Context mContext;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.statistic_panel);
		mContext = this;

		Bundle params = this.getIntent().getExtras();
		if (params != null) {
			if (params.containsKey(KEY_DATA_SOURCE_TYPE)) {
				int enumValueIndex = params.getInt(KEY_DATA_SOURCE_TYPE,
						DataSourceType.Unknown.ordinal());
				if (enumValueIndex >= 0
						&& enumValueIndex < DataSourceType.values().length) {
					dataSourceType = DataSourceType.values()[enumValueIndex];
					loadDataAsync(dataSourceType);
				}
			}
		}
	}

	private void loadDataAsync(DataSourceType dataSourceType) {
		switch (dataSourceType) {
		case DailyTransactionList:

			break;
		case SearchTransactionList:

			break;
		case FavouriteTransactionList:
			showFavouriteTransactionListLineChartAsync();
			break;
		case DateRangeTransactionList:

			break;
		default:
			break;
		}
	}

	private void showFavouriteTransactionListLineChartAsync() {
		AsyncTask<Boolean, Void, Cursor> task = new AsyncTask<Boolean, Void, Cursor>() {

			@Override
			protected Cursor doInBackground(Boolean... params) {
				Cursor result = DataService.GetInstance(mContext)
						.getStarredBizLog();

				return result;
			}

			@Override
			protected void onPostExecute(Cursor result) {
				if (result != null && !result.isClosed()) {
					FragmentManager fm = getSupportFragmentManager();
					if (fm != null) {
						LineChartFragment fragment = (LineChartFragment) fm
								.findFragmentById(R.id.fragmentChart);
						if (fragment != null) {
							try {
								fragment.showData();

								fragment.addLines(result,
										getString(R.string.income),
										getString(R.string.expense),
										BizLogSchema.LastUpdateTime,
										BizLogSchema.Cost);
							} catch (ParseException e) {
								fragment.showMessage(e.getMessage());
								e.printStackTrace();
							}
						}
					}
				}
			}
		};
		task.execute();
	}
}
