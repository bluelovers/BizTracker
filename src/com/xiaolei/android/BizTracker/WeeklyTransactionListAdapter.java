/**
 * 
 */
package com.xiaolei.android.BizTracker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.xiaolei.android.common.Utility;
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

	public WeeklyTransactionListAdapter(Activity context, Date startDate, Date endDate) {
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
		TextView tvPay = (TextView) convertView
				.findViewById(R.id.textViewTotalPay);
		TextView tvEarn = (TextView) convertView
				.findViewById(R.id.textViewTotalEarn);

		Date value = items[position];
		Date now = new Date();
		tvDate.setText(Utility.toLocalDateString(context, value));
		SimpleDateFormat format = new SimpleDateFormat("EEEE");// E
		tvStuffName.setText(format.format(value));
		if (value.getDate() == now.getDate()) {
			tvDate.setText(tvDate.getText().toString() + " ("
					+ context.getString(R.string.today) + ")");
		}

		double pay = DataService.GetInstance(context).getTotalPay(value,
				Utility.getEndTimeOfDate(value));
		double earn = DataService.GetInstance(context).getTotalEarn(value,
				Utility.getEndTimeOfDate(value));

		if (pay < 0) {
			tvPay.setText(Utility.formatCurrency(pay, defaultCurrencyCode));
		} else {
			tvPay.setText("*");
		}
		if (earn > 0) {
			tvEarn.setText(Utility.formatCurrency(earn, defaultCurrencyCode));
		} else {
			tvEarn.setText("*");
		}

		return convertView;
	}

}