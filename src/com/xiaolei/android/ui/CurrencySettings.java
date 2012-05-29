/**
 * 
 */
package com.xiaolei.android.ui;

import com.xiaolei.android.BizTracker.R;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;

/**
 * @author xiaolei
 * 
 */
public class CurrencySettings extends FragmentActivity implements
		OnClickListener {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.exchange_rate_list);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.buttonUpdateExchangRateOnline:
			FragmentManager fragmentManager = this.getSupportFragmentManager();
			if (fragmentManager != null) {
				ExchangeRateListFragment fragment = (ExchangeRateListFragment) fragmentManager
						.findFragmentById(R.id.fragmentExchangeRateList);
				if (fragment != null) {
					fragment.updateAllExchangeRateOnlineAsync();
				}
			}
			break;
		default:
			break;
		}
	}
}
