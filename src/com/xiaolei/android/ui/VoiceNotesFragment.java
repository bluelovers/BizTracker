package com.xiaolei.android.ui;

import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.xiaolei.android.BizTracker.R;
import com.xiaolei.android.BizTracker.VoiceNotesCursorAdapter;
import com.xiaolei.android.common.Utility;
import com.xiaolei.android.entity.VoiceNoteSchema;
import com.xiaolei.android.listener.OnVoiceNoteCreatedListener;
import com.xiaolei.android.service.DataService;

public class VoiceNotesFragment extends Fragment implements OnClickListener,
		OnVoiceNoteCreatedListener, OnItemClickListener, OnPreparedListener,
		OnCompletionListener {

	public static final String FRAGMENT_VOICE_NOTE_RECORDER_DIALOG = "fragmentVoiceNoteRecorderDialog";
	private long mTransactionId = 0;
	private String mCurrentAudioFileName;
	private MediaPlayer mPlayer;
	private Cursor mVoiceNoteListCursor = null;
	private boolean mIsPaused = false;

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
					if (lv != null) {
						if (lv.getAdapter() != null) {
							VoiceNotesCursorAdapter adapter = (VoiceNotesCursorAdapter) lv
									.getAdapter();
							if (adapter != null) {
								adapter.changeCursor(result);
							}
						} else {
							VoiceNotesCursorAdapter adapter = new VoiceNotesCursorAdapter(
									getActivity(), result);
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

		if (mVoiceNoteListCursor != null && !mVoiceNoteListCursor.isClosed()) {
			mVoiceNoteListCursor.close();
			mVoiceNoteListCursor = null;
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
					.newInstance("Record voice note", mTransactionId);
			recorderFragment.setOnVoiceNoteCreatedListener(this);
			recorderFragment.show(getActivity().getSupportFragmentManager(),
					FRAGMENT_VOICE_NOTE_RECORDER_DIALOG);
			break;
		case R.id.imageButtonPlay:
			play(mCurrentAudioFileName);
			break;
		case R.id.imageButtonPrevious:
			playPrevious();
			break;
		case R.id.imageButtonNext:
			playNext();
			break;
		default:
			break;
		}
	}

	private void playNext() {
		if (mVoiceNoteListCursor != null && !mVoiceNoteListCursor.isClosed()) {
			if (mVoiceNoteListCursor.moveToLast()) {
				String fileName = mVoiceNoteListCursor
						.getString(mVoiceNoteListCursor
								.getColumnIndex(VoiceNoteSchema.FileName));
				fileName = Utility
						.getAudioFullFileName(getActivity(), fileName);
				play(fileName);
			}
		}
	}

	private void playPrevious() {
		if (mVoiceNoteListCursor != null && !mVoiceNoteListCursor.isClosed()) {
			if (mVoiceNoteListCursor.moveToLast()) {
				String fileName = mVoiceNoteListCursor
						.getString(mVoiceNoteListCursor
								.getColumnIndex(VoiceNoteSchema.FileName));
				fileName = Utility
						.getAudioFullFileName(getActivity(), fileName);
				play(fileName);
			}
		}
	}

	@Override
	public void onVoiceNoteCreated(String fileName) {
		mCurrentAudioFileName = fileName;
		this.loadDataAsync();
	}

	private void play(String fileName) {
		if (mPlayer == null) {
			mPlayer = new MediaPlayer();
			mPlayer.setAudioStreamType(AudioManager.STREAM_RING);
			mPlayer.setOnPreparedListener(this);
			mPlayer.setOnCompletionListener(this);
		}

		if (mIsPaused) {
			mIsPaused = false;
			mPlayer.start();
		} else {
			if (!mPlayer.isPlaying()) {
				if (Utility.fileExists(mCurrentAudioFileName)) {
					try {
						mCurrentAudioFileName = fileName;
						mPlayer.setDataSource(mCurrentAudioFileName);
						mPlayer.prepareAsync();
					} catch (Exception ex) {
						Toast.makeText(getActivity(), ex.getMessage(),
								Toast.LENGTH_LONG).show();
					}
				}
			} else {
				mPlayer.pause();
				mIsPaused = true;
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Object tag = view.getTag();
		if (tag != null) {
			mCurrentAudioFileName = tag.toString();
			play(mCurrentAudioFileName);
		}
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		ImageButton btnPlay = (ImageButton) getView().findViewById(
				R.id.imageButtonPlay);
		if (btnPlay != null) {
			btnPlay.setImageResource(R.drawable.ic_media_pause);
		}

		mp.start();
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		ImageButton btnPlay = (ImageButton) getView().findViewById(
				R.id.imageButtonPlay);
		if (btnPlay != null) {
			btnPlay.setImageResource(R.drawable.ic_media_play);
		}
		mp.reset();
	}
}
