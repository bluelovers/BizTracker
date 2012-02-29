/**
 * 
 */
package com.xiaolei.android.ui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.ViewSwitcher;

import com.xiaolei.android.BizTracker.BizTracker;
import com.xiaolei.android.BizTracker.TransactionListCursorAdapter;
import com.xiaolei.android.BizTracker.Helper;
import com.xiaolei.android.BizTracker.R;
import com.xiaolei.android.common.Utility;
import com.xiaolei.android.entity.BizLog;
import com.xiaolei.android.listener.OnNotifyDataChangedListener;
import com.xiaolei.android.service.DataService;
import com.xiaolei.android.service.DataService.TransactionType;

/**
 * @author xiaolei
 * 
 */
public class TransactionListFragment extends Fragment implements
		OnClickListener, OnItemClickListener, OnItemLongClickListener {
	private TransactionListFragment context;
	public static final int REQUEST_CODE = 1118;
	private ViewType viewType = ViewType.Unknown;
	private String searchKeyword = "";
	private Date date = new Date();
	private Date startDate;
	private Date endDate;
	private OnNotifyDataChangedListener onNotifyDataChangedListener;
	private Cursor mCursor = null;
	private boolean showCheckBox = false;

	private ArrayList<Long> checkedTransactionIds = new ArrayList<Long>();

	public void setOnNotifyDataChangedListener(
			OnNotifyDataChangedListener listener) {
		onNotifyDataChangedListener = listener;
	}

	public void notifyDataChanged() {
		if (onNotifyDataChangedListener != null) {
			onNotifyDataChangedListener.onNotifyDataChanged(this);
		}
	}

	public void showTransactionListByDate(Date date) {
		super.setHasOptionsMenu(true);
		this.date = date;
		viewType = ViewType.DailyTransactionList;

		fillDataAsync(date);
		calculateStatisticInfoAsync();
	}

	public void showFavouriteTransactionList() {
		super.setHasOptionsMenu(true);

		viewType = ViewType.FavouriteTransactionList;
		fillFavouriteTransactionListAsync();
		calculateStatisticInfoAsync();
	}

	public void showDateRangeTransactionList(Date startDate, Date endDate) {
		super.setHasOptionsMenu(true);
		viewType = ViewType.DateRangeTransactionList;

		showDataRangeTransactionListAsync(startDate, endDate);
		calculateStatisticInfoAsync();
	}

	private void showDataRangeTransactionListAsync(Date startDate, Date endDate) {
		this.startDate = startDate;
		this.endDate = endDate;

		AsyncTask<Date, Void, Cursor> task = new AsyncTask<Date, Void, Cursor>() {

			@Override
			protected Cursor doInBackground(Date... dates) {
				Cursor result = null;
				if (dates != null && dates.length == 2) {
					Date startDate = dates[0];
					Date endDate = dates[1];
					result = DataService.GetInstance(getActivity())
							.getTransactionListByDateRange(startDate, endDate);
				}

				return result;
			}

			@Override
			protected void onPostExecute(Cursor result) {
				showData(result);
			}
		};
		task.execute(this.startDate, this.endDate);
	}

	public void search(String keyword) {
		super.setHasOptionsMenu(true);

		searchKeyword = keyword;
		viewType = ViewType.SearchTransactionList;

		searchAsync(keyword);
		calculateStatisticInfoAsync();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View result = inflater.inflate(R.layout.transaction_list_fragment,
				container, false);
		context = this;

		if (result != null) {
			ListView lv = (ListView) result
					.findViewById(R.id.listViewBizLogByDay);
			if (lv != null) {
				lv.setOnItemClickListener(this);
				lv.setOnItemLongClickListener(this);
			}

			ViewFlipper viewFlipper = (ViewFlipper) result
					.findViewById(R.id.viewFlipperStatistic);
			if (viewFlipper != null) {
				viewFlipper.setOnClickListener(this);
			}
		}

		return result;
	}

	private void showData(Cursor result) {
		if (mCursor != null && !mCursor.isClosed()) {
			mCursor.close();
			mCursor = null;
		}
		mCursor = result;
		if (result != null && !result.isClosed()) {
			if (result.getCount() > 0) {
				if (getView() != null) {
					ListView lv = (ListView) getView().findViewById(
							R.id.listViewBizLogByDay);
					if (lv != null) {
						TransactionListCursorAdapter listAdapter = (TransactionListCursorAdapter) lv
								.getAdapter();

						if (listAdapter == null) {
							boolean showFullDate = false;
							if (this.viewType == ViewType.SearchTransactionList
									|| this.viewType == ViewType.DateRangeTransactionList
									|| this.viewType == ViewType.FavouriteTransactionList) {
								showFullDate = true;
							}

							listAdapter = new TransactionListCursorAdapter(
									getActivity(), result, showFullDate, false,
									context);
							lv.setAdapter(listAdapter);
						} else {
							listAdapter.changeCursor(result);
						}
					}

					ViewSwitcher viewSwitcher = (ViewSwitcher) getView()
							.findViewById(R.id.viewSwitcherDayBizLog);
					if (viewSwitcher.getDisplayedChild() != 0) {
						viewSwitcher.showNext();
					}

					ViewSwitcher viewSwitcherContent = (ViewSwitcher) getView()
							.findViewById(R.id.viewSwitcherContent);
					viewSwitcherContent.setDisplayedChild(1);
				}
			} else {
				if (getView() != null) {
					ViewSwitcher viewSwitcher = (ViewSwitcher) getView()
							.findViewById(R.id.viewSwitcherDayBizLog);
					viewSwitcher.setDisplayedChild(1);
				}
			}
		} else {
			if (getView() != null) {
				ViewSwitcher viewSwitcher = (ViewSwitcher) getView()
						.findViewById(R.id.viewSwitcherDayBizLog);
				viewSwitcher.setDisplayedChild(1);
			}
		}
	}

	private class StatisticInfo {
		private double totalIncome = 0d;
		private double totalExpense = 0d;
		private double totalBalance = 0d;
		private String defaultCurrencyCode = "";

		public double getTotalIncome() {
			return totalIncome;
		}

		public void setTotalIncome(double totalIncome) {
			this.totalIncome = totalIncome;
		}

		public double getTotalExpense() {
			return totalExpense;
		}

		public void setTotalExpense(double totalExpense) {
			this.totalExpense = totalExpense;
		}

		public String getDefaultCurrencyCode() {
			return defaultCurrencyCode;
		}

		public void setDefaultCurrencyCode(String defaultCurrencyCode) {
			this.defaultCurrencyCode = defaultCurrencyCode;
		}

		public double getTotalBalance() {
			return totalBalance;
		}

		public void setTotalBalance(double totalBalance) {
			this.totalBalance = totalBalance;
		}
	}

	public void showStatisticInformationAsync() {
		if (getView() != null) {
			ViewFlipper viewFlipper = (ViewFlipper) getView().findViewById(
					R.id.viewFlipperStatistic);
			if (viewFlipper != null) {
				if (viewFlipper.getVisibility() != ViewFlipper.VISIBLE) {
					viewFlipper.setVisibility(ViewFlipper.VISIBLE);
					/*
					 * TextView tvTotal = (TextView) getView().findViewById(
					 * R.id.textViewTotal); if (tvTotal != null) {
					 * tvTotal.setText(getString(R.string.computing)); }
					 */
					if (viewFlipper.getDisplayedChild() != 1) {
						viewFlipper.setDisplayedChild(0);
					}
				} else {
					viewFlipper.setVisibility(ViewFlipper.GONE);
					return;
				}
			}
		}

		AsyncTask<Void, Void, StatisticInfo> task = new AsyncTask<Void, Void, StatisticInfo>() {

			@Override
			protected StatisticInfo doInBackground(Void... params) {
				String defaultCurrencyCode = DataService.GetInstance(
						getActivity()).getDefaultCurrencyCode();
				StatisticInfo result = new StatisticInfo();
				result.setDefaultCurrencyCode(defaultCurrencyCode);
				double totalIncome = 0d;
				double totalExpense = 0d;

				switch (viewType) {
				case DailyTransactionList:
					totalIncome = DataService.GetInstance(getActivity())
							.getTotalEarn(date, date);
					totalExpense = DataService.GetInstance(getActivity())
							.getTotalPay(date, date);
					break;
				case SearchTransactionList:
					totalIncome = DataService.GetInstance(getActivity())
							.getTotalMoneyOfSearchedTransactions(searchKeyword,
									TransactionType.Income);
					totalExpense = DataService.GetInstance(getActivity())
							.getTotalMoneyOfSearchedTransactions(searchKeyword,
									TransactionType.Expense);
					break;
				case FavouriteTransactionList:
					totalIncome = DataService.GetInstance(getActivity())
							.getTotalMoneyOfFavouriteTransactions(
									TransactionType.Income);
					totalExpense = DataService.GetInstance(getActivity())
							.getTotalMoneyOfFavouriteTransactions(
									TransactionType.Expense);
					break;
				case DateRangeTransactionList:
					totalIncome = DataService.GetInstance(getActivity())
							.getTotalEarn(startDate, endDate);
					totalExpense = DataService.GetInstance(getActivity())
							.getTotalPay(startDate, endDate);
					break;
				default:
					break;
				}

				result.setTotalIncome(totalIncome);
				result.setTotalExpense(totalExpense);

				return result;
			}

			@Override
			protected void onPostExecute(StatisticInfo result) {
				if (result != null && getView() != null) {
					ViewFlipper viewFlipper = (ViewFlipper) getView()
							.findViewById(R.id.viewFlipperStatistic);
					if (viewFlipper != null) {
						TextView tvTotalIncome = (TextView) getView()
								.findViewById(R.id.textViewTotalIncome);
						TextView tvTotalExpense = (TextView) getView()
								.findViewById(R.id.textViewTotalExpense);

						if (tvTotalIncome != null) {
							tvTotalIncome.setText(Utility.formatCurrency(
									result.getTotalIncome(),
									result.getDefaultCurrencyCode(), true));
						}
						if (tvTotalExpense != null) {
							tvTotalExpense.setText(Utility.formatCurrency(
									result.getTotalExpense(),
									result.getDefaultCurrencyCode(), true));
						}

						viewFlipper.setDisplayedChild(1);
					}
				}
			}

		};
		task.execute();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

	}

	@Override
	public void onDestroyView() {
		if (mCursor != null && !mCursor.isClosed()) {
			mCursor.close();
			mCursor = null;
		}

		super.onDestroyView();

	}

	private void fillFavouriteTransactionListAsync() {
		AsyncTask<Boolean, Void, Cursor> task = new AsyncTask<Boolean, Void, Cursor>() {

			@Override
			protected Cursor doInBackground(Boolean... params) {
				Cursor result = DataService.GetInstance(getActivity())
						.getStarredBizLog();

				return result;
			}

			@Override
			protected void onPostExecute(Cursor result) {
				showData(result);
			}
		};
		task.execute();
	}

	public void searchAsync(String keyword) {
		AsyncTask<String, Void, Cursor> task = new AsyncTask<String, Void, Cursor>() {

			@Override
			protected Cursor doInBackground(String... params) {
				Cursor cursor = DataService.GetInstance(getActivity())
						.searchBizLog(params[0]);

				return cursor;
			}

			@Override
			protected void onPostExecute(Cursor result) {
				showData(result);
			}
		};
		task.execute(keyword);
	}

	private void fillDataAsync(Date date) {
		AsyncTask<Date, Void, Cursor> task = new AsyncTask<Date, Void, Cursor>() {

			@Override
			protected Cursor doInBackground(Date... dates) {
				Cursor result = null;
				if (dates != null && dates.length > 0) {
					result = DataService.GetInstance(getActivity())
							.getBizLogByDay(dates[0]);
				}

				return result;
			}

			@Override
			protected void onPostExecute(Cursor result) {
				showData(result);
			}
		};
		task.execute(date);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.viewFlipperStatistic:
			hideStatisticPanel();

			break;
		default:
			break;
		}
	}

	private void hideStatisticPanel() {
		ViewFlipper viewFlipper = (ViewFlipper) getView().findViewById(
				R.id.viewFlipperStatistic);
		if (viewFlipper != null
				&& viewFlipper.getVisibility() == ViewFlipper.VISIBLE) {
			viewFlipper.setVisibility(ViewFlipper.GONE);
		}

		if (showCheckBox) {
			this.showMultiCheckBox(false);
		}
	}

	public void changeStar(long transactionId) {
		/*
		 * AsyncTask<Long, Void, Void> task = new AsyncTask<Long, Void, Void>()
		 * {
		 * 
		 * @Override protected Void doInBackground(Long... params) {
		 * DataService.GetInstance(getActivity()).reverseStar(params[0]); return
		 * null; }
		 * 
		 * @Override protected void onPostExecute(Void result) { ListView lv =
		 * (ListView) getView().findViewById( R.id.listViewBizLogByDay); if (lv
		 * != null) { CursorAdapter adpt = (CursorAdapter) lv.getAdapter(); if
		 * (adpt != null) {
		 * 
		 * } } }
		 * 
		 * }; task.execute(transactionId);
		 */
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		if (view.getTag() != null) {
			BizLog transactionInfo = (BizLog) view.getTag();
			if (transactionInfo != null) {
				this.showItemOptionMenu(transactionInfo);
			}
		}

		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (!showCheckBox) {
			showTransactionDetails(id);
		} else {
			CheckBox chkBox = (CheckBox) view
					.findViewById(R.id.checkBoxChecked);
			if (chkBox != null) {
				chkBox.setChecked(!chkBox.isChecked());

				if (chkBox.isChecked()) {
					if (!checkedTransactionIds.contains(id)) {
						checkedTransactionIds.add(id);
					}
				} else {
					if (checkedTransactionIds.contains(id)) {
						checkedTransactionIds.remove(id);
					}
				}

				ListView lv = (ListView) getView().findViewById(
						R.id.listViewBizLogByDay);
				if (lv != null) {
					TransactionListCursorAdapter listAdapter = (TransactionListCursorAdapter) lv
							.getAdapter();
					if (listAdapter != null) {
						listAdapter
								.setCheckedTransactionIds(checkedTransactionIds);
					}
				}

				calculateStatisticInfoOfCheckItemsAsync();
			}
		}
	}

	private void calculateStatisticInfoAsync() {
		AsyncTask<Void, Void, StatisticInfo> task = new AsyncTask<Void, Void, StatisticInfo>() {

			@Override
			protected StatisticInfo doInBackground(Void... params) {
				String defaultCurrencyCode = DataService.GetInstance(
						getActivity()).getDefaultCurrencyCode();
				StatisticInfo result = new StatisticInfo();
				result.setDefaultCurrencyCode(defaultCurrencyCode);
				double totalIncome = 0d;
				double totalExpense = 0d;
				double balance = 0d;

				switch (viewType) {
				case DailyTransactionList:
					totalIncome = DataService.GetInstance(getActivity())
							.getTotalEarn(date, date);
					totalExpense = DataService.GetInstance(getActivity())
							.getTotalPay(date, date);
					break;
				case SearchTransactionList:
					totalIncome = DataService.GetInstance(getActivity())
							.getTotalMoneyOfSearchedTransactions(searchKeyword,
									TransactionType.Income);
					totalExpense = DataService.GetInstance(getActivity())
							.getTotalMoneyOfSearchedTransactions(searchKeyword,
									TransactionType.Expense);
					break;
				case FavouriteTransactionList:
					totalIncome = DataService.GetInstance(getActivity())
							.getTotalMoneyOfFavouriteTransactions(
									TransactionType.Income);
					totalExpense = DataService.GetInstance(getActivity())
							.getTotalMoneyOfFavouriteTransactions(
									TransactionType.Expense);
					break;
				case DateRangeTransactionList:
					totalIncome = DataService.GetInstance(getActivity())
							.getTotalEarn(startDate, endDate);
					totalExpense = DataService.GetInstance(getActivity())
							.getTotalPay(startDate, endDate);
					break;
				default:
					break;
				}
				balance = totalIncome + totalExpense;

				result.setTotalIncome(totalIncome);
				result.setTotalExpense(totalExpense);
				result.setTotalBalance(balance);

				return result;
			}

			@Override
			protected void onPostExecute(StatisticInfo result) {
				if (result != null && getView() != null) {

					TextView tvTotalIncome = (TextView) getView().findViewById(
							R.id.textViewTotalIncomeMoney);
					TextView tvTotalExpense = (TextView) getView()
							.findViewById(R.id.textViewTotalExpenseMoney);
					TextView tvBalance = (TextView) getView().findViewById(
							R.id.textViewTotalBalanceMoney);

					if (tvTotalIncome != null) {
						tvTotalIncome.setText(Utility.formatCurrency(
								result.getTotalIncome(),
								result.getDefaultCurrencyCode(), false));
					}
					if (tvTotalExpense != null) {
						tvTotalExpense.setText(Utility.formatCurrency(
								result.getTotalExpense(),
								result.getDefaultCurrencyCode(), false));
					}

					if (tvBalance != null) {
						if (result.getTotalBalance() > 0) {
							int incomeColor = getActivity().getResources()
									.getColor(R.color.incomeColor);
							tvBalance.setTextColor(incomeColor);
						} else {
							int expenseColor = getActivity().getResources()
									.getColor(R.color.expenseColor);
							tvBalance.setTextColor(expenseColor);
						}
						tvBalance.setText(Utility.formatCurrency(
								result.getTotalBalance(),
								result.getDefaultCurrencyCode(), false));
					}
				}
			}

		};
		task.execute();
	}

	private void calculateStatisticInfoOfCheckItemsAsync() {
		if (checkedTransactionIds.size() == 0) {
			if (getView() != null) {
				ViewFlipper viewFlipper = (ViewFlipper) getView().findViewById(
						R.id.viewFlipperStatistic);
				if (viewFlipper != null) {
					viewFlipper.setDisplayedChild(2);
				}
			}

			return;
		}

		if (getView() != null) {
			ViewFlipper viewFlipper = (ViewFlipper) getView().findViewById(
					R.id.viewFlipperStatistic);
			if (viewFlipper != null) {
				viewFlipper.setVisibility(ViewFlipper.VISIBLE);

				if (viewFlipper.getDisplayedChild() != 1) {
					viewFlipper.setDisplayedChild(0);
				}
			}
		}

		AsyncTask<Void, Void, StatisticInfo> task = new AsyncTask<Void, Void, StatisticInfo>() {

			@Override
			protected StatisticInfo doInBackground(Void... params) {
				String defaultCurrencyCode = DataService.GetInstance(
						getActivity()).getDefaultCurrencyCode();
				StatisticInfo result = new StatisticInfo();
				result.setDefaultCurrencyCode(defaultCurrencyCode);
				double totalIncome = 0d;
				double totalExpense = 0d;

				totalIncome = DataService.GetInstance(getActivity())
						.getTotalMoneyOfTransactionList(checkedTransactionIds,
								TransactionType.Income);
				totalExpense = DataService.GetInstance(getActivity())
						.getTotalMoneyOfTransactionList(checkedTransactionIds,
								TransactionType.Expense);

				result.setTotalIncome(totalIncome);
				result.setTotalExpense(totalExpense);

				return result;
			}

			@Override
			protected void onPostExecute(StatisticInfo result) {
				if (result != null && getView() != null) {
					ViewFlipper viewFlipper = (ViewFlipper) getView()
							.findViewById(R.id.viewFlipperStatistic);
					if (viewFlipper != null) {
						TextView tvTotalIncome = (TextView) getView()
								.findViewById(R.id.textViewTotalIncome);
						TextView tvTotalExpense = (TextView) getView()
								.findViewById(R.id.textViewTotalExpense);

						if (tvTotalIncome != null) {
							tvTotalIncome.setText(Utility.formatCurrency(
									result.getTotalIncome(),
									result.getDefaultCurrencyCode(), true));
						}
						if (tvTotalExpense != null) {
							tvTotalExpense.setText(Utility.formatCurrency(
									result.getTotalExpense(),
									result.getDefaultCurrencyCode(), true));
						}

						TextView tvTotal = (TextView) getView().findViewById(
								R.id.textViewTotal);
						if (tvTotal != null) {
							tvTotal.setText(getString(R.string.total));
						}
						if (viewFlipper.getDisplayedChild() != 1) {
							viewFlipper.setDisplayedChild(1);
						}
					}
				}
			}

		};
		task.execute();
	}

	private void showTransactionDetails(long transactionId) {
		Intent intent = new Intent(getActivity(), TransactionDetails.class);
		intent.putExtra(TransactionDetails.TRANSACTION_ID, transactionId);
		startActivityForResult(intent, REQUEST_CODE);
	}

	public void reload() {
		if (mCursor != null && !mCursor.isClosed()) {
			mCursor.close();
			mCursor = null;
		}

		switch (viewType) {
		case DailyTransactionList:
			this.fillDataAsync(date);
			break;
		case SearchTransactionList:
			this.search(searchKeyword);
			break;
		case FavouriteTransactionList:
			this.showFavouriteTransactionList();
			break;
		case DateRangeTransactionList:
			this.showDateRangeTransactionList(startDate, endDate);
			break;
		default:
			break;
		}

		calculateStatisticInfoAsync();
		notifyDataChanged();
	}

	private void showItemOptionMenu(final BizLog transactionInfo) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(transactionInfo.getStuffName());
		builder.setCancelable(true);
		CharSequence[] items = new CharSequence[] {
				transactionInfo.getStar() == true ? getString(R.string.remove_star)
						: getString(R.string.make_star),
				getString(R.string.modify), getString(R.string.delete),
				getString(R.string.comment),
				getString(R.string.manually_total),
				// getString(R.string.location),
				getString(R.string.back) };
		builder.setItems(items, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case 0: {
					DataService.GetInstance(getActivity()).reverseStar(
							transactionInfo.getId());
					reload();
				}

					break;
				case 1:
					showModifyDetailsDialog(transactionInfo);

					break;
				case 2:
					deleteTransactionRecord(transactionInfo);

					break;
				case 3:
					showCommentDialog(transactionInfo);

					break;
				case 4:
					showMultiCheckBox(!showCheckBox);

					break;
				case 5:
					dialog.dismiss();
					break;
				default:
					break;
				}

			}

			private void showCommentDialog(final BizLog transactionInfo) {
				AlertDialog dlgComment = Utility.showDialog(getActivity(),
						R.layout.comment_editor, getString(R.string.comment),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								AlertDialog dlg = (AlertDialog) dialog;
								EditText txtComment = (EditText) dlg
										.findViewById(R.id.editTextComment);
								String comment = txtComment.getText()
										.toString();

								DataService.GetInstance(getActivity())
										.updateBizLogComment(
												transactionInfo.getId(),
												comment, false);
								reload();
								dialog.dismiss();
							}
						}, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						});
				EditText txtComment = (EditText) dlgComment
						.findViewById(R.id.editTextComment);
				txtComment.setText(transactionInfo.getComment());
				Utility.requestInputMethod(dlgComment);
			}

			private void deleteTransactionRecord(final BizLog transactionInfo) {
				if (transactionInfo != null) {
					Utility.showConfirmDialog(getActivity(), transactionInfo
							.getStuffName(), String.format(
							getString(R.string.delete_item_confirm),
							transactionInfo.getStuffName()),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									DataService.GetInstance(getActivity())
											.deleteBizLogById(
													transactionInfo.getId());
									reload();

									Intent resultIntent = new Intent();
									getActivity().setResult(Activity.RESULT_OK,
											resultIntent);
								}
							});
				}
				;
			}
		});
		builder.show();
	}

	private void showModifyDetailsDialog(final BizLog transactionInfo) {
		AlertDialog dlg = Utility.showDialog(getActivity(),
				R.layout.details_editor,
				getString(R.string.modify_details_info),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						AlertDialog adlg = (AlertDialog) dialog;
						EditText txtStuffName = (EditText) adlg
								.findViewById(R.id.editTextDetailEditor_StuffName);
						EditText txtCost = (EditText) adlg
								.findViewById(R.id.editTextDetailsEditor_Cost);
						RadioButton rbIncome = (RadioButton) adlg
								.findViewById(R.id.radioEarn);
						Button btnCurrencyCode = (Button) adlg
								.findViewById(R.id.buttonChooseCurrency);

						String stuffName = txtStuffName.getText().toString();
						String costString = txtCost.getText().toString();
						String currencyCode = btnCurrencyCode.getText()
								.toString();
						double cost = 0;
						if (TextUtils.isEmpty(stuffName)
								|| stuffName.trim().length() == 0) {
							return;
						}

						if (TextUtils.isEmpty(costString)
								|| costString.trim().length() == 0) {
							return;
						} else {
							try {
								cost = Double.parseDouble(costString);
							} catch (Exception ex) {
								return;
							}
						}

						if (!rbIncome.isChecked()) {
							cost = cost * -1;
						}

						Boolean saveSuccess = false;
						try {
							Date updateTime = Utility
									.replaceWithCurrentTime(transactionInfo
											.getLastUpdateTime());
							DataService.GetInstance(getActivity())
									.updateBizLog(transactionInfo.getId(),
											stuffName, cost, currencyCode,
											updateTime);
							saveSuccess = true;
							Toast.makeText(getActivity(),
									getString(R.string.modify_biz_log_success),
									Toast.LENGTH_SHORT).show();
						} catch (Exception ex) {
							Toast.makeText(getActivity(),
									getString(R.string.save_failed),
									Toast.LENGTH_SHORT).show();
						}

						if (saveSuccess == true) {
							getActivity().setResult(Activity.RESULT_OK);
							reload();
						}
					}

				}, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

		EditText txtStuffName = (EditText) dlg
				.findViewById(R.id.editTextDetailEditor_StuffName);
		EditText txtCost = (EditText) dlg
				.findViewById(R.id.editTextDetailsEditor_Cost);
		Button btnCurrencyCode = (Button) dlg
				.findViewById(R.id.buttonChooseCurrency);
		RadioButton rbIncome = (RadioButton) dlg.findViewById(R.id.radioEarn);
		RadioButton rbPay = (RadioButton) dlg.findViewById(R.id.radioPay);

		txtStuffName.setText(transactionInfo.getStuffName());
		if (transactionInfo.getCost() > 0) {
			rbIncome.setChecked(true);
		} else {
			rbPay.setChecked(true);
		}
		txtCost.setText(String.valueOf(Math.abs(transactionInfo.getCost())));

		if (!TextUtils.isEmpty(transactionInfo.getCurrencyCode())) {
			btnCurrencyCode.setText(transactionInfo.getCurrencyCode());
		}

		Button btnChooseCurrency = (Button) dlg
				.findViewById(R.id.buttonChooseCurrency);
		btnChooseCurrency.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				chooseCurencyCode((Button) v);
			}
		});
	}

	private void chooseCurencyCode(final Button button) {
		String currencyCode = button.getText().toString();
		Helper.chooseDefaultCurrencyCode(getActivity(),
				getString(R.string.currency), currencyCode,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (!TextUtils.isEmpty(Helper.DefaultCurrencyCode)) {
							button.setText(Helper.DefaultCurrencyCode);
						}
					}
				}, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
	}

	public enum ViewType {
		Unknown, DailyTransactionList, SearchTransactionList, FavouriteTransactionList, DateRangeTransactionList
	}

	@Override
	public void onResume() {
		super.onResume();

	}

	private void showMultiCheckBox(boolean visible) {
		this.showCheckBox = visible;

		ListView lv = (ListView) getView().findViewById(
				R.id.listViewBizLogByDay);
		if (lv != null) {
			TransactionListCursorAdapter listAdapter = (TransactionListCursorAdapter) lv
					.getAdapter();
			if (listAdapter != null) {
				listAdapter.setCheckedTransactionIds(checkedTransactionIds);
				listAdapter.allowMultiCheckable(visible);
			}
		}

		ViewFlipper viewFlipper = (ViewFlipper) getView().findViewById(
				R.id.viewFlipperStatistic);
		if (viewFlipper != null) {
			viewFlipper.setVisibility(visible ? ViewFlipper.VISIBLE
					: ViewFlipper.GONE);
			if (visible) {
				viewFlipper.setDisplayedChild(2);
			}
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		switch (viewType) {
		case DailyTransactionList:
			inflater.inflate(R.menu.menu_for_biz_log, menu);
			break;
		case SearchTransactionList:
		case FavouriteTransactionList:
		case DateRangeTransactionList:
			inflater.inflate(R.menu.menu_transaction_list, menu);
			break;
		default:
			break;
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
			case REQUEST_CODE:
				reload();
				break;
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.itemAppendLog:
			this.newTransaction();

			return true;
		case R.id.itemRemoveTodayLog:
			if (date != null) {
				Utility.showConfirmDialog(getActivity(),
						getString(R.string.reset_today_cost), String.format(
								getString(R.string.confirm_reset_today_cost),
								DateUtils.formatDateTime(getActivity(),
										date.getTime(),
										DateUtils.FORMAT_SHOW_DATE)),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								deleteTodayCostHistoryAsync();
							}
						});
			}

			return true;
		case R.id.itemShowStatisticsInfo:
			if (viewType == ViewType.SearchTransactionList
					&& TextUtils.isEmpty(searchKeyword)) {
				return true;
			}
			showStatisticInformationAsync();

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void newTransaction() {
		if (date == null) {
			return;
		}
		Intent intent = new Intent(getActivity(), BizTracker.class);

		Date now = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);

		// Use current time
		cal.set(Calendar.HOUR_OF_DAY, now.getHours());
		cal.set(Calendar.MINUTE, now.getMinutes());
		cal.set(Calendar.SECOND, now.getSeconds());
		Date transactionDate = cal.getTime();

		intent.putExtra(BizTracker.KEY_UPDATE_DATE, transactionDate);
		this.startActivityForResult(intent, REQUEST_CODE);
	}

	private void deleteTodayCostHistoryAsync() {
		if (date == null) {
			return;
		}
		AsyncTask<Boolean, Void, Boolean> task = new AsyncTask<Boolean, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(Boolean... params) {
				DataService.GetInstance(getActivity()).resetHistoryByDate(date);
				return true;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				if (result) {
					getActivity().setResult(Activity.RESULT_OK);
					reload();
				}
			}
		};
		task.execute();

	}

}
