package com.xiaolei.android.BizTracker;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.xiaolei.android.common.TimeSpan;
import com.xiaolei.android.common.Utility;
import com.xiaolei.android.entity.VoiceNoteSchema;

public class VoiceNotesCursorAdapter extends CursorAdapter {

	private LayoutInflater mInflater;

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
		long duration = cursor.getLong(cursor
				.getColumnIndex(VoiceNoteSchema.Duration));
		String title = cursor.getString(cursor
				.getColumnIndex(VoiceNoteSchema.Title));
		String summary = cursor.getString(cursor
				.getColumnIndex(VoiceNoteSchema.Summary));
		String fileName = cursor.getString(cursor
				.getColumnIndex(VoiceNoteSchema.FileName));
		fileName = Utility.getAudioFullFileName(context, fileName);
		String createdDate = cursor.getString(cursor
				.getColumnIndex(VoiceNoteSchema.LastUpdatedTime));

		view.setTag(fileName);

		TextView tvTitle = (TextView) view
				.findViewById(R.id.textViewVoiceNoteTitle);
		TextView tvSummary = (TextView) view
				.findViewById(R.id.textViewVoiceNoteSummary);

		if (TextUtils.isEmpty(title)) {
			if (tvTitle != null) {
				tvTitle.setText(Utility.toLocalDateString(createdDate));
			}
		} else {
			tvTitle.setText(title);
		}

		if (tvSummary != null) {
			TimeSpan durationTime = new TimeSpan(duration);
			tvSummary.setText(durationTime.toString() + (summary != null ? " "
					+ summary : ""));
		}
	}

}
