/**
 * 
 */
package com.xiaolei.android.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;
import com.xiaolei.android.BizTracker.R;

/**
 * @author xiaolei
 * 
 */
public class CurrencySettings extends FragmentActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.exchange_rate_list);
	}
}
