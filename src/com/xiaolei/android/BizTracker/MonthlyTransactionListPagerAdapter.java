/**
 * 
 */
package com.xiaolei.android.BizTracker;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import com.xiaolei.android.listener.OnLoadCompletedListener;

import android.app.Activity;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * @author xiaolei
 * 
 */
public class MonthlyTransactionListPagerAdapter extends PagerAdapter {

	private int count = 12;
	private Date currentDate = new Date();
	private Date date = new Date();
	private Activity context;
	private LayoutInflater inflater;
	private OnItemClickListener onItemClickListener;

	private HashMap<Integer, View> viewPositionMapping = new HashMap<Integer, View>();

	public MonthlyTransactionListPagerAdapter(Activity context, Date date) {
		this.context = context;
		this.inflater = LayoutInflater.from(context);

		// Set the start day of the specified month of date
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal.set(Calendar.MONTH, Calendar.JANUARY);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		this.date = cal.getTime();
		this.currentDate = date;
	}

	public View getViewAtPosition(int position) {
		View result = null;
		if (viewPositionMapping.containsKey(position)) {
			result = viewPositionMapping.get(position);
		}

		return result;
	}

	public void reloadView(int position) {
		View view = getViewAtPosition(position);
		if (view != null) {
			ListView lv = (ListView) view
					.findViewById(R.id.listViewYearlyTransactionList);
			if (lv != null) {
				MonthlyTransactionListAdapter adapter = (MonthlyTransactionListAdapter) lv
						.getAdapter();
				if (adapter != null) {
					adapter.loadDataAsync();
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.view.PagerAdapter#destroyItem(android.view.View,
	 * int, java.lang.Object)
	 */
	@Override
	public void destroyItem(View collection, int position, Object view) {
		if (viewPositionMapping.containsKey(position)) {
			viewPositionMapping.remove(position);
		}

		((ViewPager) collection).removeView((View) view);
		view = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.view.PagerAdapter#finishUpdate(android.view.View)
	 */
	@Override
	public void finishUpdate(View view) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.view.PagerAdapter#getCount()
	 */
	@Override
	public int getCount() {
		return count;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.view.PagerAdapter#instantiateItem(android.view.View,
	 * int)
	 */
	@Override
	public Object instantiateItem(View collection, int position) {
		ViewPager pager = (ViewPager) collection;
		View result = inflater.inflate(
				R.layout.pager_monthly_transaction_list_item, pager, false);
		if (!viewPositionMapping.containsKey(position)) {
			viewPositionMapping.put(position, result);
		}
		pager.addView(result);

		if (date != null) {
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTime(date);
			cal.add(Calendar.MONTH, position);
			currentDate = cal.getTime();
		}

		ListView lv = (ListView) result
				.findViewById(R.id.listViewMonthlyTransactionList);
		if (lv != null && onItemClickListener != null) {
			lv.setOnItemClickListener(onItemClickListener);
		}

		MonthlyTransactionListAdapter adpt = new MonthlyTransactionListAdapter(
				context, result, lv, currentDate,
				new OnLoadCompletedListener() {

					@Override
					public void onLoadCompleted(Boolean loadSuccess) {
						if (loadSuccess == true) {

						}
					}

				});

		adpt.loadDataAsync();

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.view.PagerAdapter#isViewFromObject(android.view.View,
	 * java.lang.Object)
	 */
	@Override
	public boolean isViewFromObject(View view, Object object) {

		return view == object;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.view.PagerAdapter#restoreState(android.os.Parcelable,
	 * java.lang.ClassLoader)
	 */
	@Override
	public void restoreState(Parcelable arg0, ClassLoader arg1) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.view.PagerAdapter#saveState()
	 */
	@Override
	public Parcelable saveState() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.view.PagerAdapter#startUpdate(android.view.View)
	 */
	@Override
	public void startUpdate(View arg0) {
		// TODO Auto-generated method stub

	}

	/**
	 * @param onItemClickListener
	 *            the onItemClickListener to set
	 */
	public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
		this.onItemClickListener = onItemClickListener;
	}

	/**
	 * @return the onItemClickListener
	 */
	public OnItemClickListener getOnItemClickListener() {
		return onItemClickListener;
	}

}
