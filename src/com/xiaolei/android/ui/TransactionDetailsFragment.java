/**
 * 
 */
package com.xiaolei.android.ui;

import java.io.File;
import java.util.Date;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.xiaolei.android.BizTracker.R;
import com.xiaolei.android.BizTracker.TransactionPhotoPageAdapter;
import com.xiaolei.android.common.Utility;
import com.xiaolei.android.entity.BizLog;
import com.xiaolei.android.entity.TransactionPhoto;
import com.xiaolei.android.service.DataService;

/**
 * @author xiaolei
 * 
 */
public class TransactionDetailsFragment extends Fragment implements
		OnClickListener, OnPageChangeListener {
	@SuppressWarnings("unused")
	private static final int REQUEST_CODE = 1112;
	public static final int PICK_PHOTO = 1113;
	private long mTransactionId = 0;
	private int photoCount = 0;
	private String defaultCurrencyCode = "";
	private TransactionPhoto currentPhotoInfo = null;
	private Cursor cursor = null;
	private TransactionPhotoPageAdapter adpt;

	public void setTransactionId(long transactionId) {
		if (transactionId != mTransactionId) {
			mTransactionId = transactionId;
			this.loadDataAsync();
			this.loadPhotoAsync();
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View result = inflater.inflate(R.layout.transaction_details_fragment,
				container, false);
		if (result != null) {
			RelativeLayout relativeLayoutNoPhoto = (RelativeLayout) result
					.findViewById(R.id.relativeLayoutNoPhoto);
			if (relativeLayoutNoPhoto != null) {
				relativeLayoutNoPhoto.setOnClickListener(this);
			}

			ViewPager viewPager = (ViewPager) result
					.findViewById(R.id.viewPaperTransactionPhotos);
			if (viewPager != null) {
				viewPager.setOnPageChangeListener(this);
			}

			// Bind click listener for all tool buttons
			LinearLayout linearLayoutToolButtons = (LinearLayout) result
					.findViewById(R.id.linearLayoutToolButtons);
			if (linearLayoutToolButtons != null) {
				int childCount = linearLayoutToolButtons.getChildCount();
				for (int i = 0; i < childCount; i++) {
					View child = linearLayoutToolButtons.getChildAt(i);
					if (child instanceof Button) {
						Button button = (Button) child;
						if (button != null) {
							button.setOnClickListener(this);
						}
					}
				}
			}
		}

		return result;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

	}

	@Override
	public void onDestroyView() {

		super.onDestroyView();

	}

	private void loadDataAsync() {
		if (getView() == null) {
			return;
		}

		ViewFlipper viewFlipper = (ViewFlipper) getView().findViewById(
				R.id.viewFlipperTransactionDetails);
		if (viewFlipper != null) {
			viewFlipper.setDisplayedChild(0);
		}

		AsyncTask<Long, Void, BizLog> task = new AsyncTask<Long, Void, BizLog>() {

			@Override
			protected BizLog doInBackground(Long... params) {
				BizLog result = null;
				if (params != null && params.length > 0) {
					result = DataService.GetInstance(getActivity())
							.getTransactionDetailsById(mTransactionId);
				}
				defaultCurrencyCode = DataService.GetInstance(getActivity())
						.getDefaultCurrencyCode();

				return result;
			}

			@Override
			protected void onPostExecute(BizLog result) {
				showData(result);
			}

		};

		task.execute(mTransactionId);
	}

	private void loadPhotoAsync() {
		ViewFlipper viewFlipper = (ViewFlipper) getView().findViewById(
				R.id.viewFlipperTransactionPhoto);
		if (viewFlipper != null) {
			viewFlipper.setDisplayedChild(0);
		}

		AsyncTask<Long, Void, Cursor> task = new AsyncTask<Long, Void, Cursor>() {

			@Override
			protected Cursor doInBackground(Long... params) {
				photoCount = DataService.GetInstance(getActivity())
						.getTransactionPhotoCount(mTransactionId);
				Cursor cursor = DataService.GetInstance(getActivity())
						.getTransactionPhotos(mTransactionId);

				return cursor;
			}

			@Override
			protected void onPostExecute(Cursor result) {
				cursor = result;
				showPhotoData(result);
				ViewFlipper viewFlipper = (ViewFlipper) getView().findViewById(
						R.id.viewFlipperTransactionPhoto);
				if (viewFlipper != null) {
					if (photoCount <= 0) {
						viewFlipper.setDisplayedChild(1);
					} else {
						viewFlipper.setDisplayedChild(2);
					}
				}
			}

		};

		task.execute(mTransactionId);
	}

	private void showData(BizLog log) {
		ViewFlipper viewFlipper = (ViewFlipper) getView().findViewById(
				R.id.viewFlipperTransactionDetails);
		if (viewFlipper != null) {
			viewFlipper.setDisplayedChild(1);
		}

		if (log == null) {
			return;
		}

		View container = getView();
		TextView tvStuffName = (TextView) container
				.findViewById(R.id.textViewStuffName);
		TextView tvUpdateTime = (TextView) container
				.findViewById(R.id.textViewTransactionTime);
		TextView tvTransactionCost = (TextView) container
				.findViewById(R.id.textViewTransactionCost);
		TextView tvTransactionComment = (TextView) container
				.findViewById(R.id.textViewTransactionComment);

		// ImageView ivStar = (ImageView) findViewById(R.id.imageViewTDStar);

		if (tvStuffName != null) {
			tvStuffName.setText(log.getStuffName());
		}
		if (tvUpdateTime != null) {
			String value = DateUtils.formatDateTime(getActivity(), log
					.getLastUpdateTime().getTime(), DateUtils.FORMAT_SHOW_DATE
					| DateUtils.FORMAT_SHOW_TIME);
			tvUpdateTime.setText(value);
		}
		if (tvTransactionCost != null) {
			tvTransactionCost.setText(Utility.formatCurrency(log.getCost(),
					this.defaultCurrencyCode));
			if (log.getCost() > 0) {
				tvTransactionCost.setTextColor(Color.parseColor("#99CC00"));
			} else {
				tvTransactionCost.setTextColor(Color.parseColor("#ff6600"));
			}
		}
		if (tvTransactionComment != null) {
			tvTransactionComment.setText(log.getComment());
		}
	}

	private void showPhotoData(Cursor cursor) {
		if (cursor != null && cursor.isClosed() != true) {
			TransactionPhotoPageAdapter adpt = new TransactionPhotoPageAdapter(
					getActivity(), cursor);
			ViewPager viewPager = (ViewPager) getView().findViewById(
					R.id.viewPaperTransactionPhotos);
			if (viewPager != null) {
				viewPager.setAdapter(adpt);
				currentPhotoInfo = adpt.GetItemSource(0);
			}
		}
	}

	public void reloadPhotos() {
		if (cursor != null && cursor.isClosed() == false) {
			cursor.close();
			cursor = null;
		}
		loadPhotoAsync();
	}

	@Override
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {

	}

	@Override
	public void onPageSelected(int position) {
		currentPhotoInfo = null;

		if (adpt == null) {
			ViewPager viewPager = (ViewPager) getView().findViewById(
					R.id.viewPaperTransactionPhotos);
			if (viewPager != null) {
				adpt = (TransactionPhotoPageAdapter) viewPager.getAdapter();
			}
		}

		if (adpt != null) {
			currentPhotoInfo = adpt.GetItemSource(position);
		}
	}

	@Override
	public void onPageScrollStateChanged(int state) {

	}

	@Override
	public void onClick(View v) {
		if (v == null) {
			return;
		}

		switch (v.getId()) {
		case R.id.relativeLayoutNoPhoto:
		case R.id.toolButtonNewPhoto:
			TransactionDetails activity = (TransactionDetails) getActivity();
			if (activity != null) {
				activity.openCamera();
			}
			break;
		case R.id.toolButtonDeletePhoto:
			deletePhoto();
			break;
		case R.id.toolButtonAddExistingPhoto:
			choosePhotoFromGallery();
			break;
		/*
		 * case R.id.imageViewTDStar: changeStar(); break;
		 */
		default:
			break;
		}
	}

	private void choosePhotoFromGallery() {
		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
		photoPickerIntent.setType("image/*");
		startActivityForResult(photoPickerIntent, PICK_PHOTO);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == PICK_PHOTO && resultCode == Activity.RESULT_OK) {
			if (data != null) {
				Uri selectedImageUri = data.getData();
				String fileName = Utility.getPhotoPathFromGallery(
						getActivity(), selectedImageUri);
				addTransactionPhoto(fileName);
			}
		}
	}

	private void addTransactionPhoto(String fullFileName) {
		if (TextUtils.isEmpty(fullFileName) || mTransactionId <= 0) {
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
			String photoPath = Utility.getDefaultPhotoFolderPath(getActivity());
			if (!file.getParent().equalsIgnoreCase(photoPath)) {
				fileName = fullFileName;
			} else {
				fileName = shortFileName;
			}
		} else {
			return;
		}

		photo.setBizLogId(mTransactionId);
		photo.setFileName(fileName);
		photo.setCreatedTime(new Date());

		AsyncTask<TransactionPhoto, Void, Boolean> task = new AsyncTask<TransactionPhoto, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(TransactionPhoto... params) {
				Boolean result = false;
				if (params == null || params.length == 0) {
					return result;
				}
				long photoId = DataService.GetInstance(getActivity())
						.addTransactionPhoto(params[0]);
				result = photoId > 0;
				return result;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				if (result == true) {
					reloadPhotos();
				} else {
					Toast.makeText(getActivity(), "Failed to save photo.",
							Toast.LENGTH_SHORT).show();
				}
			}
		};
		task.execute(photo);
	}

	private void deletePhoto() {
		if (currentPhotoInfo == null) {
			return;
		}

		Utility.showConfirmDialog(getActivity(), getString(R.string.confirm),
				getString(R.string.confirm_delete_photo),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						deletePhotoAsync();
						if (currentPhotoInfo != null) {
							deletePhotoFile(currentPhotoInfo.getFileName());
						}
					}
				});
	}

	private void deletePhotoAsync() {
		if (currentPhotoInfo == null) {
			return;
		}
		AsyncTask<Long, Void, Boolean> task = new AsyncTask<Long, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(Long... ids) {
				if (ids != null && ids.length > 0) {
					long id = ids[0];
					DataService.GetInstance(getActivity())
							.removeTransactionPhotoRelation(id);

				} else {
					return false;
				}
				return true;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				if (result == true) {
					loadPhotoAsync();
				}
			}
		};
		task.execute(currentPhotoInfo.getId());
	}

	private Boolean fileExists(String fileName) {
		if (TextUtils.isEmpty(fileName)) {
			return false;
		}

		File file = new File(fileName);
		return file.exists();
	}

	private void deletePhotoFile(String fileName) {
		boolean fileExists = false;
		String finalFileName = fileName;

		if (fileExists(fileName)) {
			finalFileName = fileName;
			fileExists = true;
		} else {
			String fullFileName = Utility
					.getDefaultPhotoFolderPath(getActivity())
					+ File.separator
					+ fileName;
			if (fileExists(fullFileName)) {
				finalFileName = fullFileName;
				fileExists = true;
			}
		}

		final String fullFileName = finalFileName;
		if (fileExists) {
			AsyncTask<String, Void, Boolean> task = new AsyncTask<String, Void, Boolean>() {

				@Override
				protected Boolean doInBackground(String... fileNames) {
					if (fileNames != null && fileNames.length > 0) {
						String fileName = fileNames[0];
						File file = new File(fileName);
						file.delete();
					} else {
						return false;
					}
					return true;
				}

				@Override
				protected void onPostExecute(Boolean result) {
					if (result == true) {
						loadPhotoAsync();
					}
				}
			};
			task.execute(fullFileName);
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		if (cursor != null && cursor.isClosed() == false) {
			cursor.close();
			cursor = null;
		}
	}
}