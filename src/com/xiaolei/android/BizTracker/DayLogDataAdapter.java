package com.xiaolei.android.BizTracker;

import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiaolei.android.common.Utility;
import com.xiaolei.android.entity.BizLog;
import com.xiaolei.android.entity.BizLogSchema;

public class DayLogDataAdapter extends CursorAdapter {
	private LayoutInflater inflater;
	private Boolean showFullDateTime = false;
	private View.OnClickListener starOnClickListener;
	private boolean showCheckBox = false;
	private ArrayList<Long> checkedTransactionIds = new ArrayList<Long>();

	public DayLogDataAdapter(Context context, Cursor c,
			Boolean showFullDateTime, View.OnClickListener starOnClickListener) {
		super(context, c);
		this.showFullDateTime = showFullDateTime;
		this.starOnClickListener = starOnClickListener;
		inflater = LayoutInflater.from(context);
	}

	public DayLogDataAdapter(Context context, Cursor c,
			Boolean showFullDateTime, boolean showCheckBox,
			View.OnClickListener starOnClickListener) {
		super(context, c);
		this.showFullDateTime = showFullDateTime;
		this.starOnClickListener = starOnClickListener;
		inflater = LayoutInflater.from(context);
		this.showCheckBox = showCheckBox;
	}

	public void allowMultiCheckable(boolean allow) {
		showCheckBox = allow;
		if (allow == false) {
			checkedTransactionIds.clear();
		}
		this.notifyDataSetChanged();
	}

	public void setCheckedTransactionIds(ArrayList<Long> transactionIds) {
		if (transactionIds != null) {
			this.checkedTransactionIds = transactionIds;
		} else {
			checkedTransactionIds.clear();
		}
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

		BizLog transactionInfo = new BizLog();
		transactionInfo.setId(id);
		transactionInfo.setStuffName(stuffName);
		transactionInfo.setCost(cost);
		transactionInfo.setComment(comment);
		transactionInfo.setLastUpdateTime(Utility.parseDate(value, new Date()));
		transactionInfo.setCurrencyCode(currencyCode);
		transactionInfo.setStar(star);
		view.setTag(transactionInfo);

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
		CheckBox chkBox = (CheckBox) view.findViewById(R.id.checkBoxChecked);

		long[] tag = new long[] { (star == true ? 1 : 0), id };
		if (star) {
			ivStar.setImageResource(R.drawable.heart_on);
			ivStar.setTag(tag);
		} else {
			ivStar.setImageResource(R.drawable.heart_off);
			ivStar.setTag(tag);
		}

		tvStuffName.setText(stuffName);
		tvCurrencyCode.setText(currencyCode);
		tvComment.setText(comment);
		tvTime.setText(lastUpdateTime);

		int incomeColor = context.getResources().getColor(R.color.incomeColor);
		int expenseColor = context.getResources()
				.getColor(R.color.expenseColor);

		if (cost > 0) {
			tvCost.setText(Utility.formatCurrency(cost, currencyCode, false));
			tvCost.setTextColor(incomeColor);
			tvCurrencyCode.setTextColor(incomeColor);
		} else {
			tvCost.setText(Utility.formatCurrency(cost, currencyCode, false));
			tvCost.setTextColor(expenseColor);
			tvCurrencyCode.setTextColor(expenseColor);
		}

		if (this.starOnClickListener != null) {
			ivStar.setOnClickListener(starOnClickListener);
		}
		if (chkBox != null) {
			chkBox.setVisibility(showCheckBox ? CheckBox.VISIBLE
					: CheckBox.GONE);
			chkBox.setChecked(false);
			if (checkedTransactionIds.contains(id)) {
				chkBox.setChecked(true);
			}
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return inflater.inflate(R.layout.biz_item_template2, parent, false);
	}

}
