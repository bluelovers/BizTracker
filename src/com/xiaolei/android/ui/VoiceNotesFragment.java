package com.xiaolei.android.ui;

import com.xiaolei.android.BizTracker.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class VoiceNotesFragment extends Fragment implements OnClickListener {

	private long mTransactionId = 0;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (savedInstanceState != null) {
			// Serializable dataset = savedInstanceState
			// .getSerializable("dataset");

		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View result = inflater.inflate(R.layout.voice_notes_fragment,
				container, false);
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

		// outState.putSerializable("dataset", mDataset);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

	}

	@Override
	public void onDestroyView() {

		super.onDestroyView();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		}
	}

	public void showVoiceNotes(long transactionId) {
		if (transactionId != mTransactionId) {
			mTransactionId = transactionId;

		}
	}
}
