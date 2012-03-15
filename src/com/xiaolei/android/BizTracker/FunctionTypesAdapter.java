package com.xiaolei.android.BizTracker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.xiaolei.android.common.Utility;
import com.xiaolei.android.service.DataService;

public class FunctionTypesAdapter extends BaseAdapter {

	private String[] items;
	private LayoutInflater inflater;
	private Activity context;
	private ArrayList<View> viewList;
	private double todayPay = 0;
	private double todayEarn = 0;
	private String defaultCurrencyCode = "";

	public FunctionTypesAdapter(Activity context) {
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
			double value = DataService.GetInstance(context).getTodaySumPay();
			if (value < 0) {
				tvPay.setText(Utility
						.formatCurrency(value, defaultCurrencyCode));
			} else {
				tvPay.setText("*");
			}
			double value2 = DataService.GetInstance(context).getTodaySumEarn();
			if (value2 > 0) {
				tvEarn.setText(Utility.formatCurrency(value2,
						defaultCurrencyCode));
			} else {
				tvEarn.setText("*");
			}

			break;
		case 1: // This week
			Date startDayOfThisWeek = Utility.getStartDayOfThisWeek();
			Date endDayOfThisWeek = Utility.getEndTimeOfDate(Utility.addDays(
					startDayOfThisWeek, 6));
			tvDate.setText(String.format("%s ~ %s",
					Utility.toLocalDateString(context, startDayOfThisWeek),
					Utility.toLocalDateString(context, endDayOfThisWeek)));

			double valueWeekPay = DataService.GetInstance(context).getTotalPay(
					startDayOfThisWeek, endDayOfThisWeek);
			if (valueWeekPay < 0) {
				tvPay.setText(Utility.formatCurrency(valueWeekPay,
						defaultCurrencyCode));
			} else {
				tvPay.setText("*");
			}
			double valueWeekEarn = DataService.GetInstance(context)
					.getTotalEarn(startDayOfThisWeek, endDayOfThisWeek);
			if (valueWeekEarn > 0) {
				tvEarn.setText(Utility.formatCurrency(valueWeekEarn,
						defaultCurrencyCode));
			} else {
				tvEarn.setText("*");
			}

			break;
		case 2:// This month
			SimpleDateFormat format = new SimpleDateFormat("MMM");
			tvDate.setText(format.format(now));
			Date startDayOfMonth = Utility.getStartDayOfMonth(now);
			Date endDayOfMonth = Utility.getEndTimeOfDate(Utility
					.getEndDayOfMonth(now));

			double monthPay = DataService.GetInstance(context).getTotalPay(
					startDayOfMonth, endDayOfMonth);
			double monthEarn = DataService.GetInstance(context).getTotalEarn(
					startDayOfMonth, endDayOfMonth);

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
			Date startDayOfYear = Utility.getStartDayOfYear(now);
			Date endDayOfYear = Utility.getEndTimeOfDate(Utility
					.getEndDayOfYear(now));
			tvDate.setText(String.valueOf(Calendar.getInstance().get(
					Calendar.YEAR)));

			double yearPay = DataService.GetInstance(context).getTotalPay(
					startDayOfYear, endDayOfYear);
			double yearEarn = DataService.GetInstance(context).getTotalEarn(
					startDayOfYear, endDayOfYear);

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
		case 4:
			double[] cost = DataService.GetInstance(context)
					.getTransactionsTotalCost();
			if (cost != null && cost.length >= 3) {
				tvEarn.setText(Utility.formatCurrency(cost[0],
						defaultCurrencyCode));
				tvPay.setText(Utility.formatCurrency(cost[1],
						defaultCurrencyCode));
			}

			Date[] dateRange = DataService.GetInstance(context)
					.getTransactionsDateRange();
			if (dateRange != null && dateRange.length >= 2) {
				tvDate.setText(String.format("%s ~ %s",
						Utility.toLocalDateString(context, dateRange[0]),
						Utility.toLocalDateString(context, dateRange[1])));
			}

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

	@SuppressWarnings("unused")
	private void getTodayTotalPayAsync() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				todayPay = DataService.GetInstance(context).getTodaySumPay();
				context.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						View view = viewList.get(0);
						if (view != null) {
							TextView tvPay = (TextView) view
									.findViewById(R.id.textViewTotalPay);
							tvPay.setText(String.valueOf(todayPay));
						}
					}
				});
			}
		}).run();
	}

	@SuppressWarnings("unused")
	private void getTodayTotalEarnAsync() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				todayEarn = DataService.GetInstance(context).getTodaySumEarn();
				context.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						View view = viewList.get(0);
						if (view != null) {
							TextView tvPay = (TextView) view
									.findViewById(R.id.textViewTotalEarn);
							tvPay.setText(String.valueOf(todayEarn));
						}
					}
				});
			}
		}).run();
	}
}
