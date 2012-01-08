package com.xiaolei.android.BizTracker;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiaolei.android.common.Utility;
import com.xiaolei.android.entity.BizLogSchema;

public class DayLogDataAdapter extends CursorAdapter {
	private LayoutInflater inflater;
	private Boolean showFullDateTime = false;
	private View.OnClickListener starOnClickListener;

	public DayLogDataAdapter(Context context, Cursor c,
			Boolean showFullDateTime, View.OnClickListener starOnClickListener) {
		super(context, c);
		this.showFullDateTime = showFullDateTime;
		this.starOnClickListener = starOnClickListener;
		inflater = LayoutInflater.from(context);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		String currencyCode = cursor.getString(cursor
				.getColumnIndex("CurrencyCode"));
		String stuffName = cursor.getString(cursor.getColumnIndex("StuffName"));
		String comment = cursor.getString(cursor.getColumnIndex("Comment"));
		long id = cursor.getLong(cursor.getColumnIndex("_id"));

		double cost = cursor
				.getDouble(cursor.getColumnIndex(BizLogSchema.Cost));
		String value = cursor.getString(cursor
				.getColumnIndex(BizLogSchema.LastUpdateTime));

		String lastUpdateTime = "";
		if (!showFullDateTime) {
			lastUpdateTime = Utility.toLocalTimeString(value);
		} else {
			lastUpdateTime = Utility.toLocalDateString(value);
		}

		String starString = cursor.getString(cursor
				.getColumnIndex(BizLogSchema.Star));
		Boolean star = Boolean.parseBoolean(starString);

		TextView tvStuffName = (TextView) view
				.findViewById(R.id.textViewBizItemStuffName);
		ImageView ivStar = (ImageView) view
				.findViewById(R.id.imageButtonStarIt);
		TextView tvCost = (TextView) view
				.findViewById(R.id.textViewBizItemCost);
		TextView tvCurrencyCode = (TextView) view
				.findViewById(R.id.textViewBizItemCurrencyCode);
		TextView tvTime = (TextView) view
				.findViewById(R.id.textViewBizItemDate);
		TextView tvComment = (TextView) view.findViewById(R.id.textViewComment);

		long[] tag = new long[] { (star == true ? 1 : 0), id };
		if (star) {
			ivStar.setImageResource(R.drawable.star);
			ivStar.setTag(tag);
		} else {
			ivStar.setImageResource(R.drawable.star_wb);
			ivStar.setTag(tag);
		}

		tvStuffName.setText(stuffName);
		tvCurrencyCode.setText(currencyCode);
		tvComment.setText(comment);
		tvTime.setText(lastUpdateTime);

		if (cost > 0) {
			tvCost.setText(Utility.formatCurrency(cost, currencyCode, false));
			tvCost.setTextColor(Color.parseColor("#99CC00"));
			tvCurrencyCode.setTextColor(Color.parseColor("#99CC00"));
		} else {
			tvCost.setText(Utility.formatCurrency(cost, currencyCode, false));
			tvCost.setTextColor(Color.parseColor("#ff6600"));
			tvCurrencyCode.setTextColor(Color.parseColor("#ff6600"));
		}

		if (this.starOnClickListener != null) {
			ivStar.setOnClickListener(starOnClickListener);
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return inflater.inflate(R.layout.biz_item_template2, parent, false);
	}

}
