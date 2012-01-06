/**
 * 
 */
package com.xiaolei.android.listener;

import android.widget.ImageView;

import com.xiaolei.android.BizTracker.DailyTransactionListCursorAdapter;
import com.xiaolei.android.entity.BizLog;

/**
 * @author xiaolei
 * 
 */
public interface OnStarImageViewClickListener {
	void onStarImageViewClick(ImageView imageView, BizLog bizLog, DailyTransactionListCursorAdapter listViewAdapter);
}
