/**
 * 
 */
package com.xiaolei.android.activity;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.xiaolei.android.listener.OnCameraTakedPhotoListener;
import com.xiaolei.android.service.DataService;

/**
 * @author xiaolei
 * 
 */
public class TransactionDetails extends CameraSupportableActivity implements
		OnClickListener, OnCameraTakedPhotoListener, OnPageChangeListener {

	private static final String JPG = ".jpg";
	private long transactionId = -1;
	private BizLog source;
	public static String TRANSACTION_ID = "TransactionId";
	public static final int REQUEST_CODE = 101;
	private TransactionDetails context;
	private String defaultCurrencyCode = "CNY";
	private int photoCount = 0;
	private String currentPhotoFileName = "";
	private Cursor cursor;
	private ImageView ivStar;
	private ViewPager viewPager;
	private TransactionPhotoPageAdapter adpt;
	private Boolean reloadingPhoto = false;
	private TransactionPhoto currentPhotoInfo;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.transaction_details);
		context = this;

		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			if (bundle.containsKey(TRANSACTION_ID)) {
				transactionId = bundle.getLong(TRANSACTION_ID);
				loadDataAsync();
				loadPhotoAsync();
			}
		}

		ImageButton btnNewPhoto = (ImageButton) findViewById(R.id.buttonNewPhoto);
		if (btnNewPhoto != null) {
			btnNewPhoto.setOnClickListener(this);
		}

		RelativeLayout relativeLayoutNoPhoto = (RelativeLayout) findViewById(R.id.relativeLayoutNoPhoto);
		if (relativeLayoutNoPhoto != null) {
			relativeLayoutNoPhoto.setOnClickListener(this);
		}
		
		viewPager = (ViewPager) context
				.findViewById(R.id.viewPaperTransactionPhotos);
		if (viewPager != null) {
			viewPager.setOnPageChangeListener(this);
		}

		/*
		 * ImageView ivTakePhoto = (ImageView)
		 * findViewById(R.id.imageViewTakePhoto); ivStar = (ImageView)
		 * findViewById(R.id.imageViewTDStar);
		 * 
		 * if (ivTakePhoto != null) { ivTakePhoto.setOnClickListener(this); } if
		 * (ivStar != null) { ivStar.setOnClickListener(this); }
		 */

		// Bind click listener for all tool buttons
		LinearLayout linearLayoutToolButtons = (LinearLayout) findViewById(R.id.linearLayoutToolButtons);
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

		this.AddCameraTakedPhotoListener(this);
	}

	private void loadDataAsync() {
		ViewFlipper viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipperTransactionDetails);
		if (viewFlipper != null) {
			viewFlipper.setDisplayedChild(0);
		}

		AsyncTask<Long, Void, BizLog> task = new AsyncTask<Long, Void, BizLog>() {

			@Override
			protected BizLog doInBackground(Long... params) {
				BizLog result = null;
				if (params != null && params.length > 0) {
					result = DataService.GetInstance(context)
							.getTransactionDetailsById(transactionId);
				}
				context.defaultCurrencyCode = DataService.GetInstance(context)
						.getDefaultCurrencyCode();
				context.source = result;

				return result;
			}

			@Override
			protected void onPostExecute(BizLog result) {
				showData(result);
			}

		};

		task.execute(transactionId);
	}

	private void loadPhotoAsync() {
		reloadingPhoto = true;

		ViewFlipper viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipperTransactionPhoto);
		if (viewFlipper != null) {
			viewFlipper.setDisplayedChild(0);
		}

		AsyncTask<Long, Void, Void> task = new AsyncTask<Long, Void, Void>() {

			@Override
			protected Void doInBackground(Long... params) {
				photoCount = DataService.GetInstance(context)
						.getTransactionPhotoCount(transactionId);
				context.cursor = DataService.GetInstance(context)
						.getTransactionPhotos(transactionId);

				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				showPhotoData();
				ViewFlipper viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipperTransactionPhoto);
				if (viewFlipper != null) {
					if (photoCount <= 0) {
						viewFlipper.setDisplayedChild(1);
					} else {
						viewFlipper.setDisplayedChild(2);
					}
				}

				reloadingPhoto = false;
			}

		};

		task.execute(transactionId);
	}

	private void showData(BizLog log) {
		ViewFlipper viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipperTransactionDetails);
		if (viewFlipper != null) {
			viewFlipper.setDisplayedChild(1);
		}

		if (log == null) {
			return;
		}

		TextView tvStuffName = (TextView) findViewById(R.id.textViewStuffName);
		TextView tvUpdateTime = (TextView) findViewById(R.id.textViewTransactionTime);
		TextView tvTransactionCost = (TextView) findViewById(R.id.textViewTransactionCost);
		TextView tvTransactionComment = (TextView) findViewById(R.id.textViewTransactionComment);

		// ImageView ivStar = (ImageView) findViewById(R.id.imageViewTDStar);

		if (tvStuffName != null) {
			tvStuffName.setText(log.getStuffName());
		}
		if (tvUpdateTime != null) {
			String value = DateUtils.formatDateTime(context, log
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

		if (ivStar != null) {
			if (log.getStar() == true) {
				ivStar.setImageResource(android.R.drawable.btn_star_big_on);
			} else {
				ivStar.setImageResource(android.R.drawable.btn_star_big_off);
			}
		}
	}

	private void showPhotoData() {
		/*
		 * TextView tvTransactionPhotoCount = (TextView)
		 * findViewById(R.id.textViewTransactionPhotoCount);
		 * 
		 * if (tvTransactionPhotoCount != null) { if (photoCount > 0) {
		 * tvTransactionPhotoCount.setText(String.valueOf(photoCount) + " " +
		 * getString(R.string.photos)); } else {
		 * tvTransactionPhotoCount.setText(String.valueOf(photoCount) + " " +
		 * getString(R.string.photo)); } }
		 */

		if (cursor != null && cursor.isClosed() != true) {
			adpt = new TransactionPhotoPageAdapter(context, context.cursor);
			viewPager = (ViewPager) context
					.findViewById(R.id.viewPaperTransactionPhotos);
			if (viewPager != null) {
				viewPager.setAdapter(adpt);
			}
		}
	}

	private void openCamera() {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		currentPhotoFileName = String.format("photo_%s",
				format.format(new Date()));

		this.takePhoto(currentPhotoFileName);
	}

	@SuppressWarnings("unused")
	private void changeStar() {
		if (source != null) {
			Boolean star = false;
			if (source.getStar() == true) {
				source.setStar(false);
				star = false;
			} else {
				source.setStar(true);
				star = true;
			}

			if (ivStar != null) {
				ivStar.setImageResource(star == true ? android.R.drawable.btn_star_big_on
						: android.R.drawable.btn_star_big_off);
			}

			AsyncTask<Boolean, Void, Boolean> task = new AsyncTask<Boolean, Void, Boolean>() {

				@Override
				protected Boolean doInBackground(Boolean... params) {
					Boolean result = false;
					if (params != null && params.length > 0) {
						if (params[0] == true) {
							DataService.GetInstance(context).addStar(
									transactionId);
							result = true;
						} else {
							DataService.GetInstance(context).removeStar(
									transactionId);
							result = true;
						}
					}

					return result;
				}

				@Override
				protected void onPostExecute(Boolean result) {
					if (result == true) {
						context.setResult(RESULT_OK);
					}
				}

			};
			task.execute(star);
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
					reloadPhotos();
				} else {
					Toast.makeText(context, "Failed to save photo.",
							Toast.LENGTH_SHORT).show();
				}
			}

		};
		task.execute(photo);
	}

	private void reloadPhotos() {
		if (reloadingPhoto == true) {
			return;
		}

		if (cursor != null && cursor.isClosed() == false) {
			cursor.close();
			cursor = null;
		}
		loadPhotoAsync();
	}

	@Override
	public void onClick(View v) {
		if (v == null) {
			return;
		}

		switch (v.getId()) {
		case R.id.buttonNewPhoto:
		case R.id.relativeLayoutNoPhoto:
		case R.id.toolButtonNewPhoto:
			openCamera();
			break;
		case R.id.toolButtonDeletePhoto:
			deletePhoto();
			break;
		/*
		 * case R.id.imageViewTDStar: changeStar(); break;
		 */
		default:
			break;
		}
	}

	private void deletePhoto() {
		if (currentPhotoInfo == null) {
			return;
		}

		Utility.showConfirmDialog(this, getString(R.string.confirm),
				getString(R.string.confirm_delete_photo),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						deletePhotoAsync();
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
					DataService.GetInstance(context)
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

	@Override
	public void OnTakedPhoto(String fileName, Exception error) {
		if (error == null) {
			if (!TextUtils.isEmpty(fileName)) {
				addTransactionPhoto(fileName);
			}
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (cursor != null && cursor.isClosed() == false) {
			cursor.close();
			cursor = null;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		reloadPhotos();
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageSelected(int position) {
		currentPhotoInfo = null;
		if (adpt != null) {
			currentPhotoInfo = adpt.GetItemSource(position);
		}
	}
}
