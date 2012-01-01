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
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorTreeAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiaolei.android.common.Utility;
import com.xiaolei.android.entity.BizLog;
import com.xiaolei.android.entity.BizLogSchema;
import com.xiaolei.android.event.OnLoadCursorCompletedListener;
import com.xiaolei.android.listener.OnExpandedListViewStarImageViewClickListener;
import com.xiaolei.android.service.DataService;

/**
 * @author xiaolei
 * 
 */
public class TransactionListCursorTreeAdapter extends CursorTreeAdapter {

	private Context context;
	private LayoutInflater inflater;
	private Date date;
	private View parentView;
	private ExpandableListView listView;
	private OnExpandedListViewStarImageViewClickListener starOnClickListener;
	private OnLoadCursorCompletedListener loadCursorCompletedListener;
	private TransactionListCursorTreeAdapter self;
	private String defaultCurrencyCode = "CNY";

	public TransactionListCursorTreeAdapter(Activity context, View parentView,
			ExpandableListView listView, Date date, Boolean showFullDateTime,
			OnLoadCursorCompletedListener loadCursorCompletedListener,
			OnExpandedListViewStarImageViewClickListener starOnClickListener) {
		super(null, context);

		this.self = this;
		this.parentView = parentView;
		this.listView = listView;
		this.context = context;
		this.date = date;
		this.loadCursorCompletedListener = loadCursorCompletedListener;
		this.starOnClickListener = starOnClickListener;

		inflater = LayoutInflater.from(context);
		this.listView.setAdapter(this);
	}

	public TransactionListCursorTreeAdapter(Cursor cursor, Context context,
			boolean autoRequery) {
		super(cursor, context, autoRequery);

		this.self = this;
		this.context = context;
		inflater = LayoutInflater.from(context);
	}

	public void loadDataAsync() {
		AsyncTask<Boolean, Void, Cursor> task = new AsyncTask<Boolean, Void, Cursor>() {

			@Override
			protected Cursor doInBackground(Boolean... params) {
				Cursor cursor = null;

				defaultCurrencyCode = DataService.GetInstance(context)
						.getDefaultCurrencyCode();
				double defaultCurrencyUSDExchangeRate = DataService
						.GetInstance(context).getUSDExchangeRate(
								defaultCurrencyCode);
				cursor = DataService.GetInstance(context)
						.getGroupedTransactionListByDay(date,
								defaultCurrencyCode,
								defaultCurrencyUSDExchangeRate);

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.CursorTreeAdapter#bindChildView(android.view.View,
	 * android.content.Context, android.database.Cursor, boolean)
	 */
	@Override
	protected void bindChildView(View view, Context context, Cursor cursor,
			boolean isLastChild) {
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

		String lastUpdateTime = DateUtils.formatDateTime(context, currentLog
				.getLastUpdateTime().getTime(), DateUtils.FORMAT_SHOW_DATE);

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
			tvCost.setText(Utility.formatCurrency(currentLog.getCost(), ""));
			tvCost.setTextColor(Color.parseColor("#99CC00"));
			tvCurrencyCode.setTextColor(Color.parseColor("#99CC00"));
		} else {
			tvCost.setText(Utility.formatCurrency(currentLog.getCost(), ""));
			tvCost.setTextColor(Color.parseColor("#ff6600"));
			tvCurrencyCode.setTextColor(Color.parseColor("#ff6600"));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.CursorTreeAdapter#bindGroupView(android.view.View,
	 * android.content.Context, android.database.Cursor, boolean)
	 */
	@Override
	protected void bindGroupView(View view, Context context, Cursor cursor,
			boolean isExpanded) {
		final BizLog currentLog = new BizLog();
		view.setTag(currentLog);

		currentLog.setCurrencyCode(defaultCurrencyCode);

		int columnIndex = cursor.getColumnIndex("StuffName");
		if (columnIndex != -1) {
			currentLog.setStuffName(cursor.getString(columnIndex));
		}

		columnIndex = cursor.getColumnIndex("SumCost");
		if (columnIndex != -1) {
			currentLog.setCost(cursor.getDouble(columnIndex));
		}

		int stuffCounts = 0;
		columnIndex = cursor.getColumnIndex("StuffCounts");
		if (columnIndex != -1) {
			stuffCounts = cursor.getInt(columnIndex);
		}

		String fromTimeString = "";
		String toTimeString = "";
		Date fromTime = null;
		Date toTime = null;

		columnIndex = cursor.getColumnIndex("FromTime");
		if (columnIndex != -1) {
			fromTimeString = cursor.getString(columnIndex);
			fromTime = Utility.parseDate(fromTimeString, null);
			if (fromTime != null) {
				fromTimeString = DateUtils.formatDateTime(context,
						fromTime.getTime(), DateUtils.FORMAT_SHOW_DATE);
			}
		}

		columnIndex = cursor.getColumnIndex("ToTime");
		if (columnIndex != -1) {
			toTimeString = cursor.getString(columnIndex);
			toTime = Utility.parseDate(toTimeString, null);
			if (toTime != null) {
				toTimeString = DateUtils.formatDateTime(context,
						toTime.getTime(), DateUtils.FORMAT_SHOW_DATE);
			}
		}

		String dateRange = "";
		if (fromTimeString.equals(toTimeString) == false) {
			dateRange = String.format("%s ~ %s", fromTimeString, toTimeString);
		} else {
			dateRange = fromTimeString;
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

		if (ivStar != null) {
			ivStar.setImageResource(isExpanded ? R.drawable.expander_ic_minimized
					: R.drawable.expander_ic_maximized);
		}

		if (stuffCounts > 1) {
			tvStuffName.setText(String.format("%s (%d)",
					currentLog.getStuffName(), stuffCounts));
		} else {
			tvStuffName.setText(currentLog.getStuffName());
		}
		tvCurrencyCode.setText(currentLog.getCurrencyCode());
		tvComment.setText("");
		tvTime.setText(dateRange);

		if (currentLog.getCost() > 0) {
			tvCost.setText(Utility.formatCurrency(currentLog.getCost(), ""));
			tvCost.setTextColor(Color.parseColor("#99CC00"));
			tvCurrencyCode.setTextColor(Color.parseColor("#99CC00"));
		} else {
			tvCost.setText(Utility.formatCurrency(currentLog.getCost(), ""));
			tvCost.setTextColor(Color.parseColor("#ff6600"));
			tvCurrencyCode.setTextColor(Color.parseColor("#ff6600"));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.CursorTreeAdapter#getChildrenCursor(android.database.Cursor
	 * )
	 */
	@Override
	protected Cursor getChildrenCursor(Cursor groupCursor) {
		Cursor result = null;
		if (groupCursor != null) {
			int stuffCounts = groupCursor.getInt(groupCursor
					.getColumnIndex("StuffCounts"));
			long stuffId = groupCursor.getLong(groupCursor
					.getColumnIndex("StuffId"));
			if (stuffCounts > 1) {
				result = DataService.GetInstance(context)
						.getTransactionListByStuffId(stuffId, date);
			}
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.CursorTreeAdapter#newChildView(android.content.Context,
	 * android.database.Cursor, boolean, android.view.ViewGroup)
	 */
	@Override
	protected View newChildView(Context context, Cursor cursor,
			boolean isLastChild, ViewGroup parent) {

		View result = inflater.inflate(R.layout.biz_item_template2, parent,
				false);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.CursorTreeAdapter#newGroupView(android.content.Context,
	 * android.database.Cursor, boolean, android.view.ViewGroup)
	 */
	@Override
	protected View newGroupView(Context context, Cursor cursor,
			boolean isExpanded, ViewGroup parent) {
		return inflater.inflate(R.layout.biz_item_template2, parent, false);
	}

}
