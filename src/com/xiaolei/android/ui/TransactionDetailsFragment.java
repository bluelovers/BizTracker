/**
 * 
 */
package com.xiaolei.android.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.xiaolei.android.BizTracker.R;
import com.xiaolei.android.common.Utility;
import com.xiaolei.android.entity.BizLog;
import com.xiaolei.android.service.DataService;

/**
 * @author xiaolei
 * 
 */
public class TransactionDetailsFragment extends Fragment {
	private long mTransactionId = 0;
	private String defaultCurrencyCode = "";

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

		if (tvLocation != null && !TextUtils.isEmpty(log.getLocationName())) {
			tvLocation.setText(log.getLocationName());
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

	}

	@Override
	public void onStop() {
		super.onStop();

	}

	@Override
	public void onPause() {
		super.onPause();
	}
}
