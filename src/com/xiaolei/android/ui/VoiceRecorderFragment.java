package com.xiaolei.android.ui;

import com.xiaolei.android.BizTracker.R;
import com.xiaolei.android.media.AudioRecorder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class VoiceRecorderFragment extends Fragment implements OnClickListener {
	private AudioRecorder mRecorder;
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
		View result = inflater.inflate(R.layout.voice_recorder_fragment,
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
		case R.id.relativeLayoutRecorderPanel:
			if (mRecorder == null || !mRecorder.isRecording()) {
				start();
				showRecorderTips("Touch to stop recording");
			} else {
				stop();
				showRecorderTips("Touch to start recording");
			}
			break;
		default:
			break;
		}
	}

	private void showRecorderTips(String message) {
		TextView tv = (TextView) getView().findViewById(
				R.id.textViewRecorderTips);
		if (tv != null) {
			tv.setText(message);
		}
	}

	public void start() {
		if (mRecorder == null) {
			mRecorder = new AudioRecorder(getActivity());
		}

		mRecorder.start();
	}

	public void stop() {
		if (mRecorder != null) {
			mRecorder.stop();
			mRecorder = null;
		}
	}
}
