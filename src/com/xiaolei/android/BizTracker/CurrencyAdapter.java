package com.xiaolei.android.BizTracker;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.xiaolei.android.common.Utility;
import com.xiaolei.android.entity.BizLogSchema;
import com.xiaolei.android.entity.CurrencySchema;

public class CurrencyAdapter extends CursorAdapter {
	private LayoutInflater inflater;

	public CurrencyAdapter(Context context, Cursor c) {
		super(context, c);
		inflater = LayoutInflater.from(context);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		String currencyCode = cursor.getString(cursor
				.getColumnIndex("Code"));
		String name = cursor.getString(cursor.getColumnIndex("Name"));
		String value = cursor.getString(cursor
				.getColumnIndex(BizLogSchema.LastUpdateTime));
		String lastUpdateTime = Utility.toLocalDateString(value);
		String exchangeRate = cursor.getString(cursor.getColumnIndex(CurrencySchema.USDExchangeRate));


		TextView tvName = (TextView) view
				.findViewById(R.id.textViewCurrencyName);
		TextView tvTime = (TextView) view
		.findViewById(R.id.textViewLastUpdateTime);
		TextView tvExchangeRate = (TextView) view
		.findViewById(R.id.textViewExchangeRate);
		tvExchangeRate.setTextColor(Color.parseColor("#99CC00"));

		tvName.setText(String.format("%s (%s)", name, currencyCode));
		tvTime.setText(lastUpdateTime);
		tvExchangeRate.setText(exchangeRate);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return inflater.inflate(R.layout.currency_item_template, parent, false);
	}

}
