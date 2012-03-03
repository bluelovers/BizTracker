/**
 * 
 */
package com.xiaolei.android.ui;

import com.xiaolei.android.BizTracker.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author Lei Xiao
 *
 */
public class StatisticPanalFragment extends Fragment {
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (savedInstanceState != null) {
		
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View result = inflater.inflate(R.layout.statistic_panel_fragment, container,
				false);
		if (result != null) {
		
		}

		return result;
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

	}

	@Override
	public void onDestroy() {
		super.onDestroy();

	}

	@Override
	public void onDestroyView() {

		super.onDestroyView();
	}
}
