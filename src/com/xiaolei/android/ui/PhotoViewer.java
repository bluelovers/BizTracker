/**
 * 
 */
package com.xiaolei.android.ui;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.xiaolei.android.BizTracker.BizTracker;
import com.xiaolei.android.BizTracker.PhotoViewerAdapter;
import com.xiaolei.android.BizTracker.R;
import com.xiaolei.android.entity.TransactionPhoto;
import com.xiaolei.android.listener.OnCameraTakedPhotoListener;
import com.xiaolei.android.service.DataService;

/**
 * @author xiaolei
 * 
 */
public class PhotoViewer extends CameraSupportableActivity implements
		OnClickListener, OnCameraTakedPhotoListener {

	private Context context;
	private long transactionId = -1;
	private ViewFlipper viewFlipper;
	public static final String TRANSACTION_ID = "TransactionId";
	private String currentPhotoFileName = "";
	private final String JPG = ".jpg";
	private PhotoViewerAdapter adpt;
	private Cursor cursor;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		context = this;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.photo_viewer);

		viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipperPhotoViewer);
		ImageView ivTakePhoto = (ImageView) findViewById(R.id.imageViewTakePhoto);
		if (ivTakePhoto != null) {
			ivTakePhoto.setOnClickListener(this);
		}

		ImageButton ibTakePhoto = (ImageButton) findViewById(R.id.imageButtonTakePhoto);
		if (ibTakePhoto != null) {
			ibTakePhoto.setOnClickListener(this);
		}

		this.AddCameraTakedPhotoListener(this);

		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			if (bundle.containsKey(TRANSACTION_ID)) {
				transactionId = bundle.getLong(TRANSACTION_ID);
				loadDataAsync();
			}
		}
	}

	private void loadDataAsync() {
		AsyncTask<Long, Void, Cursor> task = new AsyncTask<Long, Void, Cursor>() {

			@Override
			protected Cursor doInBackground(Long... params) {
				cursor = null;
				if (params == null || params.length == 0) {
					return cursor;
				}

				cursor = DataService.GetInstance(context).getTransactionPhotos(
						params[0]);

				return cursor;
			}

			@Override
			protected void onPostExecute(Cursor result) {

				if (result == null || result.getCount() == 0) {
					if (viewFlipper != null) {
						viewFlipper.setDisplayedChild(1);
					}
				} else {
					showPhotos(result);
				}
			}

		};
		task.execute(transactionId);
	}

	private void showPhotos(Cursor cursor) {
		GridView gridView = (GridView) findViewById(R.id.gridViewPhotoGallery);
		if (viewFlipper != null && cursor != null) {
			if (!Environment.MEDIA_MOUNTED.equals(Environment
					.getExternalStorageState())) {
				Toast.makeText(context, "External storage is not available.",
						Toast.LENGTH_SHORT).show();
				return;
			}

			viewFlipper.setDisplayedChild(2);

			if (adpt == null) {
				int columnCount = 1;
				if (gridView != null && cursor != null) {
					int totalCount = cursor.getCount();
					if (totalCount == 1) {
						columnCount = 1;
						gridView.setNumColumns(1);
					} else if (totalCount <= 4) {
						columnCount = 2;
						gridView.setNumColumns(2);
					} else {
						columnCount = 3;
						gridView.setNumColumns(3);
					}
				}

				int maxWidth = 64;
				int maxHeight = 64;

				LinearLayout container = (LinearLayout) findViewById(R.id.linearLayoutPhotoGallery);
				RelativeLayout titleBar = (RelativeLayout) findViewById(R.id.titleBar);
				if (container != null && titleBar != null) {
					maxWidth = container.getMeasuredWidth();
					maxHeight = container.getMeasuredHeight()
							- titleBar.getMeasuredHeight();
				}

				adpt = new PhotoViewerAdapter(this, cursor, columnCount,
						maxWidth, maxHeight);
				adpt.PhotoPath = Environment.getExternalStorageDirectory()
						.getAbsolutePath()
						+ File.separator
						+ BizTracker.PHOTO_PATH;

				if (gridView != null) {
					gridView.setAdapter(adpt);
				}
			} else {
				int totalCount = cursor.getCount();
				int columnCount = 1;
				if (gridView != null) {
					if (totalCount == 1) {
						columnCount = 1;
						gridView.setNumColumns(1);
					} else if (totalCount <= 4) {
						columnCount = 2;
						gridView.setNumColumns(2);
					} else {
						columnCount = 3;
						gridView.setNumColumns(3);
					}
				}

				adpt.ColumnCount = columnCount;
				adpt.changeCursor(cursor);
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.imageButtonTakePhoto:
		case R.id.imageViewTakePhoto:
			openCamera();
			break;
		default:
			break;
		}
	}

	private void showBusyIndicator() {
		if (viewFlipper != null) {
			viewFlipper.setDisplayedChild(0);
		}
	}

	@SuppressWarnings("unused")
	private void hideBusyIndicator() {
		if (viewFlipper != null) {
			viewFlipper.setDisplayedChild(2);
		}
	}

	/**
	 * 
	 */
	private void openCamera() {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		currentPhotoFileName = String.format("photo_%s",
				format.format(new Date()));

		this.takePhoto(currentPhotoFileName);
	}

	@Override
	public void OnTakedPhoto(String fullFileName, Exception error) {
		if (error == null) {
			this.showBusyIndicator();

			addTransactionPhoto(fullFileName);
			loadDataAsync();
		} else {

		}
	}

	private void addTransactionPhoto(String fullFileName) {
		if (TextUtils.isEmpty(currentPhotoFileName) || transactionId <= 0) {
			return;
		}
		TransactionPhoto photo = new TransactionPhoto();
		photo.setBizLogId(transactionId);
		photo.setFileName(currentPhotoFileName + JPG);
		photo.setCreatedTime(new Date());

		AsyncTask<TransactionPhoto, Void, Boolean> taks = new AsyncTask<TransactionPhoto, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(TransactionPhoto... params) {
				Boolean result = false;
				if (params == null || params.length == 0) {
					return result;
				}
				long photoId = DataService.GetInstance(context)
						.addTransactionPhoto(params[0]);
				result = photoId > 0;
				return result;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				if (result == true) {

				} else {
					Toast.makeText(context, "Failed to save photo.",
							Toast.LENGTH_SHORT).show();
				}
			}
		};
		taks.execute(photo);
	}

	@Override
	public void onStop() {
		super.onStop();

		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
	}
}
