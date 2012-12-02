/**
 * 
 */
package com.xiaolei.android.BizTracker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.xiaolei.android.common.Money;
import com.xiaolei.android.common.Utility;
import com.xiaolei.android.customControl.CurrencyView;
import com.xiaolei.android.service.DataService;

/**
 * @author xiaolei
 * 
 */
public class WeeklyTransactionListAdapter extends BaseAdapter {

	private Date[] items;
	private LayoutInflater inflater;
	private Activity context;
	private String defaultCurrencyCode = "";

	public WeeklyTransactionListAdapter(Activity context, Date startDate,
			Date endDate) {
		this.context = context;

		ArrayList<Date> dates = new ArrayList<Date>();

		Date date = startDate;
		while (date.before(endDate) || date.getDate() == endDate.getDate()) {
			dates.add(date);
			date = Utility.addDays(date, 1);
		}

		items = new Date[0];
		items = dates.toArray(items);

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

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_template, parent,
					false);
		}

		TextView tvStuffName = (TextView) convertView
				.findViewById(R.id.textViewItemTemplateStuffName);
		TextView tvDate = (TextView) convertView
				.findViewById(R.id.textViewDate);
		CurrencyView tvMoney = (CurrencyView) convertView
				.findViewById(R.id.currencyViewMoney);

		Date value = items[position];
		Date now = new Date();
		tvDate.setText(Utility.toLocalDateString(context, value));
		SimpleDateFormat format = new SimpleDateFormat("EEEE",
				Locale.getDefault());// E
		tvStuffName.setText(format.format(value));
		if (value.getDate() == now.getDate()) {
			tvDate.setText(tvDate.getText().toString() + " ("
					+ context.getString(R.string.today) + ")");
		}

		double pay = DataService.GetInstance(context).getTotalPay(value,
				Utility.getEndTimeOfDate(value));
		double earn = DataService.GetInstance(context).getTotalEarn(value,
				Utility.getEndTimeOfDate(value));
		Money[] values = new Money[2];
		values[0] = new Money(pay, defaultCurrencyCode);
		values[1] = new Money(earn, defaultCurrencyCode);
		tvMoney.setCost(values);

		return convertView;
	}

}