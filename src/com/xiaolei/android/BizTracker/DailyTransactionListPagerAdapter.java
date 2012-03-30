/**
 * 
 */
package com.xiaolei.android.BizTracker;

import java.util.Date;
import android.app.Activity;
import android.database.Cursor;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ViewSwitcher;

import com.xiaolei.android.common.Utility;
import com.xiaolei.android.entity.BizLog;
import com.xiaolei.android.event.OnGotResultListener;
import com.xiaolei.android.event.OnLoadCursorCompletedListener;
import com.xiaolei.android.listener.OnCostValueChangedListener;
import com.xiaolei.android.listener.OnStarImageViewClickListener;
import com.xiaolei.android.service.DataService;

/**
 * @author xiaolei
 * 
 */
public class DailyTransactionListPagerAdapter extends PagerAdapter {
	private Date date = new Date();
	private Activity context;
	private int count = 365;
	private LayoutInflater inflater;
	private OnCostValueChangedListener onCostValueChangedListener;
	private OnItemClickListener onItemClickListener;

	private SparseArray<View> viewPositionMapping = new SparseArray<View>();

	public DailyTransactionListPagerAdapter(Activity context, Date date, int pageCount) {
		this.context = context;
		this.date = date;
		this.inflater = LayoutInflater.from(context);
		this.count = pageCount;
	}

	public void addCostValueChangedListener(OnCostValueChangedListener listener) {
		if (listener != null) {
			onCostValueChangedListener = listener;
		}
	}

	public void removeCostValueChangedListener() {
		onCostValueChangedListener = null;
	}

	public View getViewAtPosition(int position) {
		View result = viewPositionMapping.get(position);

		return result;
	}

	public void reloadView(int position) {
		View view = getViewAtPosition(position);
		if (view != null) {
			ListView lv = (ListView) view
					.findViewById(R.id.listViewBizLogByDay);
			if (lv != null) {
				DailyTransactionListCursorAdapter adapter = (DailyTransactionListCursorAdapter) lv
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
		if (viewPositionMapping.get(position) != null) {
			viewPositionMapping.remove(position);
		}

		View tobeRemovedView = (View) view;
		if (tobeRemovedView != null) {
			ListView lv = (ListView) tobeRemovedView
					.findViewById(R.id.listViewBizLogByDay);
			if (lv != null) {
				CursorAdapter adpt = (CursorAdapter) lv.getAdapter();
				if (adpt != null) {
					Cursor cursor = adpt.getCursor();
					if (cursor != null && !cursor.isClosed()) {
						cursor.close();
					}
				}
			}
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
		View result = inflater.inflate(R.layout.daily_log_without_title, pager,
				false);
		if (viewPositionMapping.get(position) == null) {
			viewPositionMapping.put(position, result);
		}
		pager.addView(result);

		Date currentDate = null;
		if (date != null) {
			currentDate = Utility.addDays(date, position + 1);
		}

		ListView lv = (ListView) result.findViewById(R.id.listViewBizLogByDay);
		if (lv != null && onItemClickListener != null) {
			lv.setOnItemClickListener(onItemClickListener);
		}

		DailyTransactionListCursorAdapter adpt = new DailyTransactionListCursorAdapter(context, result, lv,
				currentDate, new OnLoadCursorCompletedListener() {

					@Override
					public void onLoadCursorCompleted(View sender, Cursor result) {
						if (sender != null && result != null) {
							if (result.getCount() > 0) {
								ViewSwitcher viewSwitcher = (ViewSwitcher) sender
										.findViewById(R.id.viewSwitcherDayBizLog);
								if (viewSwitcher.getDisplayedChild() != 0) {
									viewSwitcher.showNext();
								}

								ViewSwitcher viewSwitcherContent = (ViewSwitcher) sender
										.findViewById(R.id.viewSwitcherContent);
								viewSwitcherContent.setDisplayedChild(1);
							} else {
								ViewSwitcher viewSwitcher = (ViewSwitcher) sender
										.findViewById(R.id.viewSwitcherDayBizLog);
								viewSwitcher.setDisplayedChild(1);
							}
						}
					}

				}, new OnStarImageViewClickListener() {

					@Override
					public void onStarImageViewClick(ImageView imageView,
							BizLog bizLog, DailyTransactionListCursorAdapter listViewAdapter) {
						if (bizLog != null && listViewAdapter != null) {
							if (bizLog.getStar() == true) {
								DataService.GetInstance(context).removeStar(
										bizLog.getId());
							} else {
								DataService.GetInstance(context).addStar(
										bizLog.getId());
							}
							listViewAdapter.loadDataAsync();
						}
					}

				});

		lv.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View item,
					int position, long _id) {
				BizLog instance = (BizLog) item.getTag();
				final ListView listView = (ListView) parent;

				if (instance != null && listView != null) {
					Helper.showBizLogItemOptionsMenu(context, instance,
							new OnGotResultListener() {

								@Override
								public void onGotResult(Boolean result,
										Boolean effectCostValue) {
									if (result == true) {
										// Reload ListView
										DailyTransactionListCursorAdapter adapter = (DailyTransactionListCursorAdapter) listView
												.getAdapter();
										if (adapter != null) {
											adapter.loadDataAsync();
										}

										// If operations effect the cost value,
										// notify another views to reload
										// themselves.
										if (effectCostValue == true
												&& onCostValueChangedListener != null) {
											onCostValueChangedListener
													.OnCostValueChanged();
										}
									}
								}

							});
				}

				return true;
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
