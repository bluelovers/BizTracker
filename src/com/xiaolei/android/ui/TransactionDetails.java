/**
 * 
 */
package com.xiaolei.android.ui;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.Toast;

import com.xiaolei.android.BizTracker.R;
import com.xiaolei.android.common.Utility;
import com.xiaolei.android.entity.TransactionPhoto;
import com.xiaolei.android.service.DataService;

/**
 * @author xiaolei
 * 
 */
public class TransactionDetails extends FragmentActivity implements
		OnClickListener {

	private long transactionId = -1;
	public static String TRANSACTION_ID = "TransactionId";
	public static final int REQUEST_CODE = 1111;
	public static final int TAKE_PHOTO = 1114;
	private TransactionDetails context;
	private String currentPhotoFileName = "";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.transaction_details);

		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			if (bundle.containsKey(TRANSACTION_ID)) {
				transactionId = bundle.getLong(TRANSACTION_ID);
				FragmentManager fragmentMan = this.getSupportFragmentManager();
				if (fragmentMan != null) {
					TransactionDetailsFragment fragment = (TransactionDetailsFragment) fragmentMan
							.findFragmentById(R.id.fragmentTransactionDetails);
					if (fragment != null) {
						fragment.setTransactionId(transactionId);
					}
				}
			}
		}

		ImageButton btnNewPhoto = (ImageButton) findViewById(R.id.buttonNewPhoto);
		if (btnNewPhoto != null) {
			btnNewPhoto.setOnClickListener(this);
		}
	}

	private void addTransactionPhoto(String fullFileName) {
		if (TextUtils.isEmpty(fullFileName) || transactionId <= 0) {
			return;
		}
		TransactionPhoto photo = new TransactionPhoto();
		String shortFileName = "";
		String fileName = "";

		File file = new File(fullFileName);
		if (file.exists() == true) {
			shortFileName = file.getName();

			// If the photo storages in the default photoPath then use its short
			// file name,
			// otherwise, use the full file name.
			String photoPath = Utility.getDefaultPhotoFolderPath(this);
			if (!file.getParent().equalsIgnoreCase(photoPath)) {
				fileName = fullFileName;
			} else {
				fileName = shortFileName;
			}
		} else {
			return;
		}

		photo.setBizLogId(transactionId);
		photo.setFileName(fileName);
		photo.setCreatedTime(new Date());

		AsyncTask<TransactionPhoto, Void, Boolean> task = new AsyncTask<TransactionPhoto, Void, Boolean>() {

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
					notifyFragmentToReloadPhotoList();
				} else {
					Toast.makeText(context, "Failed to save photo.",
							Toast.LENGTH_SHORT).show();
				}
			}
		};
		task.execute(photo);
	}

	private void notifyFragmentToReloadPhotoList() {
		FragmentManager fragmentMan = context.getSupportFragmentManager();
		if (fragmentMan != null) {
			TransactionDetailsFragment fragment = (TransactionDetailsFragment) fragmentMan
					.findFragmentById(R.id.fragmentTransactionDetails);
			if (fragment != null) {
				fragment.reloadPhotos();
			}
		}
	}

	public void openCamera() {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		currentPhotoFileName = String.format("photo_%s",
				format.format(new Date()));

		this.takePhoto(currentPhotoFileName);
	}

	private void takePhoto(String preferredFileName) {
		String photoPath = Utility.getDefaultPhotoFolderPath(this);
		if (TextUtils.isEmpty(photoPath)) {
			throw new IllegalArgumentException(
					"External storage is not available.");
		}

		if (TextUtils.isEmpty(preferredFileName)) {
			throw new IllegalArgumentException(
					"preferredFileName cannot be empty.");
		}

		Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
		String shortFileName = preferredFileName + ".jpg";
		currentPhotoFileName = photoPath + File.separator + shortFileName;
		File photo = new File(photoPath, shortFileName);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));

		startActivityForResult(intent, TAKE_PHOTO);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == TAKE_PHOTO) {
			if (resultCode == Activity.RESULT_OK) {
				addTransactionPhoto(currentPhotoFileName);
			} else {
				deletePhotoFile(currentPhotoFileName);
				currentPhotoFileName = "";
			}
		}
	}

	private void deletePhotoFile(String fullFileName) {
		AsyncTask<String, Void, Boolean> task = new AsyncTask<String, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(String... fileNames) {
				if (fileNames != null && fileNames.length > 0) {
					String fileName = fileNames[0];
					File file = new File(fileName);
					if (file.exists()) {
						return file.delete();
					} else {
						return false;
					}
				} else {
					return false;
				}
			}

			@Override
			protected void onPostExecute(Boolean result) {
				if (result == true) {

				}
			}
		};
		task.execute(fullFileName);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString("photoFileName", currentPhotoFileName);

	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			this.currentPhotoFileName = savedInstanceState
					.getString("photoFileName");
			if (!TextUtils.isEmpty(currentPhotoFileName)) {
				notifyFragmentToReloadPhotoList();
			}
		}
	}

	@Override
	public void onClick(View v) {
		if (v == null) {
			return;
		}

		switch (v.getId()) {
		case R.id.buttonNewPhoto:
			openCamera();
			break;
		default:
			break;
		}
	}
}
