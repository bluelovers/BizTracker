/**
 * 
 */
package com.xiaolei.android.BizTracker;

import java.io.File;
import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiaolei.android.common.Utility;
import com.xiaolei.android.entity.PhotoSchema;

/**
 * @author xiaolei
 * 
 */
public class PhotoViewerAdapter extends CursorAdapter {

	private LayoutInflater inflater;
	public String PhotoPath = "";
	@SuppressWarnings("unused")
	private Context context;
	private int maxWidth = 0;
	private int maxHeight = 0;
	public int ColumnCount = 1;

	public PhotoViewerAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
		this.context = context;
		inflater = LayoutInflater.from(context);
	}

	public PhotoViewerAdapter(Context context, Cursor c, int columnCount,
			int maxWidth, int maxHeight) {
		super(context, c);
		this.context = context;
		this.ColumnCount = columnCount > 0 ? columnCount : 1;
		this.maxHeight = maxHeight != 0 ? maxHeight : 64;
		this.maxWidth = maxWidth != 0 ? maxWidth : 64;

		inflater = LayoutInflater.from(context);
	}

	/*
	 * private void detectScreenSize() { WindowManager windowManager =
	 * (WindowManager) context .getSystemService(Context.WINDOW_SERVICE);
	 * maxWidth = windowManager.getDefaultDisplay().getWidth(); maxHeight =
	 * windowManager.getDefaultDisplay().getHeight(); }
	 */

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		if (cursor == null) {
			return;
		}

		String fileName = "";
		int colFileName = cursor.getColumnIndex(PhotoSchema.FileName);
		if (colFileName != -1) {
			fileName = cursor.getString(colFileName);
		}

		ImageView ivPhoto = (ImageView) view.findViewById(R.id.imageViewPhoto);
		if (ivPhoto != null && !TextUtils.isEmpty(fileName)
				&& !TextUtils.isEmpty(PhotoPath)) {
			Bitmap photo = getScaledBitmap(fileName);
			if (photo != null) {
				ivPhoto.setImageBitmap(photo);
			} else {

			}
		}

		TextView tvName = (TextView) view.findViewById(R.id.textViewPhotoName);
		String name = "";
		String createTime = "";
		int colName = cursor.getColumnIndex(PhotoSchema.Name);
		int colCreateTime = cursor.getColumnIndex(PhotoSchema.CreatedTime);

		if (colName != -1) {
			name = cursor.getString(colName);
			if (!TextUtils.isEmpty(name) && tvName != null) {
				tvName.setText(name);
			}
		}

		if (TextUtils.isEmpty(name) && colCreateTime != -1) {
			createTime = cursor.getString(colCreateTime);
			Date value = Utility.convertToDate(createTime);
			if (value != null) {
				createTime = DateUtils.formatDateTime(context, value.getTime(),
						DateUtils.FORMAT_SHOW_DATE);
				if (!TextUtils.isEmpty(createTime) && tvName != null) {
					tvName.setText(createTime);
				}
			}
		}
	}

	private Bitmap getScaledBitmap(String fileName) {
		Bitmap result = null;
		if (!TextUtils.isEmpty(fileName)) {
			File file = new File(PhotoPath, fileName);

			if (file.exists() == true) {
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inTempStorage = new byte[16 * 1024];
				options.inJustDecodeBounds = true;
				result = BitmapFactory.decodeFile(file.getAbsolutePath(),
						options);

				int originalWidth = options.outWidth;
				int originalHeight = options.outHeight;
				int desiredWidth = maxWidth / ColumnCount;
				int desiredHeight = desiredWidth;
				if (ColumnCount == 1) {
					desiredHeight = maxHeight;
				}

				int sampleSize = Math.max(
						1,
						Math.max(originalWidth / desiredWidth, originalHeight
								/ desiredHeight));

				options.inJustDecodeBounds = false;
				options.inSampleSize = sampleSize;

				result = BitmapFactory.decodeFile(file.getAbsolutePath(),
						options);
				result = ThumbnailUtils.extractThumbnail(result, desiredWidth,
						desiredHeight);
			}
		}
		return result;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return inflater.inflate(R.layout.photo_frame, parent, false);
	}

}
