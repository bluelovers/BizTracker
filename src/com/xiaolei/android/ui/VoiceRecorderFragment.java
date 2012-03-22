package com.xiaolei.android.ui;

import java.io.File;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiaolei.android.BizTracker.R;
import com.xiaolei.android.common.TimeSpan;
import com.xiaolei.android.common.Utility;
import com.xiaolei.android.entity.VoiceNote;
import com.xiaolei.android.listener.OnVoiceNoteCreatedListener;
import com.xiaolei.android.media.AudioRecorder;
import com.xiaolei.android.service.DataService;

public class VoiceRecorderFragment extends DialogFragment implements
		OnClickListener {
	private AudioRecorder mRecorder;
	private Timer mTimer;
	private Date mStartTime = new Date();
	private Date mStopTime = new Date();
	private long mTransactionId;
	private OnVoiceNoteCreatedListener mOnVoiceNoteCreatedListener;
	private String mOutputFileName = "";
	private Handler mHandler;
	private View mContent;

	public void setOnVoiceNoteCreatedListener(
			OnVoiceNoteCreatedListener listener) {
		mOnVoiceNoteCreatedListener = listener;
	}

	protected void onVoiceNoteCreated(String fileName) {
		if (mOnVoiceNoteCreatedListener != null) {
			mOnVoiceNoteCreatedListener.onVoiceNoteCreated(fileName);
		}
	}

	public static VoiceRecorderFragment newInstance(String title,
			long transactionId) {
		VoiceRecorderFragment frag = new VoiceRecorderFragment();
		Bundle args = new Bundle();
		args.putString("title", title);
		args.putLong("transactionId", transactionId);
		frag.setArguments(args);
		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mHandler = new Handler();

		/*
		 * // mNum = getArguments().getInt("num"); int style =
		 * DialogFragment.STYLE_NORMAL, theme = 0; style =
		 * DialogFragment.STYLE_NO_TITLE; style = DialogFragment.STYLE_NO_FRAME;
		 * style = DialogFragment.STYLE_NO_INPUT; style =
		 * DialogFragment.STYLE_NORMAL; style = DialogFragment.STYLE_NORMAL;
		 * style = DialogFragment.STYLE_NO_TITLE; style =
		 * DialogFragment.STYLE_NO_FRAME; style = DialogFragment.STYLE_NORMAL;
		 * 
		 * setStyle(style, theme);
		 */
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		String title = getArguments().getString("title");
		mTransactionId = getArguments().getLong("transactionId");

		mContent = getActivity().getLayoutInflater().inflate(
				R.layout.voice_recorder_fragment, null, false);
		if (mContent != null) {
			RelativeLayout layout = (RelativeLayout) mContent
					.findViewById(R.id.relativeLayoutRecorderPanel);
			if (layout != null) {
				layout.setOnClickListener(this);
			}
		}

		return new AlertDialog.Builder(getActivity())
				.setIcon(android.R.drawable.ic_dialog_info)
				.setTitle(title)
				.setView(mContent)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								stop(true);
								onVoiceNoteCreated(mOutputFileName);
							}
						})
				.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								try {
									if (Utility.fileExists(mOutputFileName)) {
										File file = new File(mOutputFileName);
										file.delete();
									}
								} catch (Exception ex) {
									// Do nothing.
								}
							}
						}).create();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (savedInstanceState != null) {
			// Serializable dataset = savedInstanceState
			// .getSerializable("dataset");

		}

		mTimer = new Timer();
		mTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						TimeSpan durationTime = new TimeSpan(System
								.currentTimeMillis() - mStartTime.getTime());
						showDurationTime(durationTime.toString());
					}
				});
			}
		}, 0, 1000);

		start();
	}

	private void runOnUiThread(Runnable action) {
		mHandler.post(action);
	}

	/*
	 * @Override public View onCreateView(LayoutInflater inflater, ViewGroup
	 * container, Bundle savedInstanceState) { View result =
	 * inflater.inflate(R.layout.voice_recorder_fragment, container, false); if
	 * (result != null) { RelativeLayout layout = (RelativeLayout) result
	 * .findViewById(R.id.relativeLayoutRecorderPanel); if (layout != null) {
	 * layout.setOnClickListener(this); } }
	 * 
	 * return result; }
	 */

	@Override
	public void onPause() {
		stop(false);
		super.onPause();
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
		stop(false);
		super.onDestroy();
	}

	@Override
	public void onDestroyView() {

		super.onDestroyView();
	}

	@Override
	public void onClick(View v) {

	}

	@SuppressWarnings("unused")
	private void showRecorderTips(String message) {
		if (mContent != null) {
			TextView tv = (TextView) mContent
					.findViewById(R.id.textViewRecorderTips);
			if (tv != null) {
				tv.setText(message);
			}
		}
	}

	private void showDurationTime(String message) {
		if (mContent != null) {
			TextView tv = (TextView) mContent
					.findViewById(R.id.textViewRecorderDuration);
			if (tv != null) {
				tv.setText(message);
			}
		}
	}

	public void start() {
		if (mRecorder == null) {
			mRecorder = new AudioRecorder(getActivity());
		}

		mRecorder.generateNewFileName();
		mStartTime = new Date();
		mRecorder.start();
	}

	public void stop(boolean save) {
		if (mTimer != null) {
			try {
				mTimer.cancel();
			} catch (Exception ex) {
				// Do nothing
			}
		}

		if (mRecorder != null) {
			mStopTime = new Date();
			mOutputFileName = mRecorder.getOutputFileName();
			if (save) {
				addVoiceNoteAsync();
			}
			mRecorder.stop();
			mRecorder = null;
		}
	}

	private void addVoiceNoteAsync() {
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				long duration = mStopTime.getTime() - mStartTime.getTime();
				VoiceNote note = new VoiceNote();
				note.setDuration(duration);
				note.setTransactionId(mTransactionId);
				note.setFileName(Utility
						.getFileNameWithoutPath(mOutputFileName));

				DataService.GetInstance(getActivity()).addVoiceNote(note);

				return null;
			}

		};
		task.execute();
	}
}
