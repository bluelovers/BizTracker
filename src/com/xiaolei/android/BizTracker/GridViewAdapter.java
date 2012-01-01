/**
 * 
 */
package com.xiaolei.android.BizTracker;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;

import com.xiaolei.android.entity.StuffSchema;

/**
 * @author xiaolei
 * 
 */
public class GridViewAdapter extends CursorAdapter {
	@SuppressWarnings("unused")
	private Context context;

	// private LayoutInflater inflater;

	public GridViewAdapter(Context context, Cursor c) {
		super(context, c);

		this.context = context;
		// inflater = LayoutInflater.from(context);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		// String name =
		// cursor.getString(cursor.getColumnIndex(StuffSchema.Name));
		int id = cursor.getInt(cursor.getColumnIndex(StuffSchema.Id));
		/*
		 * String pictureBase64 = cursor.getString(cursor
		 * .getColumnIndex(StuffSchema.Picture));
		 * 
		 * byte[] data = Base64.decode(pictureBase64, Base64.DEFAULT); Bitmap
		 * picture = BitmapFactory.decodeByteArray(data, 0, data.length);
		 */

		ImageButton imgBtn = (ImageButton) view.findViewById(-1);
		// imgBtn.setImageBitmap(picture);
		imgBtn.setTag(id);
		imgBtn.setOnClickListener((OnClickListener) context);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return null;
		// return inflater.inflate(R.layout.stuff_button, parent, false);
	}

}
