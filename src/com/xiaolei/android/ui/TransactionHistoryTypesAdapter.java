package com.xiaolei.android.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;

import com.xiaolei.android.BizTracker.R;
import com.xiaolei.android.common.Money;
import com.xiaolei.android.common.Utility;
import com.xiaolei.android.customControl.CurrencyView;
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
		CurrencyView tvMoney = (CurrencyView) convertView
				.findViewById(R.id.currencyViewMoney);

		tvDate.setText("");
		tvStuffName.setText(items[position].toString());

		Date now = new Date();

		switch (position) {
		case 0: // Today
			tvDate.setText(Utility.getLocalCurrentDateString());
			double[] today = mData.get(0);
			Money[] todayMoney = new Money[2];
		    todayMoney[0] = new Money(today[0], defaultCurrencyCode);
		    todayMoney[1] = new Money(today[1], defaultCurrencyCode);
		    tvMoney.setCost(todayMoney);

			break;
		case 1: // This week
			double[] thisWeek = mData.get(1);
			Date startDayOfThisWeek = Utility.getStartDayOfThisWeek();
			Date endDayOfThisWeek = Utility.getEndTimeOfDate(Utility.addDays(
					startDayOfThisWeek, 6));
			tvDate.setText(String.format("%s ~ %s",
					Utility.toLocalDateString(context, startDayOfThisWeek),
					Utility.toLocalDateString(context, endDayOfThisWeek)));

			Money[] weeklyMoney = new Money[2];
			weeklyMoney[0] = new Money(thisWeek[0], defaultCurrencyCode);
			weeklyMoney[1] = new Money(thisWeek[1], defaultCurrencyCode);
		    tvMoney.setCost(weeklyMoney);

			break;
		case 2:// This month
			double[] thisMonth = mData.get(2);
			SimpleDateFormat format = new SimpleDateFormat("MMM", Locale.getDefault());
			tvDate.setText(format.format(now));

			Money[] monthlyMoney = new Money[2];
			monthlyMoney[0] = new Money(thisMonth[0], defaultCurrencyCode);
			monthlyMoney[1] = new Money(thisMonth[1], defaultCurrencyCode);
		    tvMoney.setCost(monthlyMoney);

			break;
		case 3: // This year
			double[] thisYear = mData.get(3);
			tvDate.setText(String.valueOf(Calendar.getInstance().get(
					Calendar.YEAR)));

			Money[] yearlyMoney = new Money[2];
			yearlyMoney[0] = new Money(thisYear[0], defaultCurrencyCode);
			yearlyMoney[1] = new Money(thisYear[1], defaultCurrencyCode);
		    tvMoney.setCost(yearlyMoney);

			break;
		case 4: // All transactions
			double[] cost = mData.get(4);
			if (cost != null && cost.length >= 3) {
				Money[] allTransactionsMoney = new Money[2];
				allTransactionsMoney[0] = new Money(cost[0], defaultCurrencyCode);
				allTransactionsMoney[1] = new Money(cost[1], defaultCurrencyCode);
			    tvMoney.setCost(allTransactionsMoney);
			}

			int count = DataService.GetInstance(context)
					.getTransactionsTotalCount();
			tvDate.setText(context.getString(R.string.total_count) + " "
					+ String.valueOf(count));

			break;
		case 5: // Search
			tvMoney.setVisibility(View.GONE);
			tvDate.setText(context.getString(R.string.search_by));
			break;
		case 6: // Starred
			tvMoney.setVisibility(View.GONE);
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
