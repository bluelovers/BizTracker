/**
 * 
 */
package com.xiaolei.android.ui;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.ViewSwitcher;

import com.xiaolei.android.BizTracker.BizTracker;
import com.xiaolei.android.BizTracker.DayLogDataAdapter;
import com.xiaolei.android.BizTracker.Helper;
import com.xiaolei.android.BizTracker.R;
import com.xiaolei.android.common.Utility;
import com.xiaolei.android.service.DataService;

/**
 * @author xiaolei
 * 
 */
public class TransactionListDaily extends Activity implements
		OnItemLongClickListener, View.OnClickListener {

	private TransactionListDaily context;
	private Cursor cursor;
	private Date date;
	private DayLogDataAdapter listAdapter;
	private View selectedItem;
	private int selecteItemId = 0;
	private double selectedItemCost = 0;
	private String selectedItemName = "";
	private String selectedItemComment = "";
	private String selectedItemCurrencyCode = "USD";
	private String searchKeyword = "";
	private String title = "";
	private Boolean showFullDateTime = false;
	private Boolean getStarredBizLog = false;

	public static final String KEY_DATE = "date";
	public static final String KEY_SHOW_FULL_DATE = "showFullDateTime";
	public static final String KEY_SEARCH_KEYWORD = "searchKeyword";
	public static final String KEY_TITLE = "title";
	public static final String KEY_SHOW_STARRED_RECORDS = "getStarredBizLog";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		context = this;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.day_biz_log);

		date = new Date();
		Date now = new Date();
		try {
			Intent intent = this.getIntent();
			if (intent != null) {
				Bundle extras = intent.getExtras();
				if (extras != null) {
					if (extras.containsKey(KEY_DATE)) {
						date = (Date) extras.get(KEY_DATE);
					}

					if (extras.containsKey(KEY_SHOW_FULL_DATE)) {
						showFullDateTime = extras
								.getBoolean(KEY_SHOW_FULL_DATE);
					}

					if (extras.containsKey(KEY_SEARCH_KEYWORD)) {
						searchKeyword = extras.getString(KEY_SEARCH_KEYWORD);
					}

					if (extras.containsKey(KEY_TITLE)) {
						title = extras.getString(KEY_TITLE);
					}

					if (extras.containsKey(KEY_SHOW_STARRED_RECORDS)) {
						getStarredBizLog = extras
								.getBoolean(KEY_SHOW_STARRED_RECORDS);
					}
				}
			}
		} catch (Exception ex) {
		}

		TextView tvTitle = (TextView) findViewById(R.id.textViewTitle);
		if (TextUtils.isEmpty(title)) {
			if (date.getYear() == now.getYear()
					&& date.getMonth() == now.getMonth()
					&& date.getDay() == now.getDay()) {
				tvTitle.setText(getString(R.string.today_biz));
			} else {
				tvTitle.setText(Utility.toLocalDateString(context, date));
			}
		} else {
			tvTitle.setText(title);
		}

		ListView lv = (ListView) context.findViewById(R.id.listViewBizLogByDay);
		lv.setLongClickable(true);
		lv.setOnItemLongClickListener(this);

		fillDataAsync();
	}

	private void fillDataAsync() {
		AsyncTask<Boolean, Void, Boolean> task = new AsyncTask<Boolean, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(Boolean... params) {
				if (!getStarredBizLog) {
					if (TextUtils.isEmpty(searchKeyword)) {
						cursor = DataService.GetInstance(context)
								.getBizLogByDay(date);
					} else {
						cursor = DataService.GetInstance(context).searchBizLog(
								searchKeyword);
					}
				} else {
					cursor = DataService.GetInstance(context)
							.getStarredBizLog();
				}

				return true;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				if (result == true) {
					if (cursor.getCount() > 0) {
						if (listAdapter == null) {
							listAdapter = new DayLogDataAdapter(context,
									cursor, showFullDateTime, context);
						} else {
							listAdapter.changeCursor(cursor);
						}
						ListView lv = (ListView) context
								.findViewById(R.id.listViewBizLogByDay);
						lv.setAdapter(listAdapter);

						ViewSwitcher viewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcherDayBizLog);
						if (viewSwitcher.getDisplayedChild() != 0) {
							viewSwitcher.showNext();
						}

						ViewSwitcher viewSwitcherContent = (ViewSwitcher) findViewById(R.id.viewSwitcherContent);
						viewSwitcherContent.setDisplayedChild(1);
					} else {
						ViewSwitcher viewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcherDayBizLog);
						viewSwitcher.setDisplayedChild(1);
					}
				}
			}
		};
		task.execute();
	}

	private void showItemOptionMenu(String title, View itemView, int bizLogId) {
		selectedItem = itemView;
		TextView tvStuffName = (TextView) itemView
				.findViewById(R.id.textViewBizItemStuffName);
		TextView tvCost = (TextView) itemView
				.findViewById(R.id.textViewBizItemCost);
		TextView tvCurrencyCode = (TextView) itemView
				.findViewById(R.id.textViewBizItemCurrencyCode);
		TextView tvComment = (TextView) itemView
				.findViewById(R.id.textViewComment);
		ImageView star = (ImageView) itemView
				.findViewById(R.id.imageButtonStarIt);

		selectedItemCost = 0;
		try {
			selectedItemCost = Double.parseDouble(tvCost.getText().toString());
		} catch (Exception ex) {
		}
		selectedItemCurrencyCode = tvCurrencyCode.getText().toString();
		selectedItemName = tvStuffName.getText().toString();
		selectedItemComment = tvComment.getText().toString();
		selecteItemId = bizLogId;
		long[] tag = (long[]) star.getTag();

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		builder.setCancelable(true);
		CharSequence[] items = new CharSequence[] {
				tag[0] == 1 ? getString(R.string.remove_star)
						: getString(R.string.make_star),
				getString(R.string.modify), getString(R.string.delete),
				getString(R.string.comment), // getString(R.string.location),
				getString(R.string.back) };
		builder.setItems(items, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case 0:
					ImageView star = (ImageView) selectedItem
							.findViewById(R.id.imageButtonStarIt);
					star.setImageResource(R.drawable.star);
					long[] tag = (long[]) star.getTag();

					if (tag[0] == 1) {
						tag[0] = 0;
						star.setTag(tag);
						star.setImageResource(R.drawable.star_wb);
						DataService.GetInstance(context).removeStar(
								(int) selecteItemId);
						fillDataAsync();
					} else {
						tag[0] = 1;
						star.setTag(tag);
						star.setImageResource(R.drawable.star);
						DataService.GetInstance(context).addStar(
								(int) selecteItemId);
						fillDataAsync();
					}

					break;
				case 1:
					showModifyDetailsDialog();

					break;
				case 2:
					deleteTransactionRecord();

					break;
				case 3:
					showCommentDialog();

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

			@SuppressWarnings("unused")
			private void showGPSLocationDialog() {
				AlertDialog dlg = Utility.showDialog(context,
						R.layout.gps_location,
						getString(R.string.transaction_location),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								AlertDialog dlg = (AlertDialog) dialog;
								EditText txtLocation = (EditText) dlg
										.findViewById(R.id.editTextTransactionLocation);

								String locationName = txtLocation.getText()
										.toString();
								String location = "";
								if (Utility.LatestLocation != null) {
									location = String.format("%f,%f",
											Utility.LatestLocation
													.getLatitude(),
											Utility.LatestLocation
													.getLongitude());
								}

								DataService.GetInstance(context)
										.updateBizLogLocation(selecteItemId,
												locationName, location);
								dialog.dismiss();
							}
						}, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						});
				Button btnGPS = (Button) dlg.findViewById(R.id.buttonGPS);
				final EditText txtLocation = (EditText) dlg
						.findViewById(R.id.editTextTransactionLocation);
				final ViewFlipper viewFlipper = (ViewFlipper) dlg
						.findViewById(R.id.viewSwitcherGPSLocation);
				viewFlipper.setDisplayedChild(1);

				btnGPS.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View button) {
						viewFlipper.setDisplayedChild(0);

						final Timer timer = new Timer();
						timer.schedule(new TimerTask() {

							@Override
							public void run() {
								if (Utility.LatestLocation != null) {
									timer.cancel();

									AsyncTask<Location, Void, String> task = new AsyncTask<Location, Void, String>() {

										@Override
										protected String doInBackground(
												Location... locations) {
											String result = "";
											if (locations != null
													&& locations.length > 0) {
												Location loc = locations[0];
												Geocoder geo = new Geocoder(
														context, Locale
																.getDefault());
												try {
													List<Address> addresses = geo.getFromLocation(
															loc.getLatitude(),
															loc.getLongitude(),
															1);
													if (addresses != null
															&& addresses.size() > 0) {
														Address address = addresses
																.get(0);
														int maxIndex = address
																.getMaxAddressLineIndex();
														if (maxIndex > 0) {
															StringBuilder text = new StringBuilder();
															for (int i = 0; i <= maxIndex; i++) {
																text.append(address
																		.getAddressLine(i));
															}
															text.append(address
																	.getThoroughfare());
															result = text
																	.toString();
														}
													}
												} catch (Exception e) {
													result = "";
												}
											}

											return result;
										}

										@Override
										protected void onPostExecute(
												String result) {
											if (!TextUtils.isEmpty(result)) {
												viewFlipper
														.setDisplayedChild(1);
												txtLocation.setText(result);
											} else {
												viewFlipper
														.setDisplayedChild(2);
											}
										}
									};

									task.execute(Utility.LatestLocation);
								}
							}
						}, 1000);
					}
				});
			}

			private void showCommentDialog() {
				AlertDialog dlgComment = Utility.showDialog(context,
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

								DataService.GetInstance(context)
										.updateBizLogComment(selecteItemId,
												comment, false);
								context.fillDataAsync();
								dialog.dismiss();
							}
						}, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						}, new DialogInterface.OnShowListener() {

							@Override
							public void onShow(DialogInterface dialog) {
								AlertDialog dlg = (AlertDialog) dialog;
								if (dlg != null) {
									EditText txtComment = (EditText) dlg
											.findViewById(R.id.editTextComment);
									if (txtComment != null) {
										try {
											InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
											imm.showSoftInput(
													txtComment,
													InputMethodManager.SHOW_IMPLICIT);
										} catch (Exception ex) {
										}
									}
								}

							}
						});
				EditText txtComment = (EditText) dlgComment
						.findViewById(R.id.editTextComment);
				txtComment.setText(selectedItemComment);
			}

			private void deleteTransactionRecord() {
				if (selectedItem != null) {
					Utility.showConfirmDialog(context, selectedItemName, String
							.format(getString(R.string.delete_item_confirm),
									selectedItemName), new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							DataService.GetInstance(context).deleteBizLogById(
									selecteItemId);
							fillDataAsync();

							Intent resultIntent = new Intent();
							setResult(RESULT_OK, resultIntent);
						}
					});
				}
				;
			}
		});
		builder.show();
	}

	private void showModifyDetailsDialog() {
		AlertDialog dlg = Utility.showDialog(context, R.layout.details_editor,
				getString(R.string.modify_details_info), new OnClickListener() {

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
									.replaceWithCurrentTime(date);
							DataService.GetInstance(context).updateBizLog(
									context.selecteItemId, stuffName, cost,
									currencyCode, updateTime);
							saveSuccess = true;
							Toast.makeText(context,
									getString(R.string.modify_biz_log_success),
									Toast.LENGTH_SHORT).show();
						} catch (Exception ex) {
							Toast.makeText(context,
									getString(R.string.save_failed),
									Toast.LENGTH_SHORT).show();
						}

						if (saveSuccess == true) {
							context.setResult(RESULT_OK);
							context.fillDataAsync();
						}
					}

				}, new OnClickListener() {

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

		txtStuffName.setText(this.selectedItemName);
		if (this.selectedItemCost > 0) {
			rbIncome.setChecked(true);
		} else {
			rbPay.setChecked(true);
		}
		txtCost.setText(String.valueOf(Math.abs(this.selectedItemCost)));

		if (!TextUtils.isEmpty(selectedItemCurrencyCode)) {
			btnCurrencyCode.setText(selectedItemCurrencyCode);
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
		Helper.chooseDefaultCurrencyCode(this, getString(R.string.currency),
				currencyCode, new DialogInterface.OnClickListener() {

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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!TextUtils.isEmpty(searchKeyword) || getStarredBizLog == true) {
			return true;
		}

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_for_biz_log, menu);

		Date now = new Date();
		if (date != null && date.after(now)) {
			MenuItem item = menu.getItem(0);
			item.setEnabled(false);
		} else {
			MenuItem item = menu.getItem(0);
			item.setEnabled(true);
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.itemAppendLog:
			Intent intent = new Intent(this, BizTracker.class);

			// Use current time
			if (date != null) {
				Date now = new Date();
				Calendar cal = Calendar.getInstance();
				cal.setTime(date);
				cal.set(Calendar.HOUR_OF_DAY, now.getHours());
				cal.set(Calendar.MINUTE, now.getMinutes());
				cal.set(Calendar.SECOND, now.getSeconds());
				date = cal.getTime();
			}

			intent.putExtra("UpdateDate", date);
			this.startActivityForResult(intent, 0);

			return true;
		case R.id.itemRemoveTodayLog:
			Utility.showConfirmDialog(this,
					getString(R.string.reset_today_cost),
					getString(R.string.confirm_reset_today_cost),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							deleteTodayCostHistoryAsync();
						}
					});

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void deleteTodayCostHistoryAsync() {
		AsyncTask<Boolean, Void, Boolean> task = new AsyncTask<Boolean, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(Boolean... params) {
				DataService.GetInstance(context).resetHistoryByDate(date);
				return true;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				if (result) {
					context.setResult(RESULT_OK);
					context.fillDataAsync();
				}
			}
		};
		task.execute();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			this.setResult(RESULT_OK);
			this.fillDataAsync();
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View view, int id,
			long bizLogId) {
		TextView tvTitle = (TextView) view
				.findViewById(R.id.textViewBizItemStuffName);

		Object value = tvTitle.getTag();
		@SuppressWarnings("unused")
		int stuffId = 0;

		if (value != null) {
			stuffId = Integer.parseInt(value.toString());
		}

		if (value == null) {
			this.showItemOptionMenu(tvTitle.getText().toString(), view,
					(int) bizLogId);
		}
		return false;
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
	public void onClick(View v) {
		ImageView ivStar = (ImageView) v;
		if (ivStar != null) {
			long[] tag = (long[]) ivStar.getTag();
			long id = tag[1];

			if (tag != null) {
				if (tag[0] == 1) {
					DataService.GetInstance(context).removeStar(id);
					fillDataAsync();
				} else {
					DataService.GetInstance(context).addStar(id);
					fillDataAsync();
				}
			}
		}
	}
}
