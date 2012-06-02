/**
 * 
 */
package com.xiaolei.android.BizTracker;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.xiaolei.android.common.CurrencyNamesHelper;
import com.xiaolei.android.common.Utility;
import com.xiaolei.android.entity.BizLog;
import com.xiaolei.android.entity.CurrencySchema;
import com.xiaolei.android.event.OnGotResultListener;
import com.xiaolei.android.listener.OnPostExecuteListener;
import com.xiaolei.android.service.DataService;

/**
 * @author xiaolei
 * 
 */
public final class Helper {
	public static String DefaultCurrencyCode = "";

	public static void chooseDefaultCurrencyCode(Context context, String title,
			String checkedCurrencyCode,
			DialogInterface.OnClickListener okButtonClickListener,
			DialogInterface.OnClickListener cancelButtonClickListener) {
		final Cursor cursor = DataService.GetInstance(context)
				.getAllExchangeRate();
		if (cursor == null || cursor.getCount() == 0) {
			cursor.close();
			return;
		}

		CurrencyNamesHelper currencyNamesHelper = null;
		try {
			currencyNamesHelper = CurrencyNamesHelper.getInstance(context);
		} catch (IOException e) {
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		} catch (JSONException e) {
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}

		int selectedItemIndex = -1;
		DefaultCurrencyCode = DataService.GetInstance(context)
				.getDefaultCurrencyCode();

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		if (TextUtils.isEmpty(title)) {
			builder.setTitle(R.string.choose_default_currency);
		} else {
			builder.setTitle(title);
		}
		builder.setCancelable(true);

		// OK
		builder.setPositiveButton(context.getString(android.R.string.ok),
				okButtonClickListener);

		// Cancel
		builder.setNegativeButton(context.getString(android.R.string.cancel),
				okButtonClickListener);

		ArrayList<CharSequence> strings = new ArrayList<CharSequence>();

		int index = 0;
		if (cursor.moveToFirst()) {
			do {
				String currencyCode = cursor.getString(cursor
						.getColumnIndex(CurrencySchema.Code));
				String currencyName = cursor.getString(cursor
						.getColumnIndex(CurrencySchema.Name));
				if (currencyNamesHelper != null) {
					currencyName = currencyNamesHelper
							.getLocalizedCurrencyName(currencyCode,
									currencyName);
				}
				
				String currency = String.format("%s (%s)", currencyName,
						currencyCode);
				strings.add(currency);
				if (selectedItemIndex == -1
						&& !TextUtils.isEmpty(checkedCurrencyCode)
						&& checkedCurrencyCode.equalsIgnoreCase(currencyCode)) {
					selectedItemIndex = index;
				}

				index++;
			} while (cursor.moveToNext());
		}
		cursor.close();

		CharSequence[] items = new CharSequence[0];
		builder.setSingleChoiceItems(strings.toArray(items), selectedItemIndex,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						AlertDialog dlg = (AlertDialog) dialog;
						ListView lv = dlg.getListView();
						if (lv != null) {
							ListAdapter adapter = lv.getAdapter();
							String item = (String) adapter.getItem(which);
							DefaultCurrencyCode = item.substring(
									item.indexOf("(") + 1, item.length() - 1);
						}
					}
				});
		builder.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {

			}
		});
		builder.show();
	}

	public static void showBizLogItemOptionsMenu(final Context context,
			final BizLog log, final OnGotResultListener onGotResultListener) {
		if (log == null || log.getId() <= 0) {
			return;
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(log.getStuffName());
		builder.setCancelable(true);
		CharSequence[] items = new CharSequence[] {
				log.getStar() == true ? context.getString(R.string.remove_star)
						: context.getString(R.string.make_star),
				context.getString(R.string.modify),
				context.getString(R.string.delete),
				context.getString(R.string.comment),
				// context.getString(R.string.choose_project),
				context.getString(R.string.back) };
		builder.setItems(items, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case 0:
					if (log.getStar() == true) {
						log.setStar(false);
						DataService.GetInstance(context)
								.removeStar(log.getId());
					} else {
						log.setStar(true);
						DataService.GetInstance(context).addStar(log.getId());
					}

					if (onGotResultListener != null) {
						onGotResultListener.onGotResult(true, false);
					}

					break;
				case 1:
					showModifyDetailsDialog(context, log, onGotResultListener);

					break;
				case 2:
					deleteTransactionRecord(context, log, onGotResultListener);

					break;
				case 3:
					showCommentDialog(context, log, onGotResultListener);

					break;
				case 4:
					dialog.dismiss();

					break;
				case 5:
					dialog.dismiss();
					break;
				default:
					break;
				}
			}
		});
		builder.show();
	}

	@SuppressWarnings("unused")
	private static void saveProjectTransactionRelation(final Context context,
			final long transactionId, long[] projectIds,
			final OnPostExecuteListener completed) {
		if (projectIds != null && projectIds.length > 0) {
			AsyncTask<long[], Void, Boolean> task = new AsyncTask<long[], Void, Boolean>() {

				@Override
				protected Boolean doInBackground(long[]... projectIdsArray) {
					try {
						for (long[] projectIds : projectIdsArray) {
							for (long projectId : projectIds) {
								DataService.GetInstance(context)
										.addTransactionProjectRelation(
												transactionId, projectId);
							}
						}
					} catch (Exception ex) {
						ex.printStackTrace();
						return false;
					}
					return true;
				}

				@Override
				protected void onPostExecute(Boolean result) {
					if (completed != null) {
						completed.onPostExecute(result);
					}

					if (result == false) {
						Toast.makeText(
								context,
								"Failed to create transaction project relation.",
								Toast.LENGTH_SHORT).show();
					}
				}

			};
			task.execute(projectIds);
		}
	}

	private static void showModifyDetailsDialog(final Context context,
			final BizLog log, final OnGotResultListener onGotResultListener) {

		AlertDialog dlg = Utility.showDialog(context, R.layout.details_editor,
				context.getString(R.string.modify_details_info),
				new OnClickListener() {

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
							DataService.GetInstance(context).updateBizLog(
									log.getId(), stuffName, cost, currencyCode,
									log.getLastUpdateTime());
							saveSuccess = true;
							Toast.makeText(
									context,
									context.getString(R.string.modify_biz_log_success),
									Toast.LENGTH_SHORT).show();
						} catch (Exception ex) {
							Toast.makeText(context,
									context.getString(R.string.save_failed),
									Toast.LENGTH_SHORT).show();
						}

						if (saveSuccess == true) {
							if (onGotResultListener != null) {
								onGotResultListener.onGotResult(true, true);
							}
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

		txtStuffName.setText(log.getStuffName());
		if (log.getCost() > 0) {
			rbIncome.setChecked(true);
		} else {
			rbPay.setChecked(true);
		}
		txtCost.setText(String.valueOf(Math.abs(log.getCost())));

		if (!TextUtils.isEmpty(log.getCurrencyCode())) {
			btnCurrencyCode.setText(log.getCurrencyCode());
		}

		Button btnChooseCurrency = (Button) dlg
				.findViewById(R.id.buttonChooseCurrency);
		btnChooseCurrency.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				chooseCurencyCode(context, (Button) v);
			}
		});
	}

	private static void chooseCurencyCode(Context context, final Button button) {
		String currencyCode = button.getText().toString();
		Helper.chooseDefaultCurrencyCode(context,
				context.getString(R.string.currency), currencyCode,
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

	private static void showCommentDialog(final Context context,
			final BizLog log, final OnGotResultListener onGotResultListener) {
		AlertDialog dlgComment = Utility.showDialog(context,
				R.layout.comment_editor, context.getString(R.string.comment),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						AlertDialog dlg = (AlertDialog) dialog;
						EditText txtComment = (EditText) dlg
								.findViewById(R.id.editTextComment);
						String comment = txtComment.getText().toString();

						DataService.GetInstance(context).updateBizLogComment(
								log.getId(), comment, false);
						if (onGotResultListener != null) {
							onGotResultListener.onGotResult(true, false);
						}

						dialog.dismiss();
					}
				}, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		EditText txtComment = (EditText) dlgComment
				.findViewById(R.id.editTextComment);
		txtComment.setText(log.getComment());
	}

	private static void deleteTransactionRecord(final Context context,
			final BizLog log, final OnGotResultListener onGotResultListener) {

		if (log != null) {
			Utility.showConfirmDialog(context, log.getStuffName(), String
					.format(context.getString(R.string.delete_item_confirm),
							log.getStuffName()), new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					DataService.GetInstance(context).deleteBizLogById(
							log.getId());
					if (onGotResultListener != null) {
						onGotResultListener.onGotResult(true, true);
					}
				}
			});
		}
		;
	}

	public static Dialog showChooseProjectDialog2(final Context context,
			String title, final long transactionId,
			final View.OnClickListener okButtonClickListener) {
		final Dialog dlg = new Dialog(context);
		dlg.setContentView(R.layout.choose_project);
		dlg.setTitle(title);
		dlg.setCancelable(true);
		dlg.setCanceledOnTouchOutside(true);

		Button btnOk = (Button) dlg.findViewById(R.id.buttonOkProject);
		Button btnCancel = (Button) dlg.findViewById(R.id.buttonCancelProject);
		ListView lvProjectList = (ListView) dlg
				.findViewById(R.id.listViewProjectList);

		if (btnOk != null) {
			btnOk.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if (okButtonClickListener != null) {
						okButtonClickListener.onClick(v);
					}
					dlg.dismiss();
				}
			});
		}
		if (btnCancel != null) {
			btnCancel.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					dlg.dismiss();
				}
			});
		}
		if (lvProjectList != null) {
			Cursor cursor = DataService.GetInstance(context).getAllProjects();
			ChooseProjectListAdapter adpt = new ChooseProjectListAdapter(
					context, cursor);
			lvProjectList.setAdapter(adpt);
		}

		dlg.show();
		return dlg;
	}

	public static void showChooseProjectDialog(final Context context,
			String title, final long transactionId,
			DialogInterface.OnClickListener okButtonClickListener,
			DialogInterface.OnClickListener cancelButtonClickListener) {
		final Cursor cursor = DataService.GetInstance(context).getProjects(
				transactionId);
		if (cursor == null || cursor.getCount() == 0) {
			// Toast.makeText(context,
			// context.getString(R.string.no_project_warning),
			// Toast.LENGTH_SHORT).show();
			return;
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		if (TextUtils.isEmpty(title)) {
			builder.setTitle(R.string.project);
		} else {
			builder.setTitle(title);
		}
		builder.setCancelable(true);

		// OK
		builder.setPositiveButton(context.getString(android.R.string.ok),
				okButtonClickListener);

		// Cancel
		builder.setNegativeButton(context.getString(android.R.string.cancel),
				okButtonClickListener);

		builder.setMultiChoiceItems(cursor, "IsChecked", "Name",
				new DialogInterface.OnMultiChoiceClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which,
							boolean isChecked) {

						CursorAdapter adpt = null;
						Cursor currentCursor = null;

						AlertDialog dlg = (AlertDialog) dialog;
						if (dlg != null) {
							ListView lv = dlg.getListView();
							if (lv != null) {
								adpt = (CursorAdapter) lv.getAdapter();

							}
						}

						if (adpt != null) {
							currentCursor = adpt.getCursor();
						}

						if (currentCursor != null
								&& currentCursor.moveToPosition(which)) {
							if (isChecked == true) {
								long projectId = currentCursor.getLong(0);
								DataService.GetInstance(context)
										.addTransactionProjectRelation(
												transactionId, projectId);
							} else {
								long relationId = currentCursor.getLong(1);
								DataService.GetInstance(context)
										.deleteTransactionProjectRelation(
												relationId);
							}
						}

						if (adpt != null) {
							Cursor newCursor = DataService.GetInstance(context)
									.getProjects(transactionId);
							adpt.changeCursor(newCursor);
						}
					}
				});

		builder.show();
	}
}
