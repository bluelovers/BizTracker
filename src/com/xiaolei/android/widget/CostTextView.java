package com.xiaolei.android.widget;

import com.xiaolei.android.BizTracker.R;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.TextView;

public class CostTextView extends TextView {
	private boolean mIsHintText = false;
	private int mHintTextColor = Color.WHITE;
	private int mDefaultTextColor = Color.WHITE;

	public CostTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public CostTextView(Context context) {
		super(context);
		init();
	}

	public CostTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		mDefaultTextColor = getContext().getResources().getColor(
				R.color.lightGreen);
		mHintTextColor = this.getTextColors().getDefaultColor();
		this.setTextColor(mDefaultTextColor);
	}

	public boolean IsHintText() {
		return mIsHintText;
	}

	public void setIsHintText(boolean isHintText) {
		mIsHintText = isHintText;
		if (isHintText) {
			this.setTextColor(mHintTextColor);
		} else {
			this.setTextColor(mDefaultTextColor);
		}
	}

	public void setCost(double cost, boolean isHintText) {
		setCost(String.valueOf(cost), isHintText);
	}
	public void setCost(String costText, boolean isHintText){
		setIsHintText(isHintText);
		this.setText(costText);
	}
}
