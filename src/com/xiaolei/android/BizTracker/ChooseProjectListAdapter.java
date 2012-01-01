/**
 * 
 */
package com.xiaolei.android.BizTracker;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.xiaolei.android.entity.ProjectSchema;

/**
 * @author xiaolei
 * 
 */
public class ChooseProjectListAdapter extends CursorAdapter {
	private LayoutInflater inflater = null;

	public ChooseProjectListAdapter(Context context, Cursor c,
			boolean autoRequery) {
		super(context, c, autoRequery);
		inflater = LayoutInflater.from(context);
	}

	public ChooseProjectListAdapter(Context context, Cursor cursor) {
		super(context, cursor);
		inflater = LayoutInflater.from(context);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		//CheckBox checkBox = (CheckBox) view
		//		.findViewById(R.id.checkBoxProjectIsChecked);
		TextView tvName = (TextView) view
				.findViewById(R.id.textViewProjectName);

		String name = cursor.getString(cursor
				.getColumnIndex(ProjectSchema.Name));

		if (tvName != null) {
			tvName.setText(name);
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return inflater.inflate(R.layout.choose_project_item_template, parent,
				false);
	}

}
