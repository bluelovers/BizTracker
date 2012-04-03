/**
 * 
 */
package com.xiaolei.android.ui;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.xiaolei.android.BizTracker.R;
import com.xiaolei.android.common.Utility;
import com.xiaolei.android.entity.BizLog;
import com.xiaolei.android.listener.OnGotLocationInfoListener;
import com.xiaolei.android.service.DataService;
import com.xiaolei.android.service.LocationService;

/**
 * @author xiaolei
 * 
 */
public class TransactionDetailsFragment extends Fragment implements
		OnClickListener {
	private long mTransactionId = 0;
	private String defaultCurrencyCode = "";
	private LocationService mLocationService = null;
	public static final int REQUEST_CODE = 2117;

	public static TransactionDetailsFragment newInstance(long transactionId) {
		TransactionDetailsFragment result = new TransactionDetailsFragment();
		Bundle args = new Bundle();
		args.putLong("transactionId", transactionId);
		result.setArguments(args);

		return result;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (getArguments() != null) {
			mTransactionId = getArguments().getLong("transactionId");
		}

		View result = inflater.inflate(R.layout.transaction_details_fragment,
				container, false);
		if (result != null) {
			TextView tvLocation = (TextView) result
					.findViewById(R.id.textViewLocation);
			if (tvLocation != null) {
				tvLocation.setOnClickListener(this);
			}
		}

		return result;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		loadDataAsync();
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
		TextView tvLocation = (TextView) container
				.findViewById(R.id.textViewLocation);

		// ImageView ivStar = (ImageView) findViewById(R.id.imageViewTDStar);

		if (tvStuffName != null) {
			if (log.getStuffCount() <= 1) {
				tvStuffName.setText(log.getStuffName());
			} else {
				tvStuffName.setText(String.format("%s ¡Á %d",
						log.getStuffName(), log.getStuffCount()));
			}
		}
		if (tvUpdateTime != null) {
			String value = DateUtils.formatDateTime(getActivity(), log
					.getLastUpdateTime().getTime(), DateUtils.FORMAT_SHOW_DATE
					| DateUtils.FORMAT_SHOW_TIME);
			tvUpdateTime.setText(value);
		}
		if (tvTransactionCost != null) {
			tvTransactionCost.setText(Utility.formatCurrency(log.getCost()
					* log.getStuffCount(), this.defaultCurrencyCode));
			if (log.getCost() > 0) {
				tvTransactionCost.setTextColor(Color.parseColor("#99CC00"));
			} else {
				tvTransactionCost.setTextColor(Color.parseColor("#ff6600"));
			}
		}
		if (tvTransactionComment != null) {
			tvTransactionComment.setText(log.getComment());
		}

		if (tvLocation != null && !TextUtils.isEmpty(log.getLocationName())) {
			tvLocation.setText(log.getLocationName());
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CODE:
			initLocationService();
			if (mLocationService != null
					&& !mLocationService
							.isProviderEnable(LocationManager.GPS_PROVIDER)) {
				mLocationService.start();
				displayCurrentLocationAddress(getActivity().getString(
						R.string.loading));
			}

			break;
		default:
			break;
		}
	}

	@Override
	public void onStop() {
		if (mLocationService != null) {
			mLocationService.stop();
			mLocationService = null;
		}
		super.onStop();

	}

	@Override
	public void onPause() {
		super.onPause();
	}

	private void displayCurrentLocationAddress(String address) {
		if (!TextUtils.isEmpty(address) && getView() != null) {
			TextView tv = (TextView) getView().findViewById(
					R.id.textViewLocation);
			if (tv != null) {
				tv.setText(address);
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.textViewLocation:
			initLocationService();

			if (mLocationService != null
					&& !mLocationService
							.isProviderEnable(LocationManager.GPS_PROVIDER)) {
				Intent intent = new Intent(
						android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				this.startActivityForResult(intent, REQUEST_CODE);
			} else {
				mLocationService.start();
				displayCurrentLocationAddress(getActivity().getString(
						R.string.loading));
			}

			break;
		default:
			break;
		}
	}

	private void initLocationService() {
		if (mLocationService == null) {
			mLocationService = LocationService.getInstance(getActivity());
			mLocationService
					.setOnGotLocationInfoListener(new OnGotLocationInfoListener() {

						@Override
						public void onGotLocation(Location currentLocation) {
							// Do nothing.
						}

						@Override
						public void onGotLocationAddress(
								Location currentLocation, String address) {
							displayCurrentLocationAddress(address);
						}

					});

		}
	}
}
