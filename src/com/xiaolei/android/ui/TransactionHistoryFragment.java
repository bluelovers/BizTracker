package com.xiaolei.android.ui;

import java.util.Date;
import java.util.Hashtable;
import com.xiaolei.android.BizTracker.R;
import com.xiaolei.android.common.Utility;
import com.xiaolei.android.service.DataService;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ViewFlipper;

public class TransactionHistoryFragment extends Fragment {
	private ViewHolder mViewHolder = new ViewHolder();

	public static TransactionHistoryFragment newInstance() {
		TransactionHistoryFragment result = new TransactionHistoryFragment();
		// Bundle args = new Bundle();
		// result.setArguments(args);

		return result;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (getArguments() != null) {

		}

		View result = inflater.inflate(R.layout.transaction_history_fragment,
				container, false);
		if (result != null) {
			mViewHolder.ViewFlipperTransactionHistory = (ViewFlipper) result
					.findViewById(R.id.viewFlipperTransactionHistory);
			mViewHolder.ListViewTransactionHistoryTypes = (ListView) result
					.findViewById(R.id.listViewTransactionHistoryTypes);

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
		waiting();
		AsyncTask<Void, Void, Hashtable<Integer, double[]>> task = new AsyncTask<Void, Void, Hashtable<Integer, double[]>>() {

			@Override
			protected Hashtable<Integer, double[]> doInBackground(
					Void... params) {
				Hashtable<Integer, double[]> result = calcSummaryInfo();
				return result;
			}

			@Override
			protected void onPostExecute(Hashtable<Integer, double[]> result) {
				showData(result);
			}
		};
		task.execute();
	}

	private Hashtable<Integer, double[]> calcSummaryInfo() {
		Hashtable<Integer, double[]> result = new Hashtable<Integer, double[]>();

		// Today
		double[] today = new double[2];
		today[0] = DataService.GetInstance(getActivity()).getTodaySumPay();
		today[1] = DataService.GetInstance(getActivity()).getTodaySumEarn();
		result.put(0, today);

		// This week
		Date startDayOfThisWeek = Utility.getStartDayOfThisWeek();
		Date endDayOfThisWeek = Utility.getEndTimeOfDate(Utility.addDays(
				startDayOfThisWeek, 6));
		double[] thisWeek = new double[2];
		thisWeek[0] = DataService.GetInstance(getActivity()).getTotalPay(
				startDayOfThisWeek, endDayOfThisWeek);
		thisWeek[1] = DataService.GetInstance(getActivity()).getTotalEarn(
				startDayOfThisWeek, endDayOfThisWeek);
		result.put(1, thisWeek);

		// This month
		Date now = new Date();
		Date startDayOfMonth = Utility.getStartDayOfMonth(now);
		Date endDayOfMonth = Utility.getEndTimeOfDate(Utility
				.getEndDayOfMonth(now));
		double[] thisMonth = new double[2];
		thisMonth[0] = DataService.GetInstance(getActivity()).getTotalPay(
				startDayOfMonth, endDayOfMonth);
		thisMonth[1] = DataService.GetInstance(getActivity()).getTotalEarn(
				startDayOfMonth, endDayOfMonth);
		result.put(1, thisMonth);

		// This year
		Date startDayOfYear = Utility.getStartDayOfYear(now);
		Date endDayOfYear = Utility.getEndTimeOfDate(Utility
				.getEndDayOfYear(now));
		double[] thisYear = new double[2];
		thisYear[0] = DataService.GetInstance(getActivity()).getTotalPay(
				startDayOfYear, endDayOfYear);
		thisYear[1] = DataService.GetInstance(getActivity()).getTotalEarn(
				startDayOfYear, endDayOfYear);
		result.put(1, thisYear);

		// All transactions
		double[] allTransactions = DataService.GetInstance(getActivity())
				.getTransactionsTotalCost();
		result.put(1, allTransactions);

		return result;
	}

	private void showData(Hashtable<Integer, double[]> result) {
		if (result != null
				&& mViewHolder.ListViewTransactionHistoryTypes != null) {
			TransactionHistoryTypesAdapter adpt = new TransactionHistoryTypesAdapter(
					getActivity(), result);
			mViewHolder.ListViewTransactionHistoryTypes.setAdapter(adpt);
		}

		stopWaiting();
	}

	private void waiting() {
		if (mViewHolder.ViewFlipperTransactionHistory != null) {
			mViewHolder.ViewFlipperTransactionHistory.setDisplayedChild(0);
		}
	}

	private void stopWaiting() {
		if (mViewHolder.ViewFlipperTransactionHistory != null) {
			mViewHolder.ViewFlipperTransactionHistory.setDisplayedChild(1);
		}
	}

	private final static class ViewHolder {
		private ViewHolder() {
		}

		public ListView ListViewTransactionHistoryTypes;
		public ViewFlipper ViewFlipperTransactionHistory;
	}
}
