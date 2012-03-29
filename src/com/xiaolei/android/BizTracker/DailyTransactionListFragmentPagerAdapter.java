/**
 * 
 */
package com.xiaolei.android.BizTracker;

import java.util.Date;
import com.xiaolei.android.common.Utility;
import com.xiaolei.android.listener.OnNotifyDataChangedListener;
import com.xiaolei.android.ui.TransactionListFragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

/**
 * @author xiaolei
 * 
 */
public class DailyTransactionListFragmentPagerAdapter extends
		FragmentStatePagerAdapter implements OnNotifyDataChangedListener {
	private int mCount = 1;
	private Date mDate = new Date();
	private OnNotifyDataChangedListener onNotifyDataChangedListener;
	private SparseArray<Fragment> mInitializedFragments = new SparseArray<Fragment>();

	public void setOnNotifyDataChangedListener(
			OnNotifyDataChangedListener listener) {
		onNotifyDataChangedListener = listener;
	}

	public void notifyDataChanged() {
		if (onNotifyDataChangedListener != null) {
			onNotifyDataChangedListener.onNotifyDataChanged(this);
		}
	}

	public Fragment getFragmentAtPosition(int position) {
		Fragment result = mInitializedFragments.get(position);
		return result;
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
		TransactionListFragment result = new TransactionListFragment();
		Date date = Utility.addDays(mDate, position);
		result.setOnNotifyDataChangedListener(this);

		if (mInitializedFragments.get(position) == null) {
			mInitializedFragments.put(position, result);
		}

		result.showTransactionListByDate(date);

		return result;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		super.destroyItem(container, position, object);

		if (mInitializedFragments.get(position) != null) {
			mInitializedFragments.remove(position);
		}
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
