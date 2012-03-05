package com.xiaolei.android.adapter;

import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.xiaolei.android.BizTracker.R;
import com.xiaolei.android.common.Utility;

public class StatisticSummaryArrayAdapter extends ArrayAdapter<StatisticInfo> {

	private LayoutInflater inflater;
	private List<StatisticInfo> dataSource;
	private int itemTemplateResourceId = R.layout.statistic_item_template;

	public StatisticSummaryArrayAdapter(Context context,
			int textViewResourceId, List<StatisticInfo> objects) {
		super(context, textViewResourceId, objects);
		inflater = LayoutInflater.from(context);
		dataSource = objects;
		itemTemplateResourceId = textViewResourceId;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(itemTemplateResourceId, parent,
					false);
		}

		TextView tvTitle = (TextView) convertView
				.findViewById(R.id.textViewStatisticTitle);
		if (tvTitle != null) {
			tvTitle.setText(dataSource.get(position).Key);
		}
		TextView tvSummary = (TextView) convertView
				.findViewById(R.id.textViewStatisticSummary);
		if (tvSummary != null) {
			String summary = dataSource.get(position).Summary;
			if (!TextUtils.isEmpty(summary)) {
				tvSummary.setVisibility(TextView.VISIBLE);
				tvSummary.setText(summary);
			} else {
				tvSummary.setVisibility(TextView.GONE);
			}
		}
		TextView tvCurrencyCode = (TextView) convertView
				.findViewById(R.id.textViewCurrencyCode);
		if (tvCurrencyCode != null) {
			tvCurrencyCode.setText(dataSource.get(position).CurrencyCode);
		}
		TextView tvValue = (TextView) convertView
				.findViewById(R.id.textViewStatisticValue);
		if (tvValue != null) {
			StatisticInfo item = dataSource.get(position);
			if (item.Value >= 0) {
				tvValue.setTextColor(this.getContext().getResources()
						.getColor(R.color.incomeColor));
			} else {
				tvValue.setTextColor(this.getContext().getResources()
						.getColor(R.color.expenseColor));
			}
			tvValue.setText(Utility.formatCurrency(item.Value,
					item.CurrencyCode, false));
		}

		return convertView;
	}
}
