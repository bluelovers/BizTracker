package com.xiaolei.android.media;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import android.os.Environment;
import android.widget.Toast;

public class AudioRecorder implements OnErrorListener, OnPreparedListener {
	protected String mOutputPath = "BizTracker";

	protected OnErrorListener mErrorListener;
	protected OnInfoListener mInfoListener;

	private Context mContext;
	private String mOutputFileName = null;
	private MediaRecorder mRecorder = null;
	private MediaPlayer mPlayer = null;
	private boolean mIsRecording = false;

	public AudioRecorder(Context context, String outputFileName) {
		mContext = context;
		createParentFolderIfNotExist(outputFileName);
		mOutputFileName = outputFileName;
	}

	private void createParentFolderIfNotExist(String outputFileName) {
		File file = new File(outputFileName);
		if (!file.getParentFile().exists()) {
			if (!file.getParentFile().mkdir()) {
				Toast.makeText(mContext, "Cannot create dictionary.",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	public AudioRecorder(Context context) {
		mContext = context;
		generateNewFileName();
	}

	/**
	 * Generate a new random output file name to storage the audio data.
	 */
	public void generateNewFileName() {
		String audioPath = "";
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			audioPath = Environment.getExternalStorageDirectory()
					+ File.separator + "BizTracker/audio";
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
			mOutputFileName = String.format("audio_%s.3gp",
					format.format(new Date()));
			mOutputFileName = audioPath + File.separator + mOutputFileName;

			createParentFolderIfNotExist(mOutputFileName);
		} else {
			Toast.makeText(mContext, "External storage is not mounted.",
					Toast.LENGTH_SHORT).show();
		}
	}

	public boolean isRecording() {
		return mIsRecording;
	}

	public String getOutputFileName() {
		return mOutputFileName;
	}

	public void setOnErrorListener(OnErrorListener errorListener) {
		mErrorListener = errorListener;
	}

	public void setOnInfoListener(OnInfoListener infoListener) {
		mInfoListener = infoListener;
	}

	public void start() {
		if (mIsRecording) {
			return;
		}

		if (mOutputFileName == null) {
			Toast.makeText(mContext, "Output file name cannot be empty.",
					Toast.LENGTH_SHORT).show();
			return;
		}

		if (mRecorder == null) {
			mRecorder = new MediaRecorder();
		}

		mRecorder.reset();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
		mRecorder.setOutputFile(mOutputFileName);
		mRecorder.setMaxFileSize(0);
		mRecorder.setMaxDuration(0);

		mRecorder.setOnErrorListener(this);
		try {
			mRecorder.prepare();
		} catch (Exception ex) {
			Toast.makeText(mContext, ex.getLocalizedMessage(),
					Toast.LENGTH_SHORT).show();
			mRecorder.reset();
			mIsRecording = false;
			return;
		}
		mRecorder.start();
		mIsRecording = true;
	}

	public void pause() {
		// TODO: not implement

	}

	public void resume() {
		// TODO: not implement
	}

	public void stop() {
		if (mIsRecording) {
			try {
				mRecorder.stop();
				mRecorder.release();
				mRecorder = null;
			} finally {
				mIsRecording = false;
			}
		}

		if (mPlayer != null && mPlayer.isPlaying()) {
			mPlayer.stop();
			mPlayer.release();
			mPlayer = null;
		}
	}

	public void play() {
		play(mOutputFileName);
	}

	public void play(String audioFileName) {
		if (mPlayer != null && mPlayer.isPlaying()) {
			return;
		}

		if (mPlayer == null) {
			mPlayer = new MediaPlayer();
			mPlayer.setOnPreparedListener(this);
			mPlayer.setOnCompletionListener(new OnCompletionListener() {

				@Override
				public void onCompletion(MediaPlayer mp) {

				}
			});
		}

		try {
			File file = new File(audioFileName);
			if (!file.exists()) {
				Toast.makeText(mContext, "File not found.", Toast.LENGTH_SHORT)
						.show();
			}
			mPlayer.reset();
			mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mPlayer.setDataSource(audioFileName);

		} catch (Exception ex) {
			Toast.makeText(mContext, ex.getLocalizedMessage(),
					Toast.LENGTH_SHORT).show();
		}
		mPlayer.prepareAsync();
	}

	@Override
	public void onError(MediaRecorder mr, int what, int extra) {
		mRecorder.reset();
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		mp.start();
	}
}
