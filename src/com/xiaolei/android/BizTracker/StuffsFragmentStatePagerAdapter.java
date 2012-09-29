package com.xiaolei.android.BizTracker;

import com.xiaolei.android.listener.OnLoadedListener;
import com.xiaolei.android.service.DataService;
import com.xiaolei.android.ui.StuffsFragment;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;

public class StuffsFragmentStatePagerAdapter extends FragmentStatePagerAdapter {

	private int mPageCount = 0;
	private static final int PAGE_SIZE = 9;
	private Context mContext;
	private OnClickListener mOnButtonClickListener;
	private OnLongClickListener mOnStuffLongClickListener;
	private OnLoadedListener<Integer> mOnLoadedListener;

	public StuffsFragmentStatePagerAdapter(Context context, FragmentManager fm,
			OnClickListener onStuffClickListener,
			OnLongClickListener onStuffLongClickListener) {
		super(fm);

		mContext = context;
		mOnButtonClickListener = onStuffClickListener;
		mOnStuffLongClickListener = onStuffLongClickListener;

		calcPageCountAsync();
	}

	@Override
	public Fragment getItem(int position) {
		StuffsFragment result = StuffsFragment.newInstance(PAGE_SIZE, position
				* PAGE_SIZE);
		result.setOnStuffClickListener(mOnButtonClickListener);
		result.setOnStuffLongClickListener(mOnStuffLongClickListener);

		return result;
	}

	/**
	 * Override this method to make notifyDataSetChanged method works.
	 */
	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}

	@Override
	public int getCount() {
		return mPageCount;
	}

	@Override
	public void notifyDataSetChanged() {
		calcPageCountAsync();
		super.notifyDataSetChanged();
	}

	public void setOnLoadedListener(OnLoadedListener<Integer> onLoadedListener) {
		mOnLoadedListener = onLoadedListener;
	}

	public void onLoaded(Integer result) {
		if (mOnLoadedListener != null) {
			mOnLoadedListener.onLoaded(result);
		}
	}

	private void calcPageCountAsync() {
		AsyncTask<Void, Void, Integer> task = new AsyncTask<Void, Void, Integer>() {

			@Override
			protected Integer doInBackground(Void... params) {
				int result = DataService.GetInstance(mContext)
						.calcStuffsPageCount(PAGE_SIZE);
				return result;
			}

			@Override
			protected void onPostExecute(Integer result) {
				mPageCount = result;
				onLoaded(result);
			}
		};
		task.execute();
	}

}
