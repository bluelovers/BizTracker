package com.xiaolei.android.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

import com.xiaolei.android.BizTracker.R;
import com.xiaolei.android.common.Utility;
import com.xiaolei.android.service.DataService;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class TransactionHistoryTypesAdapter extends BaseAdapter {

	private String[] items;
	private LayoutInflater inflater;
	private Activity context;
	private ArrayList<View> viewList;
	private String defaultCurrencyCode = "";
	private Hashtable<Integer, double[]> mData;

	public TransactionHistoryTypesAdapter(Activity context,
			Hashtable<Integer, double[]> data) {
		mData = data;
		viewList = new ArrayList<View>();
		this.context = context;
		items = new String[] { context.getString(R.string.today),
				context.getString(R.string.this_week),
				context.getString(R.string.this_month),
				context.getString(R.string.this_year),
				context.getString(R.string.all_transactions),
				context.getString(R.string.search),
				context.getString(R.string.starred_biz_log),
		/* context.getString(R.string.project) */};
		inflater = LayoutInflater.from(context);
		defaultCurrencyCode = DataService.GetInstance(context)
				.getDefaultCurrencyCode();
	}

	@Override
	public int getCount() {
		return items.length;
	}

	@Override
	public Object getItem(int arg0) {
		return items[arg0];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public View getView(int index) {
		if (index >= 0 && index < viewList.size()) {
			return viewList.get(index);
		} else {
			return null;
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_template, parent,
					false);
			viewList.add(convertView);
		}

		TextView tvStuffName = (TextView) convertView
				.findViewById(R.id.textViewItemTemplateStuffName);
		TextView tvDate = (TextView) convertView
				.findViewById(R.id.textViewDate);
		TextView tvPay = (TextView) convertView
				.findViewById(R.id.textViewTotalPay);
		TextView tvEarn = (TextView) convertView
				.findViewById(R.id.textViewTotalEarn);

		tvDate.setText("");
		tvStuffName.setText(items[position].toString());

		tvPay.setVisibility(View.VISIBLE);
		tvEarn.setVisibility(View.VISIBLE);

		Date now = new Date();

		switch (position) {
		case 0: // Today
			tvDate.setText(Utility.getLocalCurrentDateString());
			double[] today = mData.get(0);
			double value = today[0];
			if (value < 0) {
				tvPay.setText(Utility
						.formatCurrency(value, defaultCurrencyCode));
			} else {
				tvPay.setText("*");
			}
			double value2 = today[1];
			if (value2 > 0) {
				tvEarn.setText(Utility.formatCurrency(value2,
						defaultCurrencyCode));
			} else {
				tvEarn.setText("*");
			}

			break;
		case 1: // This week
			double[] thisWeek = mData.get(1);
			Date startDayOfThisWeek = Utility.getStartDayOfThisWeek();
			Date endDayOfThisWeek = Utility.getEndTimeOfDate(Utility.addDays(
					startDayOfThisWeek, 6));
			tvDate.setText(String.format("%s ~ %s",
					Utility.toLocalDateString(context, startDayOfThisWeek),
					Utility.toLocalDateString(context, endDayOfThisWeek)));

			double valueWeekPay = thisWeek[0];
			if (valueWeekPay < 0) {
				tvPay.setText(Utility.formatCurrency(valueWeekPay,
						defaultCurrencyCode));
			} else {
				tvPay.setText("*");
			}
			double valueWeekEarn = thisWeek[1];
			if (valueWeekEarn > 0) {
				tvEarn.setText(Utility.formatCurrency(valueWeekEarn,
						defaultCurrencyCode));
			} else {
				tvEarn.setText("*");
			}

			break;
		case 2:// This month
			double[] thisMonth = mData.get(2);
			SimpleDateFormat format = new SimpleDateFormat("MMM");
			tvDate.setText(format.format(now));

			double monthPay = thisMonth[0];
			double monthEarn = thisMonth[1];

			if (monthPay < 0) {
				tvPay.setText(Utility.formatCurrency(monthPay,
						defaultCurrencyCode));
			} else {
				tvPay.setText("*");
			}
			if (monthEarn > 0) {
				tvEarn.setText(Utility.formatCurrency(monthEarn,
						defaultCurrencyCode));
			} else {
				tvEarn.setText("*");
			}

			break;
		case 3: // This year
			double[] thisYear = mData.get(3);
			tvDate.setText(String.valueOf(Calendar.getInstance().get(
					Calendar.YEAR)));

			double yearPay = thisYear[0];
			double yearEarn = thisYear[1];

			if (yearPay < 0) {
				tvPay.setText(String.valueOf(Utility.formatCurrency(yearPay,
						defaultCurrencyCode)));
			} else {
				tvPay.setText("*");
			}
			if (yearEarn > 0) {
				tvEarn.setText(Utility.formatCurrency(yearEarn,
						defaultCurrencyCode));
			} else {
				tvEarn.setText("*");
			}

			break;
		case 4: // All transactions
			double[] cost = mData.get(4);
			if (cost != null && cost.length >= 3) {
				tvEarn.setText(cost[0] != 0 ? Utility.formatCurrency(cost[0],
						defaultCurrencyCode) : "*");
				tvPay.setText(cost[1] != 0 ? Utility.formatCurrency(cost[1],
						defaultCurrencyCode) : "*");
			}

			int count = DataService.GetInstance(context)
					.getTransactionsTotalCount();
			tvDate.setText(context.getString(R.string.total_count) + " "
					+ String.valueOf(count));

			break;
		case 5: // Search
			tvPay.setVisibility(View.INVISIBLE);
			tvEarn.setVisibility(View.INVISIBLE);
			tvDate.setText(context.getString(R.string.search_by));
			break;
		case 6: // Starred
			tvPay.setVisibility(View.INVISIBLE);
			tvEarn.setVisibility(View.INVISIBLE);
			tvDate.setText(context.getString(R.string.view_starred_items));
			break;
		case 7: // Project
			tvDate.setText(context.getString(R.string.project_description));
			break;
		default:
			break;
		}

		return convertView;
	}
}
