package com.xiaolei.android.BizTracker;

import java.util.Hashtable;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;

import com.xiaolei.android.ui.TransactionHistoryFragment;
import com.xiaolei.android.ui.TransactionRecorderFragment;

public class MainFragmentPagerAdapter extends FragmentPagerAdapter {
	private final int PAGE_COUNT = 2;
	private Hashtable<Integer, Fragment> mFragments = new Hashtable<Integer, Fragment>();

	public MainFragmentPagerAdapter(FragmentManager fm) {
		super(fm);

	}

	@Override
	public Fragment getItem(int position) {
		Fragment result = null;
		switch (position) {
		case 0:
			result = TransactionRecorderFragment.newInstance();
			mFragments.put(position, result);

			break;
		case 1:
			result = TransactionHistoryFragment.newInstance();
			mFragments.put(position, result);

			break;
		default:
			break;
		}

		return result;
	}

	@Override
	public int getCount() {
		return PAGE_COUNT;
	}

	@Override
	public void destroyItem(View container, int position, Object object) {
		super.destroyItem(container, position, object);

		if (mFragments.containsKey(position)) {
			mFragments.remove(position);
		}
	}

	/**
	 * Get the specified fragment at position. If the fragment has not been
	 * constructed, returns null.
	 * 
	 * @param position
	 * @return
	 */
	public Fragment getFragmentAtPosition(int position) {
		Fragment result = mFragments.get(position);
		return result;
	}
}
