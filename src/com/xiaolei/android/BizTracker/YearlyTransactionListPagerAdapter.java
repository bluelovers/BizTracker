/**
 * 
 */
package com.xiaolei.android.BizTracker;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import android.app.Activity;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import com.xiaolei.android.listener.OnLoadCompletedListener;

/**
 * @author xiaolei
 * 
 */
public class YearlyTransactionListPagerAdapter extends PagerAdapter {

	private Date date = new Date();
	private Activity context;
	private int count = 100;
	private LayoutInflater inflater;
	private OnItemClickListener onItemClickListener;

	private HashMap<Integer, View> viewPositionMapping = new HashMap<Integer, View>();

	public YearlyTransactionListPagerAdapter(Activity context, Date date,
			int pageCount) {
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		this.count = pageCount;

		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal.add(Calendar.YEAR, -pageCount + 1);
		cal.set(Calendar.MONTH, Calendar.JANUARY);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		this.date = cal.getTime();
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
				YearlyTransactionListAdapter adapter = (YearlyTransactionListAdapter) lv
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
		// count = count + (currentDate.before(new Date()) ? 1 : 0);
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
				R.layout.pager_child_transaction_list_yearly, pager, false);
		if (!viewPositionMapping.containsKey(position)) {
			viewPositionMapping.put(position, result);
		}
		pager.addView(result);

		Date currentDate = null;
		if (date != null) {
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTime(date);
			cal.add(Calendar.YEAR, position);
			currentDate = cal.getTime();
		}

		ListView lv = (ListView) result
				.findViewById(R.id.listViewYearlyTransactionList);
		if (lv != null && onItemClickListener != null) {
			lv.setOnItemClickListener(onItemClickListener);
		}

		YearlyTransactionListAdapter adpt = new YearlyTransactionListAdapter(
				context, result, lv, currentDate, new OnLoadCompletedListener() {

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
