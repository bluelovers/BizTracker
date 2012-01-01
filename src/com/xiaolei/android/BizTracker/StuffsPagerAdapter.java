/**
 * 
 */
package com.xiaolei.android.BizTracker;

import android.content.Context;
import android.database.Cursor;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.xiaolei.android.entity.StuffSchema;

/**
 * @author xiaolei
 * 
 */
public class StuffsPagerAdapter extends PagerAdapter {

	private int pageCount = 1;
	private Cursor mCursor;
	private Context mContext;
	private LayoutInflater mInflater;
	private OnClickListener mOnButtonClickListener;
	private final String TAG_PREFIX = "buttonStuff";
	protected int MAX_STUFF_COUNT = 9;
	protected int STUFF_COUNT_PER_ROW = 3;

	public StuffsPagerAdapter(Context context, Cursor stuffsCursor,
			OnClickListener onStuffClickListener) {
		mCursor = stuffsCursor;
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
		mOnButtonClickListener = onStuffClickListener;

		int totalCount = stuffsCursor.getCount();
		pageCount = totalCount / MAX_STUFF_COUNT
				+ (totalCount % MAX_STUFF_COUNT != 0 ? 1 : 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.view.PagerAdapter#destroyItem(android.view.View,
	 * int, java.lang.Object)
	 */
	@Override
	public void destroyItem(View collection, int position, Object view) {
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
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.view.PagerAdapter#getCount()
	 */
	@Override
	public int getCount() {
		return pageCount;
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

		View result = mInflater.inflate(R.layout.stuffs_buttons, pager, false);
		if (result != null) {
			if (mCursor.moveToPosition(position * MAX_STUFF_COUNT)) {
				int index = 0;
				do {
					Button btnStuff = (Button) result
							.findViewWithTag(TAG_PREFIX + (index + 1));
					if (btnStuff != null) {
						String stuffName = mCursor.getString(mCursor
								.getColumnIndex(StuffSchema.Name));
						int stuffId = mCursor.getInt(mCursor
								.getColumnIndex(StuffSchema.Id));

						btnStuff.setBackgroundResource(R.drawable.button);
						btnStuff.setText(stuffName);
						btnStuff.setTag(stuffId);

						if (mOnButtonClickListener != null) {
							btnStuff.setOnClickListener(mOnButtonClickListener);
						}
					}
					index++;
				} while (index <= MAX_STUFF_COUNT && mCursor.moveToNext());
			}
			pager.addView(result);
		}

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

}
