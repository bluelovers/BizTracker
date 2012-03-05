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
import com.xiaolei.android.service.DataService;
import com.xiaolei.android.service.DataService.TransactionType;

/**
 * @author Lei Xiao
 * 
 */
public class StatisticPanalFragment extends Fragment {
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

	public void showFavouriteTransactionListSummaryDataAsync() {
		AsyncTask<Void, Void, ArrayList<StatisticInfo>> task = new AsyncTask<Void, Void, ArrayList<StatisticInfo>>() {

			@Override
			protected ArrayList<StatisticInfo> doInBackground(Void... params) {
				ArrayList<StatisticInfo> result = new ArrayList<StatisticInfo>();

				String defaultCurrencyCode = DataService.GetInstance(
						getActivity()).getDefaultCurrencyCode();
				double totalIncome = DataService.GetInstance(getActivity())
						.getTotalMoneyOfFavouriteTransactions(
								TransactionType.Income);
				double totalExpense = DataService.GetInstance(getActivity())
						.getTotalMoneyOfFavouriteTransactions(
								TransactionType.Expense);
				double balance = totalIncome + totalExpense;

				StatisticInfo itemIncome = new StatisticInfo();
				itemIncome.Key = getActivity().getString(R.string.total_income);
				itemIncome.Value = totalIncome;
				itemIncome.CurrencyCode = defaultCurrencyCode;
				result.add(itemIncome);

				StatisticInfo itemExpense = new StatisticInfo();
				itemExpense.Key = getActivity().getString(
						R.string.total_expense);
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

			@Override
			protected void onPostExecute(ArrayList<StatisticInfo> result) {
				if (result != null) {
					ListView lv = (ListView) getView().findViewById(
							R.id.listViewSummary);
					if (lv != null) {
						StatisticSummaryArrayAdapter adpt = new StatisticSummaryArrayAdapter(
								getActivity(),
								R.layout.statistic_item_template, result);
						lv.setAdapter(adpt);
					}
				}
			}

		};

		task.execute();
	}

	public void showSearchResultSummaryDataAsync(String keyword) {
		AsyncTask<Void, Void, Double[]> task = new AsyncTask<Void, Void, Double[]>() {

			@Override
			protected Double[] doInBackground(Void... params) {
				Double[] result = new Double[3];
				result[0] = 0d;
				result[1] = 0d;
				result[2] = 0d;

				return result;
			}

			@Override
			protected void onPostExecute(Double[] result) {

			}

		};

		task.execute();
	}

	public void showDailyTransactionListSummaryDataAsync(Date date) {
		AsyncTask<Void, Void, Double[]> task = new AsyncTask<Void, Void, Double[]>() {

			@Override
			protected Double[] doInBackground(Void... params) {
				Double[] result = new Double[3];
				result[0] = 0d;
				result[1] = 0d;
				result[2] = 0d;

				return result;
			}

			@Override
			protected void onPostExecute(Double[] result) {

			}

		};

		task.execute();
	}

	public void showDateRangeTransactionListSummaryDataAsync(Date startDate,
			Date endDate) {
		AsyncTask<Void, Void, Double[]> task = new AsyncTask<Void, Void, Double[]>() {

			@Override
			protected Double[] doInBackground(Void... params) {
				Double[] result = new Double[3];
				result[0] = 0d;
				result[1] = 0d;
				result[2] = 0d;

				return result;
			}

			@Override
			protected void onPostExecute(Double[] result) {

			}

		};

		task.execute();
	}
}
