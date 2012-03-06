/**
 * 
 */
package com.xiaolei.android.ui;

import java.util.ArrayList;
import java.util.Date;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.xiaolei.android.BizTracker.R;
import com.xiaolei.android.adapter.StatisticInfo;
import com.xiaolei.android.adapter.StatisticSummaryArrayAdapter;
import com.xiaolei.android.listener.OnLoadCompletedListener;
import com.xiaolei.android.service.DataService;
import com.xiaolei.android.service.DataService.TransactionType;

/**
 * @author Lei Xiao
 * 
 */
public class StatisticPanalFragment extends Fragment {
	private OnLoadCompletedListener mLoadCompletedListener;

	public void setOnLoadCompletedListener(
			OnLoadCompletedListener loadCompletedListener) {
		mLoadCompletedListener = loadCompletedListener;
	}
	
	protected void onLoadCompleted(boolean success){
		if(mLoadCompletedListener != null){
			mLoadCompletedListener.onLoadCompleted(success);
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (savedInstanceState != null) {

		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View result = inflater.inflate(R.layout.statistic_panel_fragment,
				container, false);
		if (result != null) {

		}

		return result;
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

	}

	@Override
	public void onDestroy() {
		super.onDestroy();

	}

	@Override
	public void onDestroyView() {

		super.onDestroyView();
	}

	private void showSummaryResult(ArrayList<StatisticInfo> result) {
		if (result != null) {
			ListView lv = (ListView) getView().findViewById(
					R.id.listViewSummary);
			if (lv != null) {
				StatisticSummaryArrayAdapter adpt = new StatisticSummaryArrayAdapter(
						getActivity(), R.layout.statistic_item_template, result);
				lv.setAdapter(adpt);

			}
		}
		onLoadCompleted(true);
	}

	private ArrayList<StatisticInfo> buildStatisticInfo(
			String defaultCurrencyCode, double totalIncome,
			double totalExpense, double balance) {
		ArrayList<StatisticInfo> result = new ArrayList<StatisticInfo>();

		StatisticInfo itemIncome = new StatisticInfo();
		itemIncome.Key = getActivity().getString(R.string.total_income);
		itemIncome.Value = totalIncome;
		itemIncome.CurrencyCode = defaultCurrencyCode;
		result.add(itemIncome);

		StatisticInfo itemExpense = new StatisticInfo();
		itemExpense.Key = getActivity().getString(R.string.total_expense);
		itemExpense.Value = totalExpense;
		itemExpense.CurrencyCode = defaultCurrencyCode;
		result.add(itemExpense);

		StatisticInfo itemBalance = new StatisticInfo();
		itemBalance.Key = getActivity().getString(R.string.balance);
		itemBalance.Value = balance;
		itemBalance.CurrencyCode = defaultCurrencyCode;
		result.add(itemBalance);

		return result;
	}

	public void showFavouriteTransactionListSummaryDataAsync() {
		AsyncTask<Void, Void, ArrayList<StatisticInfo>> task = new AsyncTask<Void, Void, ArrayList<StatisticInfo>>() {

			@Override
			protected ArrayList<StatisticInfo> doInBackground(Void... params) {
				String defaultCurrencyCode = DataService.GetInstance(
						getActivity()).getDefaultCurrencyCode();
				double totalIncome = DataService.GetInstance(getActivity())
						.getTotalMoneyOfFavouriteTransactions(
								TransactionType.Income);
				double totalExpense = DataService.GetInstance(getActivity())
						.getTotalMoneyOfFavouriteTransactions(
								TransactionType.Expense);
				double balance = totalIncome + totalExpense;

				return buildStatisticInfo(defaultCurrencyCode, totalIncome,
						totalExpense, balance);
			}

			@Override
			protected void onPostExecute(ArrayList<StatisticInfo> result) {
				showSummaryResult(result);
			}

		};

		task.execute();
	}

	public void showSearchResultSummaryDataAsync(final String keyword) {
		AsyncTask<Void, Void, ArrayList<StatisticInfo>> task = new AsyncTask<Void, Void, ArrayList<StatisticInfo>>() {

			@Override
			protected ArrayList<StatisticInfo> doInBackground(Void... params) {

				String defaultCurrencyCode = DataService.GetInstance(
						getActivity()).getDefaultCurrencyCode();
				double totalIncome = DataService.GetInstance(getActivity())
						.getTotalMoneyOfSearchedTransactions(keyword,
								TransactionType.Income);
				double totalExpense = DataService.GetInstance(getActivity())
						.getTotalMoneyOfSearchedTransactions(keyword,
								TransactionType.Expense);
				double balance = totalIncome + totalExpense;

				return buildStatisticInfo(defaultCurrencyCode, totalIncome,
						totalExpense, balance);
			}

			@Override
			protected void onPostExecute(ArrayList<StatisticInfo> result) {
				showSummaryResult(result);
			}

		};

		task.execute();
	}

	public void showDailyTransactionListSummaryDataAsync(final Date date) {
		AsyncTask<Void, Void, ArrayList<StatisticInfo>> task = new AsyncTask<Void, Void, ArrayList<StatisticInfo>>() {

			@Override
			protected ArrayList<StatisticInfo> doInBackground(Void... params) {

				String defaultCurrencyCode = DataService.GetInstance(
						getActivity()).getDefaultCurrencyCode();
				double totalIncome = DataService.GetInstance(getActivity())
						.getTotalEarn(date, date);
				double totalExpense = DataService.GetInstance(getActivity())
						.getTotalPay(date, date);
				double balance = totalIncome + totalExpense;

				return buildStatisticInfo(defaultCurrencyCode, totalIncome,
						totalExpense, balance);
			}

			@Override
			protected void onPostExecute(ArrayList<StatisticInfo> result) {
				showSummaryResult(result);
			}

		};

		task.execute();
	}

	public void showDateRangeTransactionListSummaryDataAsync(
			final Date startDate, final Date endDate) {
		AsyncTask<Void, Void, ArrayList<StatisticInfo>> task = new AsyncTask<Void, Void, ArrayList<StatisticInfo>>() {

			@Override
			protected ArrayList<StatisticInfo> doInBackground(Void... params) {

				String defaultCurrencyCode = DataService.GetInstance(
						getActivity()).getDefaultCurrencyCode();
				double totalIncome = DataService.GetInstance(getActivity())
						.getTotalEarn(startDate, endDate);
				double totalExpense = DataService.GetInstance(getActivity())
						.getTotalPay(startDate, endDate);
				double balance = totalIncome + totalExpense;

				return buildStatisticInfo(defaultCurrencyCode, totalIncome,
						totalExpense, balance);
			}

			@Override
			protected void onPostExecute(ArrayList<StatisticInfo> result) {
				showSummaryResult(result);
			}

		};

		task.execute();
	}
}
