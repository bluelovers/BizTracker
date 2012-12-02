package com.xiaolei.android.customControl;

import android.R.color;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiaolei.android.BizTracker.R;
import com.xiaolei.android.common.Money;
import com.xiaolei.android.common.Utility;

public class CurrencyView extends LinearLayout {
	private TextView mTextViewExpense = null;
	private TextView mTextViewExpenseCurrencyCode = null;
	private TextView mTextViewIncome = null;
	private TextView mTextViewIncomeCurrencyCode = null;
	private Money[] mValue;
	private String mCurrencyCode = "";
	private int mDefaultColor = color.primary_text_light;

	public CurrencyView(Context context) {
		super(context);
		initLayout();
	}

	public CurrencyView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initLayout();
	}

	protected void initLayout() {
		View container = LayoutInflater.from(getContext()).inflate(
				R.layout.currency_view, this, true);

		mTextViewExpense = (TextView) container
				.findViewById(R.id.textViewExpense);
		mTextViewExpenseCurrencyCode = (TextView) container
				.findViewById(R.id.textViewExpenseCurrencyCode);
		mTextViewIncome = (TextView) container
				.findViewById(R.id.textViewIncome);
		mTextViewIncomeCurrencyCode = (TextView) container
				.findViewById(R.id.textViewIncomeCurrencyCode);
	}

	protected void updateLayout() {
		mTextViewExpenseCurrencyCode.setVisibility(TextView.GONE);
		mTextViewExpense.setText("*");
		mTextViewIncomeCurrencyCode.setVisibility(TextView.GONE);
		mTextViewIncome.setText("*");

		if (mValue != null && mValue.length == 2) {
			if (mValue[0].Value != 0) {
				mTextViewExpense.setText(Utility.formatCurrency(
						mValue[0].Value, mValue[0].CurrencyCode, false));
				mTextViewExpenseCurrencyCode.setText(mValue[0].CurrencyCode);
				mTextViewExpenseCurrencyCode.setVisibility(TextView.VISIBLE);
			} else {
				mTextViewExpense.setText("*");
				mTextViewExpenseCurrencyCode.setVisibility(TextView.GONE);
			}

			if (mValue[1].Value != 0) {
				mTextViewIncome.setText(Utility.formatCurrency(mValue[1].Value,
						mValue[1].CurrencyCode, false));
				mTextViewIncomeCurrencyCode.setText(mValue[1].CurrencyCode);
				mTextViewIncomeCurrencyCode.setVisibility(TextView.VISIBLE);
			} else {
				mTextViewIncome.setText("*");
				mTextViewIncomeCurrencyCode.setVisibility(TextView.GONE);
			}
		}

	}

	public Money[] getCost() {
		return mValue;
	}

	public String getCurrencyCode() {
		return mCurrencyCode;
	}

	public void setCost(Money[] values) {
		mValue = values;
		updateLayout();
	}

	public int getDefaultColor() {
		return mDefaultColor;
	}

	public void setDefaultColor(int color) {
		this.mDefaultColor = color;
	}
}
