/**
 * 
 */
package com.xiaolei.android.common;

import android.content.Context;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.xiaolei.android.BizTracker.R;

/**
 * @author Lei Xiao
 * 
 */
public class PopupWindowHelper {
	private PopupWindow mPopupWindow = null;
	private int mResourceId = 0;
	private View mContentView = null;
	private Context mContext = null;
	private View mParentView = null;
	private int mAutoDismissDelayMillis = 5000;
	private int mExpectedWidth = 200;
	private int mExpectedHeight = 200;

	public PopupWindowHelper(Context context, View parentView) {
		mResourceId = R.layout.pop_up_content;
		mParentView = parentView;
		mContext = context;
	}

	public PopupWindowHelper(Context context, View parentView, int width,
			int height) {
		this(context, parentView);

		mExpectedWidth = width;
		mExpectedHeight = height;
	}

	public void showPopupMessage(String message, int textColor) {
		if (mContentView == null) {
			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mContentView = inflater.inflate(mResourceId, null, false);
		}

		if (mPopupWindow == null) {
			mPopupWindow = new PopupWindow(mContentView, mExpectedWidth,
					mExpectedHeight, false);
			mPopupWindow.setAnimationStyle(R.style.AnimationPopup);
			mPopupWindow.setOutsideTouchable(true);
			mPopupWindow.setClippingEnabled(true);
		}

		if (mPopupWindow != null && mContentView != null) {
			TextView tvPopupMessage = (TextView) mContentView
					.findViewById(R.id.textViewPopupMessage);
			if (tvPopupMessage != null) {
				tvPopupMessage.setTextColor(textColor);
				tvPopupMessage.setText(message);
			}
			mPopupWindow.showAtLocation(mParentView, Gravity.CENTER, 0, 0);
			
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {

				@Override
				public void run() {
					if (mPopupWindow != null) {
						mPopupWindow.dismiss();
					}
				}
			}, mAutoDismissDelayMillis);
		}
	}
}
