package com.xiaolei.android.ui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;

import org.json.JSONException;

import com.xiaolei.android.BizTracker.R;
import com.xiaolei.android.BizTracker.StuffsFragmentStatePagerAdapter;
import com.xiaolei.android.common.CurrencyNamesHelper;
import com.xiaolei.android.common.Utility;
import com.xiaolei.android.entity.BizLog;
import com.xiaolei.android.entity.CurrencySchema;
import com.xiaolei.android.entity.Stuff;
import com.xiaolei.android.listener.OnLoadedListener;
import com.xiaolei.android.preference.PreferenceHelper;
import com.xiaolei.android.preference.PreferenceKeys;
import com.xiaolei.android.service.DataService;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.ViewSwitcher;

public class TransactionRecorderFragment extends Fragment implements
		OnClickListener, OnLongClickListener {
	private ViewHolder mViewHolder = new ViewHolder();
	private AsyncTask<Boolean, Integer, Boolean> exportTask;
	private String exportTargetFileName;
	private String mExportErrorMessage = "";
	private ProgressDialog exportProgressDlg;
	private Dialog inputPasswordDlg;
	private String stuffName = "";
	private int stuffId = 0;
	private double cost;

	private SoundPool soundPool;
	private SparseIntArray sounds;
	private float volume = 1.0f;

	private String defaultCurrencyCode = "";
	private Boolean saveDefaultCurrencyCodeToDB = true;

	private double todaySumPay = -1;
	private double todaySumEarn = -1;

	private Date updateDate;

	public static final int REQUEST_CODE = 1;
	public static final String APPLICATION_FOLDER = "BizTracker";
	private final String MULTIPLY = "¡Á";
	private final String DEFAULT_CURRENCY_CODE = "USD";

	public static TransactionRecorderFragment newInstance() {
		TransactionRecorderFragment result = new TransactionRecorderFragment();
		// Bundle args = new Bundle();
		// result.setArguments(args);

		return result;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (getArguments() != null) {

		}

		View result = inflater.inflate(R.layout.transaction_recorder_fragment,
				container, false);
		if (result != null) {
			init(result);
		}

		return result;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		initSoundPoolAsync();
		loadStuffsAsync();
		loadStaticsInfoAsync();
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

		switch (requestCode) {
		case REQUEST_CODE:
			if (resultCode == Activity.RESULT_OK) {
				loadStaticsInfoAsync();
				loadStuffsAsync();
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		loadStaticsInfoAsync();

		if (soundPool == null) {
			initSoundPoolAsync();
		}
	}

	@Override
	public void onPause() {
		super.onPause();

	}

	@Override
	public void onStop() {
		super.onStop();

		if (soundPool != null) {
			soundPool.release();
			soundPool = null;
		}

	}

	@Override
	public boolean onLongClick(View v) {
		switch (v.getId()) {
		case R.id.buttonClear:
			clearCost();
			break;
		// When stuff button long click.
		case -1:
			ViewFlipper viewFlipper = mViewHolder.ViewFlipperMain;
			if (viewFlipper != null) {
				// Display stuff actions view.
				viewFlipper.setDisplayedChild(2);
			}
			break;
		default:
			break;
		}
		return false;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.main_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.itemViewCostHistory:
			viewHistory();

			return true;
		case R.id.itemConfig:
			showSettingsUI();

			return true;
			/*
			 * case R.id.itemControlPanel: this.startActivityForResult(new
			 * Intent(this, ControlPanel.class), ControlPanel.REQUEST_CODE);
			 * 
			 * return true;
			 */
		case R.id.itemExport:
			export();

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onClick(View v) {
		String currentText = null;
		switch (v.getId()) {
		case R.id.buttonNew:
		case R.id.relativeLayoutNoStuff:
			newStuff();

			break;
		case -1:
			// ImageView iv = (ImageView) findViewById(R.id.imageViewPrior);
			Button ib = (Button) v;

			int id = Integer.parseInt(ib.getTag().toString());
			stuffName = ib.getText().toString();
			mViewHolder.TextSwitcherStuffName.setText(stuffName);
			setCurrentStuffId(id);
			// iv.setImageDrawable(ib.getCompoundDrawables()[1]);

			TextView tvPayOrEarn = mViewHolder.TextViewPayOrEarn;
			if (tvPayOrEarn.getText().length() > 0) {
				showNumbersPanel();
			}

			break;
		case R.id.buttonIncome:
			showSymbol("+");
			showNumbersPanel();

			break;
		case R.id.buttonExpense:
			showSymbol("-");
			showNumbersPanel();

			break;
		case R.id.buttonNum0:
		case R.id.buttonNum1:
		case R.id.buttonNum2:
		case R.id.buttonNum3:
		case R.id.buttonNum4:
		case R.id.buttonNum5:
		case R.id.buttonNum6:
		case R.id.buttonNum7:
		case R.id.buttonNum8:
		case R.id.buttonNum9:
		case R.id.buttonNumDot:
			playSound(R.raw.click);

			TextView tv1 = mViewHolder.TextViewCost;
			Button button = (Button) v;
			String originalText = tv1.getText().toString();
			String inputText = button.getText().toString();
			if (originalText.equalsIgnoreCase("0")
					&& !inputText.equalsIgnoreCase(".")) {
				originalText = "";
			}

			if (inputText.equalsIgnoreCase(".")
					&& originalText.indexOf('.') != -1) {
				return;
			}

			if (inputText.equalsIgnoreCase(".")
					&& originalText.indexOf(MULTIPLY) != -1) {
				return;
			}

			tv1.setText(originalText + inputText);

			break;
		case R.id.buttonStuffCount:
			currentText = getCurrentCostText();
			if (!TextUtils.isEmpty(currentText)
					&& currentText.indexOf(MULTIPLY) == -1) {
				appendToCostText(MULTIPLY);
			}

			break;
		case R.id.buttonSave:
			playSound(R.raw.click);
			saveTransaction();

			break;
		case R.id.buttonClear:
			TextView tvPrice = mViewHolder.TextViewCost;
			String price = tvPrice.getText().toString();
			if (price.length() - 1 > 0) {
				price = price.substring(0, price.length() - 1);
			} else {
				price = "0";
			}
			tvPrice.setText(price);

			break;
		case R.id.buttonCancel:
			showStuffPanel();
			break;
		case R.id.buttonDeleteStuff:
			Utility.showConfirmDialog(getActivity(),
					getString(R.string.delete_stuff), String
							.format(getString(R.string.confirm_delete_stuff),
									stuffName),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							DataService.GetInstance(getActivity())
									.deleteStuffById(stuffId);
							loadStuffsAsync();
							clear();
						}
					});

			break;
		case R.id.buttonSearchStuff:
			gotoNextPageStuffs();
			break;
		case R.id.buttonSetCurrency:
			showDefaultCurrencyDialog(false, getString(R.string.currency));
			break;
		case R.id.viewSwitcherPanel:
			viewHistory();
			break;
		case R.id.buttonConfig:
			showSettingsUI();
			break;
		case R.id.buttonExport:
			export();
			break;
		case R.id.buttonViewCostHistory:
			viewHistory();
			break;
		default:
			break;
		}
	}

	private void init(View container) {
		mViewHolder.ViewFlipperMain = (ViewFlipper) container
				.findViewById(R.id.viewFlipperMain);
		mViewHolder.TextViewCost = (TextView) container
				.findViewById(R.id.textViewCost);
		mViewHolder.TextViewPayOrEarn = (TextView) container
				.findViewById(R.id.textViewPayOrEarn);
		mViewHolder.TextViewTopLeft = (TextView) container
				.findViewById(R.id.textViewTopLeft);
		mViewHolder.ViewPaperStuffs = (ViewPager) container
				.findViewById(R.id.viewPaperStuffs);
		mViewHolder.TextViewDefaultCurrencyCode = (TextView) container
				.findViewById(R.id.textViewDefaultCurrencyCode);
		mViewHolder.TextViewTodayTotalCost = (TextSwitcher) container
				.findViewById(R.id.textViewTodayTotalCost);
		mViewHolder.ViewFlipperStuffsPanel = (ViewFlipper) container
				.findViewById(R.id.viewFlipperStuffsPanel);
		mViewHolder.ViewSwitcherPanel = (ViewSwitcher) container
				.findViewById(R.id.viewSwitcherPanel);

		Button btnClear = (Button) container.findViewById(R.id.buttonClear);
		Button btnCancel = (Button) container.findViewById(R.id.buttonCancel);
		Button btnSetCurrency = (Button) container
				.findViewById(R.id.buttonSetCurrency);
		Button btnDeleteStuff = (Button) container
				.findViewById(R.id.buttonDeleteStuff);
		Button btnPay = (Button) container.findViewById(R.id.buttonExpense);
		Button btnEarn = (Button) container.findViewById(R.id.buttonIncome);
		Button btnSearchStuff = (Button) container
				.findViewById(R.id.buttonSearchStuff);
		RelativeLayout relativeLayoutNoStuff = (RelativeLayout) container
				.findViewById(R.id.relativeLayoutNoStuff);
		if (relativeLayoutNoStuff != null) {
			relativeLayoutNoStuff.setOnClickListener(this);
		}

		if (btnPay != null) {
			btnPay.setOnClickListener(this);
		}
		if (btnEarn != null) {
			btnEarn.setOnClickListener(this);
		}
		if (btnSearchStuff != null) {
			btnSearchStuff.setOnClickListener(this);
		}

		if (btnClear != null) {
			btnClear.setLongClickable(true);
			btnClear.setOnLongClickListener(this);
			btnClear.setOnClickListener(this);
		}

		if (btnCancel != null) {
			btnCancel.setOnClickListener(this);
		}

		if (btnSetCurrency != null) {
			btnSetCurrency.setOnClickListener(this);
		}

		if (btnDeleteStuff != null) {
			btnDeleteStuff.setOnClickListener(this);
		}

		defaultCurrencyCode = DataService.GetInstance(getActivity())
				.getDefaultCurrencyCode();
		showDefaultCurrencyCode();

		SharedPreferences prefs = PreferenceHelper
				.getActiveUserSharedPreferences(getActivity());
		if (prefs != null) {
			String password = prefs.getString(PreferenceKeys.Password, "");
			if (password != null && password.length() > 0) {
				showInputPasswordDialog();
			}
		}

		ViewSwitcher topScreen = mViewHolder.ViewSwitcherPanel;

		topScreen.setOnClickListener(this);

		if (TextUtils.isEmpty(defaultCurrencyCode)) {
			showDefaultCurrencyDialog(true, null);
		}
	}

	private void showInputPasswordDialog() {
		inputPasswordDlg = new Dialog(getActivity());
		inputPasswordDlg.requestWindowFeature(Window.FEATURE_LEFT_ICON);
		inputPasswordDlg.setContentView(R.layout.sign_in);
		inputPasswordDlg.setTitle(getString(R.string.input_password));
		inputPasswordDlg.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface arg0) {
				arg0.dismiss();
				getActivity().finish();
			}
		});

		Button btnOk = (Button) inputPasswordDlg
				.findViewById(R.id.buttonInputPasswordOk);
		Button btnCancel = (Button) inputPasswordDlg
				.findViewById(R.id.buttonInputPasswordCancel);

		btnOk.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				EditText txtPassword = (EditText) inputPasswordDlg
						.findViewById(R.id.editTextPassword);
				txtPassword.addTextChangedListener(new TextWatcher() {

					@Override
					public void afterTextChanged(Editable arg0) {
						TextView tvVerifyResult = (TextView) inputPasswordDlg
								.findViewById(R.id.textViewVerifyResult);
						tvVerifyResult.setVisibility(View.GONE);
					}

					@Override
					public void beforeTextChanged(CharSequence arg0, int arg1,
							int arg2, int arg3) {

					}

					@Override
					public void onTextChanged(CharSequence s, int start,
							int before, int count) {

					}
				});
				String password = txtPassword.getText().toString();
				String encryptedPassword = Utility.encrypt(password);

				SharedPreferences prefs = PreferenceHelper
						.getActiveUserSharedPreferences(getActivity());
				if (prefs != null) {
					String value = prefs.getString(PreferenceKeys.Password, "");
					if (!encryptedPassword.equals(value)) {
						EditText txtPwd = (EditText) inputPasswordDlg
								.findViewById(R.id.editTextPassword);
						TextView tvVerifyResult = (TextView) inputPasswordDlg
								.findViewById(R.id.textViewVerifyResult);
						tvVerifyResult.setVisibility(View.VISIBLE);
						tvVerifyResult.setText(getActivity().getString(
								R.string.wrong_password));
						txtPwd.selectAll();
					} else {
						inputPasswordDlg.dismiss();
					}
				} else {
					inputPasswordDlg.dismiss();
					getActivity().finish();
				}
			}
		});

		btnCancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				inputPasswordDlg.dismiss();
				getActivity().finish();
			}
		});

		// Utility.requestInputMethod(inputPasswordDlg);
		inputPasswordDlg.show();
		inputPasswordDlg.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
				R.drawable.lock);
	}

	private void showDefaultCurrencyCode() {
		TextView tvDefaultCurrencyCode = mViewHolder.TextViewDefaultCurrencyCode;

		if (tvDefaultCurrencyCode != null) {
			this.defaultCurrencyCode = DataService.GetInstance(getActivity())
					.getDefaultCurrencyCode();
			tvDefaultCurrencyCode.setText(defaultCurrencyCode);
		}
	}

	private void gotoNextPageStuffs() {
		ViewPager viewPager = mViewHolder.ViewPaperStuffs;

		if (viewPager != null) {
			int totalCount = viewPager.getAdapter().getCount();
			int currentItemIndex = viewPager.getCurrentItem();
			if (currentItemIndex + 1 < totalCount) {
				viewPager.setCurrentItem(currentItemIndex + 1, true);
			} else {
				viewPager.setCurrentItem(0, true);
			}
		}
	}

	private void initSoundPoolAsync() {
		AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(Void... params) {
				initSoundPool();

				return true;
			}

			protected void onPostExecute(Boolean result) {
				// Do nothing
			}

		};
		task.execute();
	}

	private void initSoundPool() {
		try {
			sounds = new SparseIntArray();

			soundPool = new SoundPool(3,
					android.media.AudioManager.STREAM_RING, 0);
			int soundId = soundPool.load(getActivity(), R.raw.click, 1);
			sounds.put(R.raw.click, soundId);

			AudioManager audioManager = (AudioManager) getActivity()
					.getSystemService(Activity.AUDIO_SERVICE);

			float streamVolumeMax = audioManager
					.getStreamMaxVolume(AudioManager.STREAM_RING);

			int streamVolumeCurrent = 1;
			SharedPreferences prefs = PreferenceHelper
					.getActiveUserSharedPreferences(getActivity());
			if (prefs != null) {
				streamVolumeCurrent = prefs.getInt(PreferenceKeys.Volume, 1);
			}

			volume = streamVolumeCurrent / streamVolumeMax;
		} catch (Exception ex) {
			// do nothing
			ex.printStackTrace();
		}
	}

	private void showDefaultCurrencyDialog(Boolean saveToDB, String title) {
		CurrencyNamesHelper currencyNamesHelper = null;
		try {
			currencyNamesHelper = CurrencyNamesHelper
					.getInstance(getActivity());
		} catch (IOException e) {
			Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT)
					.show();
			e.printStackTrace();
		} catch (JSONException e) {
			Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT)
					.show();
			e.printStackTrace();
		}
		Cursor cursor = DataService.GetInstance(getActivity())
				.getAllExchangeRate();
		if (cursor == null || cursor.getCount() == 0) {
			return;
		}

		saveDefaultCurrencyCodeToDB = saveToDB;
		String value = "-1";
		int selectedItemIndex = -1;

		SharedPreferences prefs = PreferenceHelper
				.getActiveUserSharedPreferences(getActivity());
		if (prefs != null) {
			value = prefs.getString(PreferenceKeys.DefaultCurrencyCode, "");
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		if (TextUtils.isEmpty(title)) {
			builder.setTitle(R.string.choose_default_currency);
		} else {
			builder.setTitle(title);
		}
		builder.setCancelable(true);

		// OK
		builder.setPositiveButton(getActivity().getString(android.R.string.ok),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						saveDefaultCurrency();
					}
				});

		// Cancel
		builder.setNegativeButton(
				getActivity().getString(android.R.string.cancel),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						saveDefaultCurrency();

						dialog.dismiss();
					}
				});

		ArrayList<CharSequence> strings = new ArrayList<CharSequence>();

		int index = 0;
		try {
			if (cursor.moveToFirst()) {
				do {
					String currencyCode = cursor.getString(cursor
							.getColumnIndex(CurrencySchema.Code));
					String name = cursor.getString(cursor
							.getColumnIndex(CurrencySchema.Name));
					if (currencyNamesHelper != null) {
						name = currencyNamesHelper.getLocalizedCurrencyName(
								currencyCode, name);
					}
					String currency = String.format("%s (%s)", name,
							currencyCode);

					strings.add(currency);
					if (value != null && selectedItemIndex == -1
							&& value.equalsIgnoreCase(currencyCode)) {
						selectedItemIndex = index;
					}

					index++;
				} while (cursor.moveToNext());
			}
		} finally {
			if (cursor.isClosed() == false) {
				cursor.close();
			}
		}

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
							defaultCurrencyCode = item.substring(
									item.indexOf("(") + 1, item.length() - 1);
						}
					}
				});
		builder.show();
	}

	private void saveDefaultCurrency() {
		if (TextUtils.isEmpty(defaultCurrencyCode)) {
			defaultCurrencyCode = DEFAULT_CURRENCY_CODE;
		}

		TextView tvDefaultCurrencyCode = (TextView) getActivity().findViewById(
				R.id.textViewDefaultCurrencyCode);
		tvDefaultCurrencyCode.setText(defaultCurrencyCode);

		if (saveDefaultCurrencyCodeToDB) {
			SharedPreferences prefs = PreferenceHelper
					.getActiveUserSharedPreferences(getActivity());
			if (prefs != null) {
				Editor editor = prefs.edit();
				editor.putString(PreferenceKeys.DefaultCurrencyCode,
						defaultCurrencyCode);
				editor.commit();
			}

			DataService.GetInstance(getActivity())
					.updateEmptyCurrencyCodeToDefaultCurrencyCode();
			loadStaticsInfoAsync();
		}
	}

	private void loadStaticsInfoAsync() {
		AsyncTask<Integer, Void, Boolean> task = new AsyncTask<Integer, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(Integer... params) {
				boolean valueChanged = false;
				double sumPay = DataService.GetInstance(getActivity())
						.getTodaySumPay();
				double sumEarn = DataService.GetInstance(getActivity())
						.getTodaySumEarn();
				if (sumPay != todaySumPay) {
					valueChanged = true;
					todaySumPay = sumPay;
				}
				if (sumEarn != todaySumEarn) {
					valueChanged = true;
					todaySumEarn = sumEarn;
				}
				defaultCurrencyCode = DataService.GetInstance(getActivity())
						.getDefaultCurrencyCode();

				return valueChanged;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				if (result) {
					refreshTodayCost();
				}
			}
		};

		task.execute();
	}

	private void refreshTodayCost() {
		TextView tvCurrency = mViewHolder.TextViewDefaultCurrencyCode;
		TextSwitcher tvTodayTotalCost = mViewHolder.TextViewTodayTotalCost;
		if (tvCurrency != null) {
			tvCurrency.setText(defaultCurrencyCode);
		}
		if (tvTodayTotalCost != null) {
			tvTodayTotalCost
					.setText(getString(R.string.today_total_pay_and_earn)
							+ " "
							+ this.defaultCurrencyCode
							+ " "
							+ Utility.formatCurrency(todaySumPay,
									defaultCurrencyCode, false)
							+ " / "
							+ Utility.formatCurrency(todaySumEarn,
									defaultCurrencyCode, false));
		}
	}

	private void saveTransaction() {
		TextView tvCost = mViewHolder.TextViewCost;
		TextView tvPayOrEarn = mViewHolder.TextViewPayOrEarn;

		if (tvCost != null && tvPayOrEarn != null) {
			String costText = tvCost.getText().toString();
			String payOrEarnText = tvPayOrEarn.getText().toString();

			int indexOfMultiply = costText.indexOf(MULTIPLY);
			if (indexOfMultiply == costText.length() - 1) {
				Toast.makeText(getActivity(),
						getString(R.string.need_stuff_count), Toast.LENGTH_LONG)
						.show();
				return;
			}

			double cost = 0d;
			int stuffCount = 1;

			try {

				String costValueText = costText;
				if (indexOfMultiply != -1) {
					costValueText = costText.substring(0, indexOfMultiply);
					String stuffCountText = costText
							.substring(indexOfMultiply + 1);
					stuffCount = Integer.parseInt(stuffCountText);
				}

				cost = Double.parseDouble(costValueText);
				if (payOrEarnText.equalsIgnoreCase(getString(R.string.pay))) {
					cost = cost * -1d;
				}

				if (cost != 0) {
					int idOfStuff = this.stuffId;
					showStuffPanel();
					saveAsync(cost, idOfStuff, this.stuffName, stuffCount);
				} else {
					Toast.makeText(getActivity(),
							getString(R.string.input_cost), Toast.LENGTH_SHORT)
							.show();
				}
			} catch (Exception ex) {
				Toast.makeText(getActivity(), getString(R.string.save_fail),
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void export() {
		Utility.showConfirmDialog(getActivity(),
				getString(R.string.export_title),
				getString(R.string.export_confirm),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						exportAsCSV();
					}
				});
	}

	private void exportAsCSV() {
		if (exportTask != null) {
			return;
		}

		exportTask = new AsyncTask<Boolean, Integer, Boolean>() {
			@Override
			protected void onPreExecute() {
				exportProgressDlg = new ProgressDialog(getActivity());
				exportProgressDlg.setCancelable(false);
				exportProgressDlg.setIcon(R.drawable.export);
				exportProgressDlg
						.setTitle(getString(R.string.exporting_to_file));
				exportProgressDlg
						.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				exportProgressDlg
						.setOnDismissListener(new DialogInterface.OnDismissListener() {

							@Override
							public void onDismiss(DialogInterface dialog) {
								if (exportTask != null
										&& !exportTask.isCancelled()) {
									exportTask.cancel(false);
								}

								exportProgressDlg.dismiss();
							}
						});
				exportProgressDlg.show();
			}

			@Override
			protected Boolean doInBackground(Boolean... arg0) {
				mExportErrorMessage = "";
				String state = Environment.getExternalStorageState();
				if (Environment.MEDIA_MOUNTED.equals(state)) {
					String appFolder = Environment
							.getExternalStorageDirectory().getAbsolutePath()
							+ File.separator + APPLICATION_FOLDER + "/backup";
					File targetDir = new File(appFolder);
					if (!targetDir.exists()) {
						boolean success = targetDir.mkdirs();
						if (!success) {
							mExportErrorMessage = "Cannot create folder: "
									+ targetDir;
							return false;
						}
					}

					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
					String fileName = String.format("BizTracker_export_%s.csv",
							format.format(new Date()));
					File targetFile = new File(targetDir, fileName);
					exportTargetFileName = targetDir.getPath() + "/" + fileName;

					try {
						BufferedWriter writer = new BufferedWriter(
								new OutputStreamWriter(new FileOutputStream(
										targetFile), "UTF-16"), 8192);

						// Writer UTF-16 BOM
						// writer.write(0xff);
						// writer.write(0xfe);

						Cursor cursor = DataService.GetInstance(getActivity())
								.getTransactionsForExport();
						try {
							int count = cursor.getCount();
							int index = 0;
							this.publishProgress(count, 0);

							LinkedHashMap<String, String> dic = new LinkedHashMap<String, String>();
							dic.put("StuffName",
									getActivity()
											.getString(R.string.stuff_name));
							dic.put("Cost",
									getActivity().getString(R.string.cost));
							dic.put("StuffCount",
									getActivity().getString(
											R.string.stuff_count));
							dic.put("CurrencyCode",
									getActivity().getString(
											R.string.currency_code));
							dic.put("LastUpdateTime",
									getActivity().getString(
											R.string.last_update_time));
							dic.put("Star",
									getActivity().getString(R.string.star));

							StringBuffer columnNames = new StringBuffer();
							for (int i = 0; i < cursor.getColumnCount(); i++) {
								String colName = cursor.getColumnName(i);
								if (dic.containsKey(colName)) {
									colName = dic.get(colName);
								} else {
									continue;
								}

								if (columnNames.length() > 0) {
									columnNames.append("\t\"" + colName + "\"");
								} else {
									columnNames.append("\"" + colName + "\"");
								}
							}
							columnNames.append("\n");
							writer.append(columnNames.toString());

							if (cursor.moveToFirst()) {
								do {
									if (this.isCancelled()) {
										break;
									}

									StringBuffer line = new StringBuffer();
									for (int colIndex = 0; colIndex < cursor
											.getColumnCount(); colIndex++) {
										String colName = cursor
												.getColumnName(colIndex);
										if (!dic.containsKey(colName)) {
											continue;
										}

										if (line.length() > 0) {
											line.append("\t\""
													+ cursor.getString(colIndex)
													+ "\"");
										} else {
											line.append("\""
													+ cursor.getString(colIndex)
													+ "\"");
										}
									}
									line.append("\n");
									writer.append(line.toString());

									index++;
									this.publishProgress(index);
								} while (cursor.moveToNext());
							}

							writer.flush();
							writer.close();
						} finally {
							cursor.close();
							cursor = null;
						}

						return true;
					} catch (IOException e) {
						mExportErrorMessage = e.getMessage();
						return false;
					}
				} else {
					mExportErrorMessage = getString(R.string.external_storage_not_available);
					return false;
				}
			}

			@Override
			protected void onProgressUpdate(Integer... values) {
				if (exportProgressDlg != null) {
					if (values.length == 1) {
						exportProgressDlg.setProgress(values[0]);
					} else if (values.length == 2) {
						exportProgressDlg.setMax(values[0]);
					}
				}
			}

			@Override
			protected void onPostExecute(Boolean result) {
				if (exportProgressDlg != null) {
					exportProgressDlg.dismiss();
				}

				if (result == true) {
					Utility.showMessageBox(
							getActivity(),
							R.drawable.success,
							getActivity().getString(
									R.string.export_success_title),
							getActivity()
									.getString(R.string.export_target_file)
									+ exportTargetFileName,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							});
				} else {
					String errorMessage = getActivity().getString(
							R.string.export_failed);
					if (!TextUtils.isEmpty(errorMessage)) {
						errorMessage = errorMessage + "\n"
								+ mExportErrorMessage;
					}

					Utility.showMessageBox(getActivity(),
							android.R.drawable.ic_dialog_alert, getActivity()
									.getString(R.string.export_fail_title),
							errorMessage,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							});
				}

				// Clean up
				exportTask = null;
				exportTargetFileName = null;
			}
		};

		exportTask.execute();
	}

	private void newStuff() {

		Utility.showDialog(getActivity(), R.layout.add_stuff,
				getString(R.string.new_stuff),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						AlertDialog alertDialog = (AlertDialog) dialog;
						AutoCompleteTextView txtName = (AutoCompleteTextView) alertDialog
								.findViewById(R.id.editTextStuffName);
						String name = txtName.getText().toString().trim();

						if (!TextUtils.isEmpty(name)) {
							Stuff stuff = new Stuff();
							stuff.setName(name);

							DataService.GetInstance(getActivity()).createStuff(
									stuff);
							txtName.setText("");

							alertDialog.dismiss();
							// fillDataAsync();
							loadStuffsAsync();

						} else {
							alertDialog.dismiss();
						}
					}
				}, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}, new DialogInterface.OnShowListener() {

					@Override
					public void onShow(DialogInterface dialog) {
						AutoCompleteTextView editView = (AutoCompleteTextView) ((AlertDialog) dialog)
								.findViewById(R.id.editTextStuffName);
						if (editView != null) {
							try {
								InputMethodManager imm = (InputMethodManager) getActivity()
										.getSystemService(
												Context.INPUT_METHOD_SERVICE);
								imm.showSoftInput(editView,
										InputMethodManager.SHOW_IMPLICIT);
							} catch (Exception ex) {
							}
						}
					}
				});
	}

	private void loadStuffsAsync() {
		ViewPager stuffViewPager = mViewHolder.ViewPaperStuffs;
		if (stuffViewPager != null) {
			if (stuffViewPager.getAdapter() == null) {
				StuffsFragmentStatePagerAdapter stuffsAdapter = new StuffsFragmentStatePagerAdapter(
						getActivity(), getFragmentManager(), this, this);
				stuffsAdapter
						.setOnLoadedListener(new OnLoadedListener<Integer>() {

							@Override
							public void onLoaded(Integer result) {
								if (result > 0) {
									ViewFlipper viewFlipper = mViewHolder.ViewFlipperStuffsPanel;
									if (viewFlipper != null
											&& viewFlipper.getDisplayedChild() != 1) {
										viewFlipper.setDisplayedChild(1);
									}
								} else {
									ViewFlipper viewFlipper = mViewHolder.ViewFlipperStuffsPanel;
									if (viewFlipper != null) {
										viewFlipper.setDisplayedChild(2);
									}
								}
							}

						});
				stuffViewPager.setAdapter(stuffsAdapter);
			} else {
				StuffsFragmentStatePagerAdapter adpt = (StuffsFragmentStatePagerAdapter) stuffViewPager
						.getAdapter();
				if (adpt != null) {
					stuffViewPager.setCurrentItem(0);
					adpt.notifyDataSetChanged();
				}
			}
		}
	}

	private void saveAsync(double money, int idOfStuff, String stuffName,
			int stuffCount) {
		this.cost = money;
		if (stuffCount == 0) {
			stuffCount = 1;
		}

		AsyncTask<String, Void, String> task = new AsyncTask<String, Void, String>() {

			@Override
			protected String doInBackground(String... params) {
				if (params == null || params.length == 0) {
					return "";
				}

				Integer id = Integer.parseInt(params[0]);
				String name = params[1];
				String currencyCode = params[2];
				int stuffCount = Integer.parseInt(params[3]);

				if (id > 0) {
					BizLog log = new BizLog();
					log.setStuffId(id);
					log.setCost(cost);
					log.setCurrencyCode(currencyCode);
					log.setStuffCount(stuffCount);
					if (updateDate != null) {
						log.setLastUpdateTime(updateDate);
					}

					DataService service = DataService
							.GetInstance(getActivity());
					service.addTransaction(log);
					service.updateLastUsedTime(id);

					todaySumPay = service.getTodaySumPay();
					todaySumEarn = service.getTodaySumEarn();
					// mLastCost = cost;

					return String.format(
							"%s %s: %s%s",
							(cost < 0 ? getActivity().getString(R.string.pay)
									: getActivity().getString(R.string.earn)),
							name,
							Utility.formatCurrency(cost, currencyCode),
							(stuffCount > 1 ? " " + MULTIPLY + " "
									+ String.valueOf(stuffCount) : ""));
				}

				return getActivity().getString(R.string.save_fail);
			}

			@Override
			protected void onPostExecute(String result) {
				if (updateDate != null) {
					clearTopLeftText();
				}

				if (!TextUtils.isEmpty(result)) {
					Toast.makeText(getActivity(), result, Toast.LENGTH_SHORT)
							.show();
					refreshTodayCost();

					// showPopupMessage(mLastCost);

					if (updateDate != null) {
						getActivity().setResult(Activity.RESULT_OK);
						updateDate = null;
						getActivity().finish();
					}
				}
			}
		};

		TextView tvCurrency = mViewHolder.TextViewDefaultCurrencyCode;
		String currency = tvCurrency.getText().toString();
		task.execute(String.valueOf(idOfStuff), stuffName, currency,
				String.valueOf(stuffCount));
	}

	private void clearTopLeftText() {
		TextView tvTopLeft = mViewHolder.TextViewTopLeft;
		if (tvTopLeft != null) {
			tvTopLeft.setText("");
		}
	}

	private void showSymbol(String symbol) {
		TextView tvPayOrEarn = mViewHolder.TextViewPayOrEarn;

		if (symbol.equalsIgnoreCase("-")) {
			tvPayOrEarn.setText(getString(R.string.pay));
		} else if (symbol.equalsIgnoreCase("+")) {
			tvPayOrEarn.setText(getString(R.string.earn));
		} else {
			tvPayOrEarn.setText(symbol);
		}
	}

	private void showNumbersPanel() {
		if (stuffId <= 0) {
			return;
		}
		ViewFlipper vf = mViewHolder.ViewFlipperMain;
		TextView tv1 = mViewHolder.TextViewCost;

		if (vf.getDisplayedChild() == 0) {
			tv1.setText("0");
			vf.showNext();
		}
	}

	private void showStuffPanel() {
		ViewFlipper vf = mViewHolder.ViewFlipperMain;

		clear();

		if (vf != null && vf.getDisplayedChild() != 0) {
			vf.setDisplayedChild(0);
		}
	}

	private void clear() {
		mViewHolder.TextSwitcherStuffName.setText("");
		showSymbol("");
		setCurrentStuffId(0);
		mViewHolder.TextViewCost.setText("0");
	}

	private void setCurrentStuffId(int stuffId) {
		this.stuffId = stuffId;
		// this.btnDeleteStuff.setVisibility(stuffId > 0 ? View.VISIBLE
		// : View.INVISIBLE);
	}

	private void playSound(int resourceId) {
		if (soundPool != null) {
			soundPool.play(sounds.get(resourceId), volume, volume, 1, 0, 1.0f);
		}
	}

	private String getCurrentCostText() {
		String result = "";
		TextView textView = mViewHolder.TextViewCost;
		result = textView.getText().toString();

		return result;
	}

	private void appendToCostText(String text) {
		if (!TextUtils.isEmpty(text)) {
			TextView textView = mViewHolder.TextViewCost;
			if (textView != null) {
				textView.setText(textView.getText() + text);
			}
		}
	}

	private void showSettingsUI() {
		this.startActivityForResult(new Intent(getActivity(),
				com.xiaolei.android.ui.Settings.class),
				com.xiaolei.android.ui.Settings.REQUEST_CODE);
	}

	private void viewHistory() {
		this.startActivity(new Intent(getActivity(), FunctionTypes.class));
	}

	private void clearCost() {
		TextView tvPrice = mViewHolder.TextViewCost;
		if (tvPrice != null) {
			tvPrice.setText("0");
		}
	}

	private final static class ViewHolder {
		private ViewHolder() {
		}

		public ViewFlipper ViewFlipperMain;
		public TextView TextViewCost;
		public TextView TextViewPayOrEarn;
		public TextView TextViewTopLeft;
		public TextSwitcher TextSwitcherStuffName;
		public ViewPager ViewPaperStuffs;
		public TextView TextViewDefaultCurrencyCode;
		public TextSwitcher TextViewTodayTotalCost;
		public ViewFlipper ViewFlipperStuffsPanel;
		public ViewSwitcher ViewSwitcherPanel;
	}
}
