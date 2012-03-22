package com.xiaolei.android.BizTracker;

import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiaolei.android.common.TimeSpan;
import com.xiaolei.android.common.Utility;
import com.xiaolei.android.entity.VoiceNoteSchema;

public class VoiceNotesCursorAdapter extends CursorAdapter {

	private LayoutInflater mInflater;
	private OnClickListener mOnButtonClickListener;
	private long mActiveVoiceNoteId = 0;

	public void setOnButtonClickListener(OnClickListener onClickListener) {
		mOnButtonClickListener = onClickListener;
	}

	public void setActiveVoiceNoteId(long id) {
		if (id != mActiveVoiceNoteId) {
			mActiveVoiceNoteId = id;
			this.notifyDataSetChanged();
		}
	}

	public VoiceNotesCursorAdapter(Context context, Cursor c) {
		super(context, c);
		mInflater = LayoutInflater.from(context);
	}

	public VoiceNotesCursorAdapter(Context context, Cursor c,
			boolean autoRequery) {
		super(context, c, autoRequery);
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View result = mInflater.inflate(R.layout.voice_note_item_template,
				parent, false);

		return result;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		long id = cursor.getLong(cursor.getColumnIndex(VoiceNoteSchema.Id));
		ImageView button = (ImageView) view
				.findViewById(R.id.imageViewDeleteVoiceNote);
		long duration = cursor.getLong(cursor
				.getColumnIndex(VoiceNoteSchema.Duration));
		String title = cursor.getString(cursor
				.getColumnIndex(VoiceNoteSchema.Title));
		String summary = cursor.getString(cursor
				.getColumnIndex(VoiceNoteSchema.Summary));
		String fileName = cursor.getString(cursor
				.getColumnIndex(VoiceNoteSchema.FileName));
		fileName = Utility.getAudioFullFileName(context, fileName);
		String createdDateString = cursor.getString(cursor
				.getColumnIndex(VoiceNoteSchema.LastUpdatedTime));
		Date createdDate = Utility.convertToDate(createdDateString);
		TimeSpan durationTime = new TimeSpan(duration);

		view.setTag(fileName);

		TextView tvTitle = (TextView) view
				.findViewById(R.id.textViewVoiceNoteTitle);
		TextView tvSummary = (TextView) view
				.findViewById(R.id.textViewVoiceNoteSummary);

		if (tvTitle != null) {
			if (TextUtils.isEmpty(title)) {
				tvTitle.setText(durationTime.toString());
			} else {
				tvTitle.setText(title);
			}

			if (id == mActiveVoiceNoteId) {
				tvTitle.setTextColor(context.getResources().getColor(
						R.color.darkBlue));
			}
		}

		if (tvSummary != null) {
			tvSummary.setText(DateUtils.getRelativeTimeSpanString(createdDate
					.getTime()) + (summary != null ? " " + summary : ""));
		}

		if (mOnButtonClickListener != null) {
			if (button != null) {
				button.setTag(id);
				button.setOnClickListener(mOnButtonClickListener);
			}
		}
	}

}
