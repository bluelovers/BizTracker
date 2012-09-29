package com.xiaolei.android.ui;

import com.xiaolei.android.BizTracker.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class StuffAnalysisFragment extends Fragment {
	private static final String STUFF_ID = "STUFF_ID";
	private int mStuffId = -1;
	private ViewHolder mViewHolder;

	public static StuffAnalysisFragment newInstance(int stuffId) {
		StuffAnalysisFragment result = new StuffAnalysisFragment();
		Bundle args = new Bundle();
		args.putInt(STUFF_ID, stuffId);
		result.setArguments(args);

		return result;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (getArguments() != null) {
			mStuffId = getArguments().getInt(STUFF_ID);
		}

		View result = inflater.inflate(R.layout.stuff_analysis_fragment, container,
				false);
		if (result != null) {

			
		}

		return result;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		loadDataAsync();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}
	
	private void loadDataAsync() {
		
	}
	
	private static final class ViewHolder{
		 
	}
}
