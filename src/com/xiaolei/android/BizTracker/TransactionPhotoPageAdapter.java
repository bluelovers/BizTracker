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
import android.widget.RelativeLayout;
import android.widget.TextView;

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
	private String photoFolerPath = "";

	public TransactionPhotoPageAdapter(Context context, Cursor cursor) {
		this.cursor = cursor;
		this.context = context;
		this.itemsSource = new Hashtable<Integer, TransactionPhoto>();

		if (cursor != null && cursor.isClosed() == false) {
			count = cursor.getCount();
		}

		inflater = LayoutInflater.from(context);

		photoFolerPath = Utility.getPhotoStoragePath();
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

	private Boolean fileExists(String fileName) {
		if (TextUtils.isEmpty(fileName)) {
			return false;
		}

		File file = new File(fileName);
		return file.exists();
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

		if (cursor != null && cursor.isClosed() == false
				&& cursor.moveToPosition(position)) {
			String fileName = cursor.getString(cursor
					.getColumnIndex(PhotoSchema.FileName));
			int id = cursor.getInt(cursor.getColumnIndex(PhotoSchema.Id));
			int transactionId = cursor.getInt(cursor
					.getColumnIndex(PhotoSchema.BizLogId));

			TransactionPhoto photoInfo = new TransactionPhoto();
			photoInfo.setId(id);
			photoInfo.setBizLogId(transactionId);

			itemsSource.put(position, photoInfo);

			Boolean fileExists = false;
			String finalFileName = fileName;

			if (fileExists(fileName)) {
				finalFileName = fileName;
				fileExists = true;
			} else {
				String fullFileName = photoFolerPath + File.separator
						+ fileName;
				if (fileExists(fullFileName)) {
					finalFileName = fullFileName;
					fileExists = true;
				}
			}

			final String fullFileName = finalFileName;
			photoInfo.setFileName(fullFileName);

			if (fileExists == true) {
				result = inflater.inflate(R.layout.photo_template, null);
				pager.addView(result);

				if (result != null) {
					final ImageViewEx ivPhoto = (ImageViewEx) result
							.findViewById(R.id.imageViewPhoto);
					if (ivPhoto != null) {

						ivPhoto.setOnSizeChangeListener(new OnSizeChangedListener() {

							@Override
							public void onSizeChanged(int width, int height) {
								if (width > 0 && height > 0) {
									Bitmap bitmap = Utility.getScaledBitmap(
											fullFileName,
											ivPhoto.getMeasuredWidth(),
											ivPhoto.getMeasuredHeight());
									if (bitmap != null) {
										ivPhoto.setImageBitmap(bitmap);
									} else {
										RelativeLayout parentView = (RelativeLayout) ivPhoto
												.getParent();
										if (parentView != null) {
											TextView tvFailLoadPhoto = (TextView) parentView
													.findViewById(R.id.textViewFailLoadPhoto);
											if (tvFailLoadPhoto != null) {
												tvFailLoadPhoto
														.setVisibility(TextView.VISIBLE);
											}
										}
									}
								}
							}
						});

					}
				}
			} else {
				result = inflater.inflate(R.layout.transaction_photo_not_exist,
						null);
				pager.addView(result);
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
