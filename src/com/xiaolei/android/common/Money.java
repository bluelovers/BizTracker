package com.xiaolei.android.common;

public class Money {
	public double Value = 0d;
	public String CurrencyCode = "USD";

	public Money(double value, String currencyCode) {
		Value = value;
		CurrencyCode = currencyCode;
	}
}
