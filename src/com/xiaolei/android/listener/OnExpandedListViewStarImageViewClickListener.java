/**
 * 
 */
package com.xiaolei.android.listener;

import android.widget.ImageView;

import com.xiaolei.android.BizTracker.TransactionListCursorTreeAdapter;
import com.xiaolei.android.entity.BizLog;

/**
 * @author xiaolei
 *
 */
public interface OnExpandedListViewStarImageViewClickListener {
	void onStarImageViewClick(ImageView imageView, BizLog bizLog, TransactionListCursorTreeAdapter listViewAdapter);
}
