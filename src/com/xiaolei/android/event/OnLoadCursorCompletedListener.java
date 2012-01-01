/**
 * 
 */
package com.xiaolei.android.event;

import android.database.Cursor;
import android.view.View;

/**
 * @author xiaolei
 * 
 */
public interface OnLoadCursorCompletedListener {
	void onLoadCursorCompleted(View sender, Cursor result);
}
