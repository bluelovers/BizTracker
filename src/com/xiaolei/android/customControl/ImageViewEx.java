/**
 * 
 */
package com.xiaolei.android.customControl;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.xiaolei.android.listener.OnSizeChangedListener;

/**
 * @author xiaolei
 * 
 */
public class ImageViewEx extends ImageView {
	private OnSizeChangedListener onSizeChangeListener;

	public ImageViewEx(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

	}

	public ImageViewEx(Context context, AttributeSet attrs) {
		super(context, attrs);

	}

	public ImageViewEx(Context context) {
		super(context);

	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		if (onSizeChangeListener != null) {
			onSizeChangeListener.onSizeChanged(w, h);
		}
	}

	public OnSizeChangedListener getOnSizeChangeListener() {
		return onSizeChangeListener;
	}

	public void setOnSizeChangeListener(
			OnSizeChangedListener onSizeChangeListener) {
		this.onSizeChangeListener = onSizeChangeListener;
	}

}
