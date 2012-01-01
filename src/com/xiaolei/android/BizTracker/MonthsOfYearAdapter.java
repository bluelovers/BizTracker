/**
 * 
 */
package com.xiaolei.android.BizTracker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.xiaolei.android.common.Utility;
import com.xiaolei.android.event.OnLoadCursorCompletedListener;
import com.xiaolei.android.listener.OnStarImageViewClickListener;
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

	private OnLoadCursorCompletedListener loadCursorCompletedListener;
	private ListView listView;

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

	public MonthsOfYearAdapter(Context context, ListView listView, Date date,
			OnLoadCursorCompletedListener loadCursorCompletedListener) {
		this(context, date);

		this.loadCursorCompletedListener = loadCursorCompletedListener;
		this.listView = listView;
		this.listView.setAdapter(this);
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
		SimpleDateFormat format = new SimpleDateFormat("MMMM");
		tvStuffName.setText(format.format(date));

		Date endDate = Utility.getEndTimeOfDate(Utility.getEndDayOfMonth(date));

		double pay = DataService.GetInstance(context)
				.getTotalPay(date, endDate);
		double earn = DataService.GetInstance(context).getTotalEarn(date,
				endDate);

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