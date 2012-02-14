/**
 * 
 */
package com.xiaolei.android.ui;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.xiaolei.android.BizTracker.DayLogDataAdapter;
import com.xiaolei.android.BizTracker.Helper;
import com.xiaolei.android.BizTracker.R;
import com.xiaolei.android.common.Utility;
import com.xiaolei.android.entity.BizLog;
import com.xiaolei.android.listener.OnNotifyDataChangedListener;
import com.xiaolei.android.service.DataService;

/**
 * @author xiaolei
 * 
 */
public class DailyTransactionListFragment extends Fragment implements
		OnClickListener, OnItemClickListener, OnItemLongClickListener {
	private DailyTransactionListFragment context;
	public static final int REQUEST_CODE = 1118;
	private ViewType viewType = ViewType.Unknown;
	private String searchKeyword = "";
	private Date date = new Date();
	private Date startDate;
	private Date endDate;
	private OnNotifyDataChangedListener onNotifyDataChangedListener;
	private Cursor mCursor = null;

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
	}

	public void showFavouriteTransactionList() {
		super.setHasOptionsMenu(false);

		viewType = ViewType.FavouriteTransactionList;
		fillFavouriteTransactionListAsync();
	}

	public void showDateRangeTransactionList(Date startDate, Date endDate) {
		super.setHasOptionsMenu(false);

		this.startDate = startDate;
		this.endDate = endDate;
		viewType = ViewType.DateRangeTransactionList;

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
		super.setHasOptionsMenu(false);

		searchKeyword = keyword;
		viewType = ViewType.SearchTransactionList;
		searchAsync(keyword);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View result = inflater.inflate(
				R.layout.daily_transaction_list_fragment, container, false);
		context = this;
		if (result != null) {
			ListView lv = (ListView) result
					.findViewById(R.id.listViewBizLogByDay);
			if (lv != null) {
				lv.setOnItemClickListener(this);
				lv.setOnItemLongClickListener(this);
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
		if (result != null) {
			if (result.getCount() > 0) {
				if (getView() != null) {
					ListView lv = (ListView) getView().findViewById(
							R.id.listViewBizLogByDay);
					if (lv != null) {
						DayLogDataAdapter listAdapter = (DayLogDataAdapter) lv
								.getAdapter();

						if (listAdapter == null) {
							listAdapter = new DayLogDataAdapter(getActivity(),
									result, false, context);
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
				Cursor result = DataService.GetInstance(getActivity())
						.searchBizLog(params[0]);

				return result;
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
		long[] tag = (long[]) v.getTag();
		if (tag != null && tag.length == 2) {
			long transactionId = tag[1];
			changeStar(transactionId);
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
		showTransactionDetails(id);
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
				getString(R.string.comment), // getString(R.string.location),
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
					// showGPSLocationDialog();
					dialog.dismiss();

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
}
