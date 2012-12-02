/**
 * 
 */
package com.xiaolei.android.BizTracker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import android.content.Context;
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
public class MonthsOfYearAdapter extends BaseAdapter {

	private Context context;
	private Date[] items;
	private LayoutInflater inflater;
	private String defaultCurrencyCode = "";

	public MonthsOfYearAdapter(Context context, Date date) {
		this.context = context;
		items = new Date[0];
		ArrayList<Date> dates = new ArrayList<Date>();

		for (int i = 0; i < 12; i++) {
			GregorianCalendar cal = new GregorianCalendar(
					date.getYear() + 1900, i, 1);
			Date d = cal.getTime();
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
		CurrencyView currencyViewMoney = (CurrencyView) convertView
				.findViewById(R.id.currencyViewMoney);

		Date date = items[position];
		SimpleDateFormat format = new SimpleDateFormat("MMM", Locale.getDefault());
		tvStuffName.setText(format.format(date));

		Date endDate = Utility.getEndTimeOfDate(Utility.getEndDayOfMonth(date));

		double pay = DataService.GetInstance(context)
				.getTotalPay(date, endDate);
		double earn = DataService.GetInstance(context).getTotalEarn(date,
				endDate);

		Money[] values = new Money[2];
		values[0] = new Money(pay, defaultCurrencyCode);
		values[1] = new Money(earn, defaultCurrencyCode);
		currencyViewMoney.setCost(values);

		return convertView;
	}
	


}