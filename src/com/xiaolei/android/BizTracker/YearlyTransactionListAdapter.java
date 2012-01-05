/**
 * 
 */
package com.xiaolei.android.BizTracker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.xiaolei.android.common.Utility;
import com.xiaolei.android.entity.CostValue;
import com.xiaolei.android.listener.OnLoadCompletedListener;
import com.xiaolei.android.service.DataService;

/**
 * @author xiaolei
 * 
 */
public class YearlyTransactionListAdapter extends BaseAdapter {

	private Context context;
	private Date[] items;
	private LayoutInflater inflater;
	private String defaultCurrencyCode = "";
	private Hashtable<Integer, CostValue> dataSource;

	private OnLoadCompletedListener loadCompletedListener;
	private ListView listView;
	private YearlyTransactionListAdapter self;
	private View parentView;

	public YearlyTransactionListAdapter(Context context, Date date) {
		this.self = this;
		this.context = context;
		items = new Date[0];
		ArrayList<Date> dates = new ArrayList<Date>();
		dataSource = new Hashtable<Integer, CostValue>();

		for (int i = 0; i < 12; i++) {
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTime(date);
			cal.set(Calendar.MONTH, i);
			cal.set(Calendar.DAY_OF_MONTH, 1);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			Date d = cal.getTime();
			dates.add(d);

			CostValue value = new CostValue();
			dataSource.put(i, value);
		}

		items = dates.toArray(items);
		inflater = LayoutInflater.from(context);
	}

	public YearlyTransactionListAdapter(Context context, View parentView,
			ListView listView, Date date,
			OnLoadCompletedListener loadCompletedListener) {
		this(context, date);

		this.parentView = parentView;
		this.loadCompletedListener = loadCompletedListener;
		this.listView = listView;
	}

	public void loadDataAsync() {
		if (parentView != null) {
			// Hide loading view
			ViewFlipper viewSwitcher = (ViewFlipper) parentView
					.findViewById(R.id.viewFlipperYearlyTransactionList);
			if (viewSwitcher != null) {
				viewSwitcher.setDisplayedChild(0);
			}
		}

		AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(Void... args) {
				defaultCurrencyCode = DataService.GetInstance(context)
						.getDefaultCurrencyCode();
				for (int i = 0; i < items.length; i++) {
					Date month = items[i];
					Date endDate = Utility.getEndTimeOfDate(Utility
							.getEndDayOfMonth(month));
					double pay = DataService.GetInstance(context).getTotalPay(
							month, endDate);
					double earn = DataService.GetInstance(context)
							.getTotalEarn(month, endDate);

					CostValue value = new CostValue();
					value.ExpenseMoney = pay;
					value.IncomeMoney = earn;

					dataSource.put(i, value);
				}

				return true;
			}

			protected void onPostExecute(Boolean result) {

				if (parentView != null) {
					// Hide loading view
					ViewFlipper viewSwitcher = (ViewFlipper) parentView
							.findViewById(R.id.viewFlipperYearlyTransactionList);
					if (viewSwitcher != null) {
						viewSwitcher.setDisplayedChild(1);
					}
				}

				if (result == true) {
					if (listView != null) {
						listView.setAdapter(self);
					}
				}

				if (loadCompletedListener != null) {
					loadCompletedListener.onLoadCompleted(result);
				}
			}

		};
		task.execute();
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

		double pay = dataSource.get(position).ExpenseMoney;
		double earn = dataSource.get(position).IncomeMoney;

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