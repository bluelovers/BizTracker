/**
 * 
 */
package com.xiaolei.android.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.media.AudioManager;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.xiaolei.android.BizTracker.R;

/**
 * @author xiaolei
 * 
 */
public class VolumeControlPreference extends DialogPreference {
	private int mValue = 0;
	private int mNewValue = 0;
	private float mMaxValue = 1;

	public VolumeControlPreference(Context context, AttributeSet attrs) {
		super(context, attrs);

		init();
	}

	public VolumeControlPreference(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		this.setDialogLayoutResource(R.layout.volumn_editor);
		detectMaxVolumeValue();
		this.setDialogIcon(R.drawable.audio_volume_high);
	}

	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);
		final View container = view;

		SeekBar seekBar = (SeekBar) view.findViewById(R.id.seekBarVolume);
		if (seekBar != null) {
			seekBar.setMax((int) mMaxValue);
			seekBar.setKeyProgressIncrement(1);
			seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

				@Override
				public void onProgressChanged(SeekBar sender, int progress,
						boolean fromUser) {
					mNewValue = progress;
					int percentage = (int) ((progress / mMaxValue) * 100);

					if (container != null) {
						TextView tv = (TextView) container
								.findViewById(R.id.textViewVolumePercentage);
						if (tv != null) {
							tv.setText(getContext().getString(R.string.volume)
									+ " (" + String.valueOf(percentage) + "%)");
						}
					}
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {

				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {

				}
			});
			seekBar.setProgress(this.getPersistedInt(0));
		}
	}

	protected void setVolumePercentage(int value) {
		final boolean wasBlocking = shouldDisableDependents();

		mValue = value;
		this.persistInt(mValue);

		int percentage = (int) ((mValue / mMaxValue) * 100);
		this.setTitle(String.format("%s (%s)",
				this.getContext().getString(R.string.volume),
				String.valueOf(percentage) + "%"));

		// Notify dependents change
		final boolean isBlocking = shouldDisableDependents();
		if (isBlocking != wasBlocking) {
			notifyDependencyChange(isBlocking);
		}
	}

	/**
	 * Gets the Volume Percentage.
	 * 
	 * @return
	 */
	public int getVolumePercentage() {
		return mValue;
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);

		if (positiveResult) {
			if (callChangeListener(mNewValue)) {
				setVolumePercentage(mNewValue);
			}
		}
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		setVolumePercentage(restoreValue ? getPersistedInt(0)
				: (Integer) defaultValue);
	}

	@Override
	public boolean shouldDisableDependents() {
		return super.shouldDisableDependents();
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return super.onGetDefaultValue(a, index);
	}

	private void detectMaxVolumeValue() {
		// Get the max audio volume
		AudioManager audioManager = (AudioManager) getContext()
				.getSystemService(Context.AUDIO_SERVICE);
		float streamVolumeMax = audioManager
				.getStreamMaxVolume(AudioManager.STREAM_RING);
		mMaxValue = streamVolumeMax;
	}
}
