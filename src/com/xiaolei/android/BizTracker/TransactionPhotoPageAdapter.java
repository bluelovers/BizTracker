/**
 * 
 */
package com.xiaolei.android.BizTracker;

import java.io.File;
import java.util.Hashtable;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import com.xiaolei.android.common.Utility;
import com.xiaolei.android.customControl.ImageViewEx;
import com.xiaolei.android.entity.PhotoSchema;
import com.xiaolei.android.entity.TransactionPhoto;
import com.xiaolei.android.listener.OnSizeChangedListener;

/**
 * @author xiaolei
 * 
 */
public class TransactionPhotoPageAdapter extends PagerAdapter {

	private Cursor cursor;
	private int count = 0;
	private LayoutInflater inflater;
	@SuppressWarnings("unused")
	private Context context;
	private Hashtable<Integer, TransactionPhoto> itemsSource;

	public TransactionPhotoPageAdapter(Context context, Cursor cursor) {
		this.cursor = cursor;
		this.context = context;
		this.itemsSource = new Hashtable<Integer, TransactionPhoto>();

		if (cursor != null && cursor.isClosed() == false) {
			count = cursor.getCount();
		}

		inflater = LayoutInflater.from(context);
	}

	public TransactionPhoto GetItemSource(int position) {
		if (itemsSource.containsKey(position)) {
			return itemsSource.get(position);
		} else {
			return null;
		}
	}

	public void changeCursor(Cursor newCursor) {
		if (this.cursor != null && this.cursor.isClosed() != true) {
			this.cursor.close();
		}

		this.cursor = newCursor;
		if (cursor != null && cursor.isClosed() == false) {
			count = cursor.getCount();

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
		if (itemsSource.containsKey(position)) {
			itemsSource.remove(position);
		}
		if (view != null) {
			((ViewPager) collection).removeView((View) view);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.view.PagerAdapter#finishUpdate(android.view.View)
	 */
	@Override
	public void finishUpdate(View arg0) {
		// TODO Auto-generated method stub

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
		View result = null;
		ViewPager pager = (ViewPager) collection;
		if (pager == null) {
			return null;
		}

		if (cursor != null && cursor.moveToPosition(position)) {
			final String fileName = cursor.getString(cursor
					.getColumnIndex(PhotoSchema.FileName));
			if (TextUtils.isEmpty(fileName)) {
				return null;
			}
			File file = new File(fileName);
			if (file.exists() == false) {
				return null;
			}

			result = inflater.inflate(R.layout.photo_template, null);
			pager.addView(result);

			if (result != null) {
				final ImageViewEx ivPhoto = (ImageViewEx) result
						.findViewById(R.id.imageViewPhoto);
				if (ivPhoto != null) {

					int id = cursor.getInt(cursor
							.getColumnIndex(PhotoSchema.Id));
					int transactionId = cursor.getInt(cursor
							.getColumnIndex(PhotoSchema.BizLogId));

					TransactionPhoto photoInfo = new TransactionPhoto();
					photoInfo.setFileName(fileName);
					photoInfo.setId(id);
					photoInfo.setBizLogId(transactionId);

					itemsSource.put(position, photoInfo);

					ivPhoto.setOnSizeChangeListener(new OnSizeChangedListener() {

						@Override
						public void onSizeChanged(int width, int height) {
							if (width > 0 && height > 0) {
								Bitmap bitmap = Utility.getScaledBitmap(
										fileName, ivPhoto.getMeasuredWidth(),
										ivPhoto.getMeasuredHeight());
								if (bitmap != null) {
									ivPhoto.setImageBitmap(bitmap);
								}
							}
						}
					});

				}
			}
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
	public boolean isViewFromObject(View arg0, Object arg1) {
		// TODO Auto-generated method stub
		return arg0 == arg1;
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
