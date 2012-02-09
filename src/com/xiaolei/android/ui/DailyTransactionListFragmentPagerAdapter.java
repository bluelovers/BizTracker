/**
 * 
 */
package com.xiaolei.android.ui;

import java.util.Date;

import com.xiaolei.android.common.Utility;
import com.xiaolei.android.listener.OnNotifyDataChangedListener;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * @author xiaolei
 * 
 */
public class DailyTransactionListFragmentPagerAdapter extends
		FragmentStatePagerAdapter implements OnNotifyDataChangedListener {
	private int mCount = 1;
	private Date mDate = new Date();
	private OnNotifyDataChangedListener onNotifyDataChangedListener;

	public void setOnNotifyDataChangedListener(
			OnNotifyDataChangedListener listener) {
		onNotifyDataChangedListener = listener;
	}

	public void notifyDataChanged() {
		if (onNotifyDataChangedListener != null) {
			onNotifyDataChangedListener.onNotifyDataChanged(this);
		}
	}

	public void setPageCount(int count) {
		mCount = count;
	}

	public DailyTransactionListFragmentPagerAdapter(FragmentManager fm,
			Date date) {
		super(fm);
		mDate = date;
	}

	public void increasePageCount() {
		++mCount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentStatePagerAdapter#getItem(int)
	 */
	@Override
	public Fragment getItem(int position) {
		DailyTransactionListFragment result = new DailyTransactionListFragment();
		Date date = Utility.addDays(mDate, position);
		result.setOnNotifyDataChangedListener(this);

		result.showTransactionListByDate(date);

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.view.PagerAdapter#getCount()
	 */
	@Override
	public int getCount() {
		return mCount;
	}

	@Override
	public void onNotifyDataChanged(Object sender) {
		this.notifyDataChanged();
	}

}
