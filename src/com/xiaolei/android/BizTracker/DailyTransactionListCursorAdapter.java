/**
 * 
 */
package com.xiaolei.android.BizTracker;

import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.xiaolei.android.common.Utility;
import com.xiaolei.android.entity.BizLog;
import com.xiaolei.android.entity.BizLogSchema;
import com.xiaolei.android.event.OnLoadCursorCompletedListener;
import com.xiaolei.android.listener.OnStarImageViewClickListener;
import com.xiaolei.android.service.DataService;

/**
 * @author xiaolei
 * 
 */
public class DailyTransactionListCursorAdapter extends CursorAdapter {

	private LayoutInflater inflater;
	private Boolean showFullDateTime = false;
	private OnStarImageViewClickListener starOnClickListener;
	private ListView listView;
	private Activity context;
	private DailyTransactionListCursorAdapter self;
	private Date date = new Date();
	private OnLoadCursorCompletedListener loadCursorCompletedListener;
	private View parentView;
	private String searchKeyword = "";

	public String getSearchKeyword() {
		return this.searchKeyword;
	}

	public void setSearchKeyword(String value) {
		if (value != null) {
			if (!value.equalsIgnoreCase(searchKeyword)) {
				searchKeyword = value;
			}
		} else {
			searchKeyword = "";
		}
	}

	public DailyTransactionListCursorAdapter(Context context, Cursor c) {
		super(context, c);

		self = this;
	}

	public DailyTransactionListCursorAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);

		self = this;
	}

	public DailyTransactionListCursorAdapter(Activity context, View parentView,
			ListView listView, Date date,
			OnLoadCursorCompletedListener loadCursorCompletedListener,
			OnStarImageViewClickListener starOnClickListener) {
		super(context, null);
		self = this;

		this.date = date;
		this.context = context;
		this.listView = listView;
		this.loadCursorCompletedListener = loadCursorCompletedListener;
		this.starOnClickListener = starOnClickListener;
		this.parentView = parentView;
		inflater = LayoutInflater.from(context);

		this.listView.setAdapter(this);
	}

	public DailyTransactionListCursorAdapter(Activity context, ListView listView, Date date,
			OnLoadCursorCompletedListener loadCursorCompletedListener,
			OnStarImageViewClickListener starOnClickListener) {
		super(context, null);
		self = this;

		this.date = date;
		this.context = context;
		this.listView = listView;
		this.loadCursorCompletedListener = loadCursorCompletedListener;
		this.starOnClickListener = starOnClickListener;
		this.parentView = null;
		inflater = LayoutInflater.from(context);

		this.listView.setAdapter(this);
	}

	public void loadDataAsync() {
		AsyncTask<Boolean, Void, Cursor> task = new AsyncTask<Boolean, Void, Cursor>() {

			@Override
			protected Cursor doInBackground(Boolean... params) {
				Cursor cursor = null;
				if (TextUtils.isEmpty(searchKeyword)) {
					if (date != null) {
						cursor = DataService.GetInstance(context)
								.getBizLogByDay(date);
					}
				} else {
					cursor = DataService.GetInstance(context).searchBizLog(
							searchKeyword);
				}

				return cursor;
			}

			@Override
			protected void onPostExecute(Cursor result) {
				self.changeCursor(result);

				if (loadCursorCompletedListener != null) {
					loadCursorCompletedListener.onLoadCursorCompleted(
							parentView, result);
				}
			}
		};
		task.execute();
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final BizLog currentLog = new BizLog();
		view.setTag(currentLog);

		int columnIndex = cursor.getColumnIndex(BizLogSchema.CurrencyCode);
		if (columnIndex != -1) {
			currentLog.setCurrencyCode(cursor.getString(columnIndex));
		}

		columnIndex = cursor.getColumnIndex("StuffName");
		if (columnIndex != -1) {
			currentLog.setStuffName(cursor.getString(columnIndex));
		}

		columnIndex = cursor.getColumnIndex(BizLogSchema.Comment);
		if (columnIndex != -1) {
			currentLog.setComment(cursor.getString(columnIndex));
		}

		columnIndex = cursor.getColumnIndex(BizLogSchema.Id);
		if (columnIndex != -1) {
			currentLog.setId(cursor.getLong(columnIndex));
		}

		columnIndex = cursor.getColumnIndex(BizLogSchema.Cost);
		if (columnIndex != -1) {
			currentLog.setCost(cursor.getDouble(columnIndex));
		}

		columnIndex = cursor.getColumnIndex(BizLogSchema.LastUpdateTime);
		if (columnIndex != -1) {
			String value = cursor.getString(columnIndex);
			currentLog.setLastUpdateTime(Utility.parseDate(value, new Date()));
		}

		columnIndex = cursor.getColumnIndex(BizLogSchema.Star);
		if (columnIndex != -1) {
			String value = cursor.getString(columnIndex);
			currentLog.setStar(Boolean.parseBoolean(value));
		}

		String lastUpdateTime = "";
		if (!showFullDateTime) {
			lastUpdateTime = Utility.toLocalTimeString(currentLog
					.getLastUpdateTime());
		} else {
			lastUpdateTime = DateUtils.formatDateTime(context, currentLog
					.getLastUpdateTime().getTime(), DateUtils.FORMAT_SHOW_DATE);
		}

		TextView tvStuffName = (TextView) view
				.findViewById(R.id.textViewBizItemStuffName);
		final ImageView ivStar = (ImageView) view
				.findViewById(R.id.imageButtonStarIt);
		TextView tvCost = (TextView) view
				.findViewById(R.id.textViewBizItemCost);
		TextView tvCurrencyCode = (TextView) view
				.findViewById(R.id.textViewBizItemCurrencyCode);
		TextView tvTime = (TextView) view
				.findViewById(R.id.textViewBizItemDate);
		TextView tvComment = (TextView) view.findViewById(R.id.textViewComment);

		if (starOnClickListener != null) {
			ivStar.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if (starOnClickListener != null) {
						starOnClickListener.onStarImageViewClick(ivStar,
								currentLog, self);
					}
				}
			});
		}

		ivStar.setTag(currentLog);
		if (currentLog.getStar() == true) {
			ivStar.setImageResource(R.drawable.star);
		} else {
			ivStar.setImageResource(R.drawable.star_wb);
		}

		tvStuffName.setText(currentLog.getStuffName());
		tvCurrencyCode.setText(currentLog.getCurrencyCode());
		tvComment.setText(currentLog.getComment());
		tvTime.setText(lastUpdateTime);

		if (currentLog.getCost() > 0) {
			tvCost.setText(Utility.formatCurrency(currentLog.getCost(), currentLog.getCurrencyCode(), false));
			tvCost.setTextColor(Color.parseColor("#99CC00"));
			tvCurrencyCode.setTextColor(Color.parseColor("#99CC00"));
		} else {
			tvCost.setText(Utility.formatCurrency(currentLog.getCost(), currentLog.getCurrencyCode(), false));
			tvCost.setTextColor(Color.parseColor("#ff6600"));
			tvCurrencyCode.setTextColor(Color.parseColor("#ff6600"));
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return inflater.inflate(R.layout.biz_item_template2, parent, false);
	}

	/**
	 * @param showFullDateTime
	 *            the showFullDateTime to set
	 */
	public void setShowFullDateTime(Boolean showFullDateTime) {
		this.showFullDateTime = showFullDateTime;
	}

	/**
	 * @return the showFullDateTime
	 */
	public Boolean getShowFullDateTime() {
		return this.showFullDateTime;
	}

}
