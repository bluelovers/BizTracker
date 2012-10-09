package com.xiaolei.android.ui;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import com.xiaolei.android.BizTracker.R;
import com.xiaolei.android.common.Utility;
import com.xiaolei.android.service.DataService;

import android.R.drawable;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.ViewFlipper;

public class TransactionHistoryFragment extends Fragment implements
		AdapterView.OnItemClickListener {
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
			mViewHolder.ListViewTransactionHistoryTypes
					.setOnItemClickListener(this);
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

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == Activity.RESULT_OK) {
			loadDataAsync();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int index, long arg3) {
		switch (index) {
		case 0:
			Intent intent = new Intent(getActivity(),
					DailyTransactionList.class);
			intent.putExtra("date", new Date());
			this.startActivityForResult(intent, 0);

			break;
		case 1:
			Intent intentWeekDays = new Intent(getActivity(),
					TransactionListWeekly.class);
			this.startActivityForResult(intentWeekDays, 0);
			break;
		case 2:
			Intent intentDaysOfMonth = new Intent(getActivity(),
					TransactionListMonthly.class);
			intentDaysOfMonth.putExtra("date", new Date());
			this.startActivityForResult(intentDaysOfMonth, 0);
			break;
		case 3:
			Intent intentYear = new Intent(getActivity(),
					TransactionListYearly.class);
			this.startActivityForResult(intentYear, 0);
			break;
		case 4:
			showAllTransactionList();
			break;
		case 5:
			showSearchChooser();
			break;
		case 6:
			showStarredBizLog();
			break;
		case 7:
			Intent intentProjectManager = new Intent(getActivity(),
					ProjectManager.class);
			this.startActivity(intentProjectManager);
			break;
		default:
			break;
		}
	}

	public void loadDataAsync() {
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
		result.put(2, thisMonth);

		// This year
		Date startDayOfYear = Utility.getStartDayOfYear(now);
		Date endDayOfYear = Utility.getEndTimeOfDate(Utility
				.getEndDayOfYear(now));
		double[] thisYear = new double[2];
		thisYear[0] = DataService.GetInstance(getActivity()).getTotalPay(
				startDayOfYear, endDayOfYear);
		thisYear[1] = DataService.GetInstance(getActivity()).getTotalEarn(
				startDayOfYear, endDayOfYear);
		result.put(3, thisYear);

		// All transactions
		double[] allTransactions = DataService.GetInstance(getActivity())
				.getTransactionsTotalCost();
		result.put(4, allTransactions);

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

	private void showAllTransactionList() {
		Intent intentAllTrans = new Intent(getActivity(), TransactionList.class);
		intentAllTrans
				.putExtra(TransactionList.KEY_SHOW_ALL_TRANSACTIONS, true);
		intentAllTrans.putExtra(TransactionList.KEY_TITLE,
				getString(R.string.all_transactions));
		startActivityForResult(intentAllTrans, 0);
	}

	private void showStarredBizLog() {
		Intent intent = new Intent(getActivity(), TransactionList.class);
		intent.putExtra(TransactionList.KEY_TITLE,
				getString(R.string.starred_biz_log));
		intent.putExtra(TransactionList.KEY_SHOW_FULL_DATE, true);
		intent.putExtra(TransactionList.KEY_SHOW_STARRED_RECORDS, true);
		this.startActivityForResult(intent, 0);
	}

	private void showSearchChooser() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setIcon(drawable.ic_dialog_info);
		builder.setTitle(R.string.choose_search_type);
		builder.setItems(
				new String[] {
						getActivity().getString(R.string.search_by_date),
						getActivity().getString(R.string.search_by_keyword),
						getActivity().getString(R.string.search_by_date_range) },
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0:
							showDatePicker();
							break;
						case 1:
							showSeachView();
							break;
						case 2:
							showDateRangePicker();
							break;
						}
					}
				});

		builder.show();
	}

	private void showSeachView() {
		Intent intent = new Intent(getActivity(), TransactionSearchResult.class);
		this.startActivityForResult(intent,
				TransactionSearchResult.REQUEST_CODE);
	}

	private void showDatePicker() {
		Utility.showDialog(getActivity(), R.layout.go_to_date,
				getString(R.string.biz_date),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						AlertDialog alertDialog = (AlertDialog) dialog;
						DatePicker datePicker = (DatePicker) alertDialog
								.findViewById(R.id.datePickerGoToDate);
						datePicker.clearFocus();

						GregorianCalendar cal = new GregorianCalendar(
								datePicker.getYear(), datePicker.getMonth(),
								datePicker.getDayOfMonth());

						Date date = cal.getTime();
						Intent intent = new Intent(getActivity(),
								DailyTransactionList.class);
						intent.putExtra(DailyTransactionList.KEY_DATE, date);
						getActivity().startActivityForResult(intent,
								DailyTransactionList.REQUEST_CODE);

						dialog.dismiss();
					}
				}, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
	}

	private void showDateRangePicker() {
		Utility.showDialog(getActivity(), R.layout.date_range_picker,
				getString(R.string.choose_date_range),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						AlertDialog alertDialog = (AlertDialog) dialog;
						DatePicker datePickerStartDate = (DatePicker) alertDialog
								.findViewById(R.id.datePickerStartDate);
						DatePicker datePickerEndDate = (DatePicker) alertDialog
								.findViewById(R.id.datePickerEndDate);

						datePickerStartDate.clearFocus();
						datePickerEndDate.clearFocus();

						GregorianCalendar calStart = new GregorianCalendar(
								datePickerStartDate.getYear(),
								datePickerStartDate.getMonth(),
								datePickerStartDate.getDayOfMonth(), 0, 0, 0);
						GregorianCalendar calEnd = new GregorianCalendar(
								datePickerEndDate.getYear(), datePickerEndDate
										.getMonth(), datePickerEndDate
										.getDayOfMonth(), 23, 59, 59);

						Date startDate = calStart.before(calEnd) ? calStart
								.getTime() : calEnd.getTime();
						Date endDate = calEnd.after(calStart) ? calEnd
								.getTime() : calStart.getTime();

						Intent intent = new Intent(getActivity(),
								TransactionList.class);
						if (!Utility.dateEquals(startDate, endDate)) {
							String title = String.format("%s ~ %s", DateUtils
									.formatDateTime(getActivity(),
											startDate.getTime(),
											DateUtils.FORMAT_SHOW_DATE),
									DateUtils.formatDateTime(getActivity(),
											endDate.getTime(),
											DateUtils.FORMAT_SHOW_DATE));

							intent.putExtra(TransactionList.KEY_START_DATE,
									startDate);
							intent.putExtra(TransactionList.KEY_END_DATE,
									endDate);
							intent.putExtra(TransactionList.KEY_TITLE, title);
						} else {
							intent.putExtra(TransactionList.KEY_DATE, startDate);
						}

						AsyncTask<Intent, Void, Void> task = new AsyncTask<Intent, Void, Void>() {

							@Override
							protected Void doInBackground(Intent... params) {
								getActivity().startActivity(params[0]);
								return null;
							}

						};
						task.execute(intent);

						// dialog.dismiss();
					}
				}, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
	}

	private final static class ViewHolder {
		private ViewHolder() {
		}

		public ListView ListViewTransactionHistoryTypes;
		public ViewFlipper ViewFlipperTransactionHistory;
	}
}
