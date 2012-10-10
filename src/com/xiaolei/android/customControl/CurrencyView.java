package com.xiaolei.android.customControl;

import com.xiaolei.android.BizTracker.R;
import com.xiaolei.android.common.Utility;

import android.R.color;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CurrencyView extends LinearLayout {
	private TextView mTextViewCost = null;
	private TextView mTextViewCurrencyCode = null;
	private double mCost = 0;
	private String mCurrencyCode = "";
	private int mDefaultColor = color.primary_text_light;
	private int mIncomeColor = mDefaultColor;
	private int mExpenseColor = mDefaultColor;

	public CurrencyView(Context context) {
		super(context);
		initLayout();
	}

	public CurrencyView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initLayout();
	}

	protected void initLayout() {
		this.setOrientation(HORIZONTAL);
		mTextViewCost = new TextView(this.getContext());
		mTextViewCurrencyCode = new TextView(this.getContext());

		LinearLayout.LayoutParams paramsCost = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		LinearLayout.LayoutParams paramsCurrencyCode = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		paramsCurrencyCode.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
		paramsCost.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
		paramsCurrencyCode.leftMargin = 4;
		mTextViewCurrencyCode.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12); // 12sp
		mTextViewCurrencyCode.setGravity(Gravity.CENTER);
		mTextViewCost.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);

		mTextViewCost.setLayoutParams(paramsCost);
		mTextViewCurrencyCode.setLayoutParams(paramsCurrencyCode);
		this.addView(mTextViewCost);
		this.addView(mTextViewCurrencyCode);

		mIncomeColor = getContext().getResources()
				.getColor(R.color.incomeColor);
		mExpenseColor = getContext().getResources().getColor(
				R.color.expenseColor);
	}

	protected void updateLayout() {
		if ((long)mCost != 0) {
			mTextViewCost.setText(Utility.formatCurrency(mCost, mCurrencyCode,
					false));
			mTextViewCurrencyCode.setText(mCurrencyCode);

			if (mCost > 0) {
				mTextViewCost.setTextColor(mIncomeColor);
				mTextViewCurrencyCode.setTextColor(mIncomeColor);
				mTextViewCurrencyCode
						.setBackgroundResource(R.drawable.radius_corner_bg_earn);
			} else {
				mTextViewCost.setTextColor(mExpenseColor);
				mTextViewCurrencyCode.setTextColor(mExpenseColor);
				mTextViewCurrencyCode
						.setBackgroundResource(R.drawable.radius_corner_bg_pay);
			}
		} else {
			mTextViewCost.setText("*");
			mTextViewCost.setTextColor(mDefaultColor);
		}
	}

	public double getCost() {
		return mCost;
	}

	public String getCurrencyCode() {
		return mCurrencyCode;
	}

	public void setCost(double cost, String currencyCode) {
		this.mCost = cost;
		this.mCurrencyCode = currencyCode;
		updateLayout();
	}

	public int getDefaultColor() {
		return mDefaultColor;
	}

	public void setDefaultColor(int color) {
		this.mDefaultColor = color;
	}
}
