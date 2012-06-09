package com.xiaolei.android.ui;

import java.util.Timer;
import java.util.TimerTask;

import android.content.DialogInterface;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.xiaolei.android.BizTracker.R;
import com.xiaolei.android.BizTracker.VoiceNotesCursorAdapter;
import com.xiaolei.android.common.Utility;
import com.xiaolei.android.entity.VoiceNoteSchema;
import com.xiaolei.android.listener.OnVoiceNoteCreatedListener;
import com.xiaolei.android.media.MediaPlayerStatus;
import com.xiaolei.android.service.DataService;

public class VoiceNotesFragment extends Fragment implements OnClickListener,
		OnVoiceNoteCreatedListener, OnItemClickListener, OnPreparedListener,
		OnCompletionListener, OnErrorListener, OnBufferingUpdateListener {

	public static final String FRAGMENT_VOICE_NOTE_RECORDER_DIALOG = "fragmentVoiceNoteRecorderDialog";
	private long mTransactionId = 0;
	private String mCurrentAudioFileName;
	private MediaPlayer mPlayer;
	private Cursor mVoiceNoteListCursor = null;
	private Handler mHandler;
	private VoiceNotesFragment mSelf;
	private Timer mTimer;
	private ProgressBar mPlayPosition;
	private long mSelectedVoiceNoteId = 0;
	private VoiceNotesCursorAdapter adapter;

	public static VoiceNotesFragment newInstance(long transactionId) {
		VoiceNotesFragment result = new VoiceNotesFragment();
		Bundle args = new Bundle();
		args.putLong("transactionId", transactionId);
		result.setArguments(args);

		return result;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (savedInstanceState != null) {
			// Serializable dataset = savedInstanceState
			// .getSerializable("dataset");

		}

		loadDataAsync();
	}

	private void loadDataAsync() {
		if (getView() != null) {
			ViewFlipper viewFlipper = (ViewFlipper) getView().findViewById(
					R.id.viewFlipperVoiceNoteList);
			if (viewFlipper != null) {
				viewFlipper.setDisplayedChild(0);
			}
		}

		AsyncTask<Void, Void, Cursor> task = new AsyncTask<Void, Void, Cursor>() {

			@Override
			protected Cursor doInBackground(Void... params) {
				mVoiceNoteListCursor = DataService.GetInstance(getActivity())
						.getAllVoiceNotes(mTransactionId);

				return mVoiceNoteListCursor;
			}

			@Override
			protected void onPostExecute(Cursor result) {
				if (getView() != null) {
					ListView lv = (ListView) getView().findViewById(
							R.id.listViewAudioList);

					if (result == null || result.getCount() == 0) {
						showOrHidePlayControlPanel(false);
					}

					if (lv != null) {
						if (lv.getAdapter() != null) {
							adapter = (VoiceNotesCursorAdapter) lv.getAdapter();
							if (adapter != null) {
								adapter.changeCursor(result);
							}
						} else {
							VoiceNotesCursorAdapter adapter = new VoiceNotesCursorAdapter(
									getActivity(), result);
							adapter.setOnButtonClickListener(mSelf);
							lv.setAdapter(adapter);
						}
					}

					ViewFlipper viewFlipper = (ViewFlipper) getView()
							.findViewById(R.id.viewFlipperVoiceNoteList);
					if (viewFlipper != null) {
						if (result.getCount() > 0) {
							viewFlipper.setDisplayedChild(1);
						} else {
							TextView tvEmpty = (TextView) getView()
									.findViewById(R.id.textViewEmpty);
							if (tvEmpty != null) {
								tvEmpty.setText(getActivity().getString(
										R.string.no_voice_notes));
							}
							viewFlipper.setDisplayedChild(2);
						}
					}
				}
			}
		};
		task.execute();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (getArguments() != null) {
			mTransactionId = getArguments().getLong("transactionId");
		}

		mSelf = this;
		mHandler = new Handler();

		View result = inflater.inflate(R.layout.voice_notes_fragment,
				container, false);
		if (result != null) {
			RelativeLayout newRecordLayout = (RelativeLayout) result
					.findViewById(R.id.relativeLayoutRecordAudio);
			if (newRecordLayout != null) {
				newRecordLayout.setOnClickListener(this);
			}

			ListView lv = (ListView) result
					.findViewById(R.id.listViewAudioList);
			if (lv != null) {
				lv.setOnItemClickListener(this);
			}

			ImageButton btnPlay = (ImageButton) result
					.findViewById(R.id.imageButtonPlay);
			ImageButton btnPrevious = (ImageButton) result
					.findViewById(R.id.imageButtonPrevious);
			ImageButton btnNext = (ImageButton) result
					.findViewById(R.id.imageButtonNext);
			if (btnPlay != null) {
				btnPlay.setOnClickListener(this);
			}

			if (btnPrevious != null) {
				btnPrevious.setOnClickListener(this);
			}

			if (btnNext != null) {
				btnNext.setOnClickListener(this);
			}

			mPlayPosition = (ProgressBar) result
					.findViewById(R.id.progressBarAudioDuration);
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

		if (mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
		}

		if (mVoiceNoteListCursor != null && !mVoiceNoteListCursor.isClosed()) {
			mVoiceNoteListCursor.close();
			mVoiceNoteListCursor = null;
		}

		if (mTimer != null) {
			try {
				mTimer.cancel();
			} catch (Exception ex) {
				// Do nothing
			}
		}
	}

	@Override
	public void onDestroyView() {

		super.onDestroyView();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.relativeLayoutRecordAudio:
			VoiceRecorderFragment recorderFragment = VoiceRecorderFragment
					.newInstance(
							getActivity().getString(R.string.recorder_title),
							mTransactionId);
			recorderFragment.setOnVoiceNoteCreatedListener(this);
			recorderFragment.show(getActivity().getSupportFragmentManager(),
					FRAGMENT_VOICE_NOTE_RECORDER_DIALOG);
			break;
		case R.id.imageButtonPlay:
			play(mCurrentAudioFileName, false);
			break;
		case R.id.imageButtonPrevious:
			playPrevious();
			break;
		case R.id.imageButtonNext:
			playNext();
			break;
		case R.id.imageViewDeleteVoiceNote:
			Object tag = v.getTag();
			if (tag != null) {
				mSelectedVoiceNoteId = Long.parseLong(tag.toString());
				stop();

				Utility.showConfirmDialog(getActivity(), getActivity()
						.getString(R.string.confirm),
						getActivity().getString(R.string.delete_confirm),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								deleteVoiceNoteAsync(mSelectedVoiceNoteId);
							}
						});
			}
			break;
		default:
			break;
		}
	}

	private void deleteVoiceNoteAsync(long id) {
		AsyncTask<Long, Void, Boolean> task = new AsyncTask<Long, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(Long... params) {
				DataService.GetInstance(getActivity()).deleteVoiceNote(
						params[0]);
				return true;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				if (result == true) {
					mSelectedVoiceNoteId = 0;
					loadDataAsync();
				}
			}

		};
		task.execute(id);
	}

	private void setSelectedVoiceNoteId(long id) {
		mSelectedVoiceNoteId = id;
		if (adapter != null && mSelectedVoiceNoteId > 0) {
			adapter.setActiveVoiceNoteId(id);
		}
	}

	private void playNext() {
		if (mVoiceNoteListCursor != null && !mVoiceNoteListCursor.isClosed()) {
			if (mSelectedVoiceNoteId > 0) {
				if (mVoiceNoteListCursor.moveToFirst()) {
					do {
						long id = mVoiceNoteListCursor
								.getLong(mVoiceNoteListCursor
										.getColumnIndex(VoiceNoteSchema.Id));
						if (id == mSelectedVoiceNoteId) {
							if (mVoiceNoteListCursor.moveToNext()) {
								mSelectedVoiceNoteId = mVoiceNoteListCursor
										.getLong(mVoiceNoteListCursor
												.getColumnIndex(VoiceNoteSchema.Id));
								setSelectedVoiceNoteId(id);
								String fileName = mVoiceNoteListCursor
										.getString(mVoiceNoteListCursor
												.getColumnIndex(VoiceNoteSchema.FileName));
								fileName = Utility.getAudioFullFileName(getActivity(),
										fileName);
								play(fileName, true);
								
								break;
							}
						}
					} while (mVoiceNoteListCursor.moveToNext());
				}
			} else {
				if (mVoiceNoteListCursor.moveToLast()) {
					String fileName = mVoiceNoteListCursor
							.getString(mVoiceNoteListCursor
									.getColumnIndex(VoiceNoteSchema.FileName));
					fileName = Utility.getAudioFullFileName(getActivity(),
							fileName);
					play(fileName, true);
				}
			}
		}
	}

	private void playPrevious() {
		if (mVoiceNoteListCursor != null && !mVoiceNoteListCursor.isClosed()) {
			if (mSelectedVoiceNoteId > 0) {
				if (mVoiceNoteListCursor.moveToFirst()) {
					do {
						long id = mVoiceNoteListCursor
								.getLong(mVoiceNoteListCursor
										.getColumnIndex(VoiceNoteSchema.Id));
						if (id == mSelectedVoiceNoteId) {
							if (mVoiceNoteListCursor.moveToPrevious()) {
								mSelectedVoiceNoteId = mVoiceNoteListCursor
										.getLong(mVoiceNoteListCursor
												.getColumnIndex(VoiceNoteSchema.Id));
								setSelectedVoiceNoteId(id);
								String fileName = mVoiceNoteListCursor
										.getString(mVoiceNoteListCursor
												.getColumnIndex(VoiceNoteSchema.FileName));
								fileName = Utility.getAudioFullFileName(getActivity(),
										fileName);
								play(fileName, true);
								
								break;
							}
						}
					} while (mVoiceNoteListCursor.moveToNext());
				}
			} else {
				if (mVoiceNoteListCursor.moveToFirst()) {
					String fileName = mVoiceNoteListCursor
							.getString(mVoiceNoteListCursor
									.getColumnIndex(VoiceNoteSchema.FileName));
					fileName = Utility.getAudioFullFileName(getActivity(),
							fileName);
					play(fileName, true);
				}
			}
		}
	}

	@Override
	public void onVoiceNoteCreated(String fileName) {
		this.loadDataAsync();
	}

	public void stop() {
		if (mPlayer != null && mPlayer.isPlaying()) {
			stopTrackPlayPosition();
			setCurrentPlayPosition(0);
			setPlayButtonState(MediaPlayerStatus.Stopped);
			mPlayer.stop();
		}
	}

	private void runOnUiThread(Runnable action) {
		mHandler.post(action);
	}

	private void play(String fileName, boolean forcePlay) {
		if (mPlayer == null) {
			mPlayer = new MediaPlayer();
			initMediaPlayer();
		}

		if (forcePlay || TextUtils.isEmpty(fileName)
				|| !fileName.equalsIgnoreCase(mCurrentAudioFileName)) {
			if (mPlayer.isPlaying()) {
				mPlayer.stop();
				setPlayButtonState(MediaPlayerStatus.Stopped);
			}

			// Play new audio file
			if (Utility.fileExists(fileName)) {
				try {
					if (!TextUtils.isEmpty(fileName)) {
						mPlayer.reset();
					}
					mCurrentAudioFileName = fileName;
					mPlayer.setDataSource(mCurrentAudioFileName);
					mPlayer.prepareAsync();
					setPlayButtonState(MediaPlayerStatus.Playing);
				} catch (Exception ex) {
					Toast.makeText(getActivity(), ex.getMessage(),
							Toast.LENGTH_LONG).show();
				}
			}
		} else {
			if (mPlayer.isPlaying()) {
				mPlayer.pause();
				setPlayButtonState(MediaPlayerStatus.Paused);
				updateCurrentPlayPosition();
			} else {
				mPlayer.start();
				setPlayButtonState(MediaPlayerStatus.Playing);
			}
		}
	}

	private void initMediaPlayer() {
		mPlayer.setAudioStreamType(AudioManager.STREAM_RING);
		mPlayer.setOnPreparedListener(this);
		mPlayer.setOnCompletionListener(this);
		mPlayer.setOnErrorListener(this);
		mPlayer.setOnBufferingUpdateListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		mSelectedVoiceNoteId = id;
		Object tag = view.getTag();
		if (tag != null) {
			String audioFileName = tag.toString();
			play(audioFileName, true);
			showOrHidePlayControlPanel(true);
		}
	}

	private void showOrHidePlayControlPanel(boolean show) {
		RelativeLayout controlPanel = (RelativeLayout) getView().findViewById(
				R.id.relativeLayoutAudioPlayer);
		if (controlPanel != null) {
			controlPanel.setVisibility(show ? RelativeLayout.VISIBLE
					: RelativeLayout.GONE);
		}
	}

	private void setPlayButtonState(MediaPlayerStatus status) {
		ImageButton btnPlay = (ImageButton) getView().findViewById(
				R.id.imageButtonPlay);
		if (btnPlay == null) {
			return;
		}

		switch (status) {
		case Playing:
			btnPlay.setImageResource(android.R.drawable.ic_media_pause);
			break;
		case Paused:
			btnPlay.setImageResource(android.R.drawable.ic_media_play);
		case Stopped:
			btnPlay.setImageResource(android.R.drawable.ic_media_play);
			setCurrentPlayPosition(0);
			break;
		default:
			break;
		}
	}

	private void setDuration(int duration) {
		ProgressBar progressBar = (ProgressBar) getView().findViewById(
				R.id.progressBarAudioDuration);
		if (progressBar != null) {
			progressBar.setMax(duration);
		}
	}

	private void setCurrentPlayPosition(int position) {
		if (mPlayer != null && mPlayPosition != null) {
			mPlayPosition.setProgress(position);
		}
	}

	private void updateCurrentPlayPosition() {
		if (mPlayer != null && mPlayPosition != null) {
			mPlayPosition.setProgress(mPlayer.getCurrentPosition());
		}
	}

	private void startTrackPlayPosition() {
		if (mTimer != null) {
			try {
				mTimer.cancel();
			} catch (Exception ex) {
				// Do nothing
			}
			mTimer = null;
		}

		mTimer = new Timer();
		mTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if (mPlayer != null && mPlayer.isPlaying()) {
							setCurrentPlayPosition(mPlayer.getCurrentPosition());
						}
					}
				});
			}

		}, 0, 1000);
	}

	private void stopTrackPlayPosition() {
		if (mTimer != null) {
			try {
				mTimer.cancel();
			} catch (Exception ex) {
				// Do nothing
			}
			mTimer = null;
		}
		if (mPlayPosition != null) {
			mPlayPosition.setProgress(0);
		}
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		setPlayButtonState(MediaPlayerStatus.Playing);
		setDuration(mp.getDuration());

		mp.start();
		startTrackPlayPosition();
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		stopTrackPlayPosition();
		setPlayButtonState(MediaPlayerStatus.Stopped);
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		stopTrackPlayPosition();
		Log.i("DEBUG", String.format("what: %s  extra: %s", what, extra));

		Toast.makeText(getActivity(), "Error", Toast.LENGTH_LONG).show();
		mp.reset();

		return true;
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		updateCurrentPlayPosition();
	}
}
