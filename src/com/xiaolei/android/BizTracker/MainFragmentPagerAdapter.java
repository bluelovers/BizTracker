package com.xiaolei.android.BizTracker;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;

import com.xiaolei.android.listener.OnBackButtonClickListener;
import com.xiaolei.android.listener.OnRefreshListener;
import com.xiaolei.android.listener.OnRequestNavigateListener;
import com.xiaolei.android.ui.TransactionHistoryFragment;
import com.xiaolei.android.ui.TransactionRecorderFragment;

public class MainFragmentPagerAdapter extends FragmentPagerAdapter implements
		OnRefreshListener, OnBackButtonClickListener {
	private final int PAGE_COUNT = 2;
	private Hashtable<Integer, Fragment> mFragments = new Hashtable<Integer, Fragment>();
	private List<OnRefreshListener> mOnRefreshListeners = new ArrayList<OnRefreshListener>();
	private List<OnBackButtonClickListener> mOnBackButtonClickListener = new ArrayList<OnBackButtonClickListener>();
	private OnRequestNavigateListener mOnRequestNavigateListener;

	public MainFragmentPagerAdapter(FragmentManager fm) {
		super(fm);

	}

	public void setOnRequestNavigateListener(OnRequestNavigateListener listener) {
		mOnRequestNavigateListener = listener;
	}

	@Override
	public Fragment getItem(int position) {
		Fragment result = null;
		switch (position) {
		case 0:
			TransactionRecorderFragment recorder = TransactionRecorderFragment
					.newInstance();
			result = recorder;
			recorder.setOnRefreshListener(this);
			recorder.setOnRequestNavigate(mOnRequestNavigateListener);
			mOnBackButtonClickListener.add(recorder);

			mFragments.put(position, recorder);

			break;
		case 1:
			TransactionHistoryFragment history = TransactionHistoryFragment
					.newInstance();
			mOnRefreshListeners.add(history);
			result = history;

			mFragments.put(position, history);

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

	@Override
	public void onRefresh() {
		if (mOnRefreshListeners != null) {
			for (OnRefreshListener listener : mOnRefreshListeners) {
				if (listener != null) {
					listener.onRefresh();
				}
			}
		}
	}

	@Override
	public boolean OnBackButtonClick() {
		boolean preventClose = false;
		if (mOnBackButtonClickListener != null) {
			for (OnBackButtonClickListener listener : mOnBackButtonClickListener) {
				if (listener != null) {
					boolean result = listener.OnBackButtonClick();
					if(result){
						preventClose = true;
					}
				}
			}
		}
		
		return preventClose;
	}
}
