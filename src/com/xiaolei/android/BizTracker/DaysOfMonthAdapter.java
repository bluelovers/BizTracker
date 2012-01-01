/**
 * 
 */
package com.xiaolei.android.BizTracker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.text.format.DateUtils;
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
public class DaysOfMonthAdapter extends BaseAdapter {

	private Activity context;
	private Date date;
	private Date[] items;
	private LayoutInflater inflater;
	private String defaultCurrencyCode = "";

	public DaysOfMonthAdapter(Activity context, Date date) {
		this.context = context;
		this.date = date;
		items = new Date[0];

		Calendar cal = Calendar.getInstance();
		cal.setTime(this.date);
		int days = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		Date startDay = Utility.getStartDayOfMonth(date);
		Date d = (Date) startDay.clone();
		ArrayList<Date> dates = new ArrayList<Date>();

		for (int i = 0; i < days; i++) {
			d = new Date(startDay.getYear(), startDay.getMonth(), i + 1);
			dates.add(d);
		}

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
			convertView = inflater.inflate(R.layout.day_of_month_item_template,
					parent, false);
		}

		TextView tvStuffName = (TextView) convertView
				.findViewById(R.id.textViewItemTemplateStuffName);
		TextView tvPay = (TextView) convertView
				.findViewById(R.id.textViewTotalPay);
		TextView tvEarn = (TextView) convertView
				.findViewById(R.id.textViewTotalEarn);

		Date date = items[position];

		// SimpleDateFormat format = new SimpleDateFormat("MMMM d");
		String formattedDateString = DateUtils.formatDateTime(context,
				date.getTime(), DateUtils.FORMAT_NO_YEAR);
		tvStuffName.setText(formattedDateString);

		double pay = DataService.GetInstance(context).getTotalPay(date,
				Utility.getEndTimeOfDate(date));
		double earn = DataService.GetInstance(context).getTotalEarn(date,
				Utility.getEndTimeOfDate(date));

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
