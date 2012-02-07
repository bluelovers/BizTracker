package com.xiaolei.android.BizTracker;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences.Editor;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.ViewSwitcher;

import com.xiaolei.android.common.BaseActivity;
import com.xiaolei.android.common.Utility;
import com.xiaolei.android.entity.BizLog;
import com.xiaolei.android.entity.CurrencySchema;
import com.xiaolei.android.entity.Stuff;
import com.xiaolei.android.preference.PreferenceHelper;
import com.xiaolei.android.preference.PreferenceKeys;
import com.xiaolei.android.service.DataService;
import com.xiaolei.android.ui.FunctionTypes;

public class BizTracker extends BaseActivity implements OnClickListener,
		OnLongClickListener {
	private BizTracker context;
	private Cursor cursor;
	private int stuffId = 0;
	private Button btnDeleteStuff;
	private TableLayout tblStuffs;
	private TextSwitcher txtStuffName;
	private double cost;
	private double todaySumPay = 0;
	private double todaySumEarn = 0;
	private String stuffName = "";
	private DataService service;
	private Date updateDate;
	private Dialog inputPasswordDlg;
	private ProgressDialog exportProgressDlg;

	private AsyncTask<Boolean, Integer, Boolean> exportTask;
	private String exportTargetFileName;
	public static final String KEY_UPDATE_DATE = "UpdateDate";
	private String defaultCurrencyCode = "";
	private Boolean saveDefaultCurrencyCodeToDB = true;

	private SoundPool soundPool;
	private HashMap<Integer, Integer> sounds;
	private float volume = 1.0f;

	public static final int REQUEST_CODE = 1;
	public static final String APPLICATION_FOLDER = "BizTracker";
	public static final String PHOTO_PATH = "BizTracker/photo";

	// private Boolean requestedLocationUpdates = false;
	// private Boolean GPSEnableConfirmDialogShown = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.main);

		context = this;

		Intent intent = this.getIntent();
		if (intent != null) {
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				if (bundle.containsKey(KEY_UPDATE_DATE)) {
					Object value = bundle.get(KEY_UPDATE_DATE);
					if (value != null) {
						try {
							updateDate = (Date) value;
							TextView tvTopLeft = (TextView) findViewById(R.id.textViewTopLeft);
							if (tvTopLeft != null) {
								tvTopLeft
										.setText(getString(R.string.transaction_date)
												+ ": "
												+ DateUtils.formatDateTime(
														context,
														updateDate.getTime(),
														DateUtils.FORMAT_SHOW_DATE));
							}
						} catch (Exception ex) {
						}
					}
				}
			}
		}

		service = DataService.GetInstance(this);
		Button buttonNew = (Button) findViewById(R.id.buttonNew);
		if (buttonNew != null) {
			buttonNew.setOnClickListener(this);
		}

		TableLayout tblNumbers = (TableLayout) findViewById(R.id.tableLayoutNumbersPanel);
		if (tblNumbers != null) {
			for (int i = 0; i < tblNumbers.getChildCount(); i++) {
				TableRow row = (TableRow) tblNumbers.getChildAt(i);
				for (int j = 0; j < row.getChildCount(); j++) {
					View button = row.getChildAt(j);
					button.setOnClickListener(this);
				}
			}
		}

		tblStuffs = (TableLayout) findViewById(R.id.tableLayoutStuffPanel);
		if (tblStuffs != null) {
			for (int i = 0; i < tblStuffs.getChildCount(); i++) {
				TableRow row = (TableRow) tblStuffs.getChildAt(i);
				for (int j = 0; j < row.getChildCount(); j++) {
					View button = row.getChildAt(j);
					button.setOnClickListener(null);
					button.setOnClickListener(this);
				}
			}
		}

		Button btnClear = (Button) findViewById(R.id.buttonClear);
		Button btnCancel = (Button) findViewById(R.id.buttonCancel);
		Button btnSetCurrency = (Button) findViewById(R.id.buttonSetCurrency);
		btnDeleteStuff = (Button) findViewById(R.id.buttonDeleteStuff);
		txtStuffName = (TextSwitcher) findViewById(R.id.textSwitcherStuffName);
		Button btnPay = (Button) findViewById(R.id.buttonExpense);
		Button btnEarn = (Button) findViewById(R.id.buttonIncome);
		Button btnSearchStuff = (Button) findViewById(R.id.buttonSearchStuff);
		TextView tvDefaultCurrencyCode = (TextView) this
				.findViewById(R.id.textViewDefaultCurrencyCode);
		RelativeLayout relativeLayoutNoStuff = (RelativeLayout) findViewById(R.id.relativeLayoutNoStuff);
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
		
		PreferenceHelper.createActiveUserRelatedPreferencesIfNeeds(this);
		PreferenceHelper.migrateOldPreferencesIfNeed(this);

		this.defaultCurrencyCode = DataService.GetInstance(this)
				.getDefaultCurrencyCode();
		if (tvDefaultCurrencyCode != null) {
			tvDefaultCurrencyCode.setText(defaultCurrencyCode);
		}
		
		SharedPreferences prefs = PreferenceHelper
				.getActiveUserSharedPreferences(this);
		if (prefs != null) {
			String password = prefs.getString(PreferenceKeys.Password, "");
			if (password != null && password.length() > 0) {
				showInputPasswordDialog();
			}
		}

		loadStuffsAsync();
		loadStaticsInfoAsync();
		// fillDataAsync();

		ViewSwitcher topScreen = (ViewSwitcher) findViewById(R.id.viewSwitcherPanel);
		topScreen.setOnClickListener(this);

		if (TextUtils.isEmpty(defaultCurrencyCode)) {
			showDefaultCurrencyDialog(true, null);
		}

		loadAsync();
		// initLocationHelper();
	}

	/*
	 * private void initLocationHelper(){
	 * LocationHelper.getInstance(getApplicationContext(), new
	 * LocationChangedListener(){
	 * 
	 * @Override public void onLocationChanged(Location location) {
	 * Utility.LatestLocation = location; } }).requestLocationUpdates(); }
	 */

	@Override
	protected void load() {
		sounds = new HashMap<Integer, Integer>();

		soundPool = new SoundPool(3, android.media.AudioManager.STREAM_RING, 0);
		int soundId = soundPool.load(this, R.raw.click, 1);
		sounds.put(R.raw.click, soundId);

		AudioManager audioManager = (AudioManager) this
				.getSystemService(AUDIO_SERVICE);

		float streamVolumeMax = audioManager
				.getStreamMaxVolume(AudioManager.STREAM_RING);

		int streamVolumeCurrent = 1;
		SharedPreferences prefs = PreferenceHelper
				.getActiveUserSharedPreferences(this);
		if (prefs != null) {
			streamVolumeCurrent = prefs.getInt(PreferenceKeys.Volume, 1);
		}

		volume = streamVolumeCurrent / streamVolumeMax;
	}

	private void showDefaultCurrencyDialog(Boolean saveToDB, String title) {
		Cursor cursor = DataService.GetInstance(this).getAllActiveCurrency();
		if (cursor == null || cursor.getCount() == 0) {
			return;
		}

		saveDefaultCurrencyCodeToDB = saveToDB;
		String value = "-1";
		int selectedItemIndex = -1;

		SharedPreferences prefs = PreferenceHelper
				.getActiveUserSharedPreferences(this);
		if (prefs != null) {
			value = prefs.getString(PreferenceKeys.DefaultCurrencyCode, "");
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		if (TextUtils.isEmpty(title)) {
			builder.setTitle(R.string.choose_default_currency);
		} else {
			builder.setTitle(title);
		}
		builder.setCancelable(true);

		// OK
		builder.setPositiveButton(context.getString(android.R.string.ok),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						TextView tvDefaultCurrencyCode = (TextView) context
								.findViewById(R.id.textViewDefaultCurrencyCode);
						tvDefaultCurrencyCode.setText(defaultCurrencyCode);

						if (saveDefaultCurrencyCodeToDB) {
							SharedPreferences prefs = PreferenceHelper
									.getActiveUserSharedPreferences(context);
							if (prefs != null) {
								Editor editor = prefs.edit();
								editor.putString(
										PreferenceKeys.DefaultCurrencyCode,
										defaultCurrencyCode);
								editor.commit();
							}

							DataService
									.GetInstance(context)
									.updateEmptyCurrencyCodeToDefaultCurrencyCode();
							loadStaticsInfoAsync();
						}
					}
				});

		// Cancel
		builder.setNegativeButton(context.getString(android.R.string.cancel),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
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
					String currency = String.format("%s (%s)", cursor
							.getString(cursor
									.getColumnIndex(CurrencySchema.Name)),
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

	private void loadStaticsInfoAsync() {
		AsyncTask<Integer, Void, Boolean> task = new AsyncTask<Integer, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(Integer... params) {
				todaySumPay = DataService.GetInstance(context).getTodaySumPay();
				todaySumEarn = DataService.GetInstance(context)
						.getTodaySumEarn();
				defaultCurrencyCode = DataService.GetInstance(context)
						.getDefaultCurrencyCode();

				return true;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				if (result) {
					context.refreshTodayCost();
				}
			}
		};

		task.execute();
	}

	private void loadStuffsAsync() {
		AsyncTask<Integer, Void, Boolean> task = new AsyncTask<Integer, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(Integer... params) {
				if (cursor != null) {
					cursor.close();
					cursor = null;
				}

				cursor = DataService.GetInstance(context).getAllStuffs();
				return true;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				if (result) {
					if (cursor != null && cursor.isClosed() == false) {
						if (cursor.getCount() > 0) {
							bindStuffsDataSource();

							ViewFlipper viewFlipper = (ViewFlipper) context
									.findViewById(R.id.viewFlipperStuffsPanel);
							if (viewFlipper != null
									&& viewFlipper.getDisplayedChild() != 1) {
								viewFlipper.setDisplayedChild(1);
							}
						} else {
							ViewFlipper viewFlipper = (ViewFlipper) context
									.findViewById(R.id.viewFlipperStuffsPanel);
							if (viewFlipper != null) {
								viewFlipper.setDisplayedChild(2);
							}
						}
					}

				}
			}
		};

		task.execute();
	}

	private void bindStuffsDataSource() {
		if (cursor != null && cursor.isClosed() == false) {
			ViewPager stuffViewPager = (ViewPager) findViewById(R.id.viewPaperStuffs);
			if (stuffViewPager != null) {
				StuffsPagerAdapter stuffsAdapter = new StuffsPagerAdapter(this,
						cursor, this);
				stuffViewPager.setAdapter(stuffsAdapter);
			}
		}
	}

	private void refreshTodayCost() {
		TextView tvCurrency = (TextView) findViewById(R.id.textViewDefaultCurrencyCode);
		TextSwitcher tvTodayTotalCost = (TextSwitcher) findViewById(R.id.textViewTodayTotalCost);
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

	private void setCurrentStuffId(int stuffId) {
		this.stuffId = stuffId;
		this.btnDeleteStuff.setVisibility(stuffId > 0 ? View.VISIBLE
				: View.INVISIBLE);
	}

	private void playSound(int resourceId) {
		if (soundPool != null) {
			soundPool.play(sounds.get(resourceId), volume, volume, 1, 0, 1.0f);
		}
	}

	@Override
	public void onClick(View v) {
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
			txtStuffName.setText(stuffName);
			setCurrentStuffId(id);
			// iv.setImageDrawable(ib.getCompoundDrawables()[1]);

			TextView tvPayOrEarn = (TextView) findViewById(R.id.textViewPayOrEarn);
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

			TextView tv1 = (TextView) findViewById(R.id.textViewCost);
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

			tv1.setText(originalText + inputText);

			break;
		case R.id.buttonSave:
			playSound(R.raw.click);

			TextView tvCost = (TextView) findViewById(R.id.textViewCost);
			TextView tvPayOrEarn1 = (TextView) findViewById(R.id.textViewPayOrEarn);

			double cost = 0d;
			try {
				cost = Double.parseDouble(tvCost.getText().toString());
				if (tvPayOrEarn1.getText().toString()
						.equalsIgnoreCase(getString(R.string.pay))) {
					cost = cost * -1d;
				}

				if (cost != 0) {
					int idOfStuff = this.stuffId;
					showStuffPanel();
					saveAsync(cost, idOfStuff, this.stuffName);
				} else {
					Toast.makeText(context, getString(R.string.input_cost),
							Toast.LENGTH_SHORT).show();
				}
			} catch (Exception ex) {
				tvPayOrEarn1.setText(getString(R.string.error));
				Toast.makeText(context, getString(R.string.save_fail),
						Toast.LENGTH_SHORT).show();
				return;
			}

			break;
		case R.id.buttonClear:
			TextView tvPrice = (TextView) findViewById(R.id.textViewCost);
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
			Utility.showConfirmDialog(this, getString(R.string.delete_stuff),
					String.format(getString(R.string.confirm_delete_stuff),
							stuffName), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							DataService.GetInstance(context).deleteStuffById(
									stuffId);
							loadStuffsAsync();
							clear();
						}
					});

			break;
		case R.id.buttonSearchStuff:
			// TO-DO: search stuff
			gotoNextPageStuffs();
			break;
		case R.id.buttonSetCurrency:
			this.showDefaultCurrencyDialog(false, getString(R.string.currency));
			break;
		case R.id.viewSwitcherPanel:
			viewHistory();
			break;
		default:
			break;
		}
	}

	private void gotoNextPageStuffs() {
		ViewPager viewPager = (ViewPager) findViewById(R.id.viewPaperStuffs);
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

	private void newStuff() {

		Utility.showDialog(this, R.layout.add_stuff,
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

							DataService.GetInstance(context).createStuff(stuff);
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
								InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
								imm.showSoftInput(editView,
										InputMethodManager.SHOW_IMPLICIT);
							} catch (Exception ex) {
							}
						}
					}
				});
		/*
		 * Stuff stuff = new Stuff(); stuff.setName("Test"); stuff.setPicture(
		 * "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"
		 * ); DataService.GetInstance(context).createStuff(stuff);
		 * fillDataAsync();
		 */
	}

	private void saveAsync(double money, int idOfStuff, String stuffName) {
		this.cost = money;

		AsyncTask<String, Void, String> task = new AsyncTask<String, Void, String>() {

			@Override
			protected String doInBackground(String... params) {
				if (params == null || params.length == 0) {
					return "";
				}

				Integer id = Integer.parseInt(params[0]);
				String name = params[1];
				String currencyCode = params[2];

				if (id > 0) {
					BizLog log = new BizLog();
					log.setStuffId(id);
					log.setCost(cost);
					log.setCurrencyCode(currencyCode);
					if (context.updateDate != null) {
						log.setLastUpdateTime(context.updateDate);
					}

					service.addBizLog(log);
					service.updateLastUsedTime(id);

					todaySumPay = service.getTodaySumPay();
					todaySumEarn = service.getTodaySumEarn();
					return String.format("%s%s: %s",
							(cost < 0 ? context.getString(R.string.pay)
									: context.getString(R.string.earn)), name,
							Utility.formatCurrency(cost, currencyCode));
				}

				return context.getString(R.string.save_fail);
			}

			@Override
			protected void onPostExecute(String result) {
				if (context.updateDate != null) {
					clearTopLeftText();
				}

				if (!TextUtils.isEmpty(result)) {
					Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
					refreshTodayCost();

					if (context.updateDate != null) {
						context.setResult(RESULT_OK);
						context.updateDate = null;
						context.finish();
					}
				}
			}
		};

		TextView tvCurrency = (TextView) this
				.findViewById(R.id.textViewDefaultCurrencyCode);
		String currency = tvCurrency.getText().toString();
		task.execute(String.valueOf(idOfStuff), stuffName, currency);
	}

	private void clearTopLeftText() {
		TextView tvTopLeft = (TextView) findViewById(R.id.textViewTopLeft);
		if (tvTopLeft != null) {
			tvTopLeft.setText("");
		}
	}

	private void showSymbol(String symbol) {
		TextView tvPayOrEarn = (TextView) findViewById(R.id.textViewPayOrEarn);

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
		ViewFlipper vf = (ViewFlipper) findViewById(R.id.viewFlipperMain);
		TextView tv1 = (TextView) findViewById(R.id.textViewCost);

		if (vf.getDisplayedChild() == 0) {
			tv1.setText("0");
			vf.showNext();
		}
	}

	private void showStuffPanel() {
		ViewFlipper vf = (ViewFlipper) findViewById(R.id.viewFlipperMain);
		clear();

		if (vf.getDisplayedChild() == 1) {
			vf.showPrevious();
		}
	}

	private void clear() {
		this.txtStuffName.setText("");
		showSymbol("");
		this.setCurrentStuffId(0);
		TextView tvCost = (TextView) findViewById(R.id.textViewCost);
		tvCost.setText("0");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.itemViewCostHistory:
			viewHistory();

			return true;
		case R.id.itemConfig:
			this.startActivityForResult(new Intent(this,
					com.xiaolei.android.ui.Settings.class),
					com.xiaolei.android.ui.Settings.REQUEST_CODE);

			return true;
			/*
			 * case R.id.itemControlPanel: this.startActivityForResult(new
			 * Intent(this, ControlPanel.class), ControlPanel.REQUEST_CODE);
			 * 
			 * return true;
			 */
		case R.id.itemExport:
			Utility.showConfirmDialog(this, getString(R.string.export_title),
					getString(R.string.export_confirm),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							export();
						}
					});

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void viewHistory() {
		this.startActivity(new Intent(this, FunctionTypes.class));
	}

	@Override
	public boolean onLongClick(View v) {
		switch (v.getId()) {
		case R.id.buttonClear:
			clearCost();
			break;
		}
		return false;
	}

	private void export() {
		if (exportTask != null) {
			return;
		}

		exportTask = new AsyncTask<Boolean, Integer, Boolean>() {
			@Override
			protected void onPreExecute() {
				exportProgressDlg = new ProgressDialog(context);
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
				String state = Environment.getExternalStorageState();
				if (Environment.MEDIA_MOUNTED.equals(state)) {
					File targetDir = new File(
							Environment.getExternalStorageDirectory() + "/"
									+ APPLICATION_FOLDER + "/backup");
					if (!targetDir.exists()) {
						targetDir.mkdir();
					}

					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
					String fileName = String.format("BizTracker_export_%s.csv",
							format.format(new Date()));
					File targetFile = new File(targetDir, fileName);
					exportTargetFileName = targetDir.getPath() + "/" + fileName;

					try {
						BufferedWriter writer = new BufferedWriter(
								new OutputStreamWriter(new FileOutputStream(
										targetFile), "UTF-8"), 8192);

						Cursor cursor = DataService.GetInstance(context)
								.getAllBizLog();
						int count = cursor.getCount();
						int index = 0;
						this.publishProgress(count, 0);

						Hashtable<String, String> dic = new Hashtable<String, String>();
						dic.put("StuffName",
								context.getString(R.string.stuff_name));
						dic.put("Cost", context.getString(R.string.cost));
						dic.put("LastUpdateTime",
								context.getString(R.string.last_update_time));
						dic.put("Star", context.getString(R.string.star));
						dic.put("CurrencyCode",
								context.getString(R.string.currency_code));

						StringBuffer columnNames = new StringBuffer();
						for (int i = 0; i < cursor.getColumnCount(); i++) {
							String colName = cursor.getColumnName(i);
							if (dic.containsKey(colName)) {
								colName = dic.get(colName);
							}

							if (columnNames.length() > 0) {
								columnNames.append("," + colName);
							} else {
								columnNames.append(colName);
							}
						}
						columnNames.append("\n");
						writer.append(columnNames.toString());

						while (cursor.moveToNext()) {
							if (this.isCancelled()) {
								break;
							}

							StringBuffer line = new StringBuffer();
							for (int colIndex = 0; colIndex < cursor
									.getColumnCount(); colIndex++) {
								if (line.length() > 0) {
									line.append(","
											+ cursor.getString(colIndex));
								} else {
									line.append(cursor.getString(colIndex));
								}
							}
							line.append("\n");
							writer.append(line.toString());

							index++;
							this.publishProgress(index);
						}

						writer.flush();
						writer.close();

						return true;
					} catch (IOException e) {
						return false;
					}
				} else {
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
					Utility.showMessageBox(context, R.drawable.success,
							context.getString(R.string.export_success_title),
							context.getString(R.string.export_target_file)
									+ exportTargetFileName,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							});
				} else {
					Utility.showMessageBox(context,
							android.R.drawable.ic_dialog_alert,
							context.getString(R.string.export_fail_title),
							context.getString(R.string.export_failed),
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

	private void clearCost() {
		TextView tvPrice = (TextView) findViewById(R.id.textViewCost);
		tvPrice.setText("0");
	}

	@Override
	public void onBackPressed() {
		ViewFlipper vf = (ViewFlipper) findViewById(R.id.viewFlipperMain);

		if (vf.getDisplayedChild() == 1) {
			this.showStuffPanel();
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public boolean onSearchRequested() {
		FrameLayout stuffSearchBar = (FrameLayout) findViewById(R.id.frameLayoutStuffSearchBar);
		if (stuffSearchBar != null) {
			if (stuffSearchBar.getVisibility() == FrameLayout.VISIBLE) {
				stuffSearchBar.setVisibility(FrameLayout.INVISIBLE);
			} else {
				stuffSearchBar.setVisibility(FrameLayout.VISIBLE);
			}
		}

		return true;
	}

	private void showInputPasswordDialog() {
		inputPasswordDlg = new Dialog(this);
		inputPasswordDlg.requestWindowFeature(Window.FEATURE_LEFT_ICON);
		inputPasswordDlg.setContentView(R.layout.sign_in);
		inputPasswordDlg.setTitle(getString(R.string.input_password));
		inputPasswordDlg.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface arg0) {
				arg0.dismiss();
				finish();
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
						tvVerifyResult.setVisibility(View.INVISIBLE);
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
						.getActiveUserSharedPreferences(context);
				if (prefs != null) {
					String value = prefs.getString(PreferenceKeys.Password, "");
					if (!encryptedPassword.equals(value)) {
						EditText txtPwd = (EditText) inputPasswordDlg
								.findViewById(R.id.editTextPassword);
						TextView tvVerifyResult = (TextView) inputPasswordDlg
								.findViewById(R.id.textViewVerifyResult);
						tvVerifyResult.setVisibility(View.VISIBLE);
						tvVerifyResult.setText(context
								.getString(R.string.wrong_password));
						txtPwd.selectAll();
					} else {
						inputPasswordDlg.dismiss();
					}
				} else {
					inputPasswordDlg.dismiss();
					finish();
				}
			}
		});

		btnCancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				inputPasswordDlg.dismiss();
				finish();
			}
		});

		//Utility.requestInputMethod(inputPasswordDlg);
		inputPasswordDlg.show();
		inputPasswordDlg.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
				R.drawable.lock);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case REQUEST_CODE:
			if (resultCode == RESULT_OK) {
				loadStaticsInfoAsync();
				loadStuffsAsync();
			}
			break;
		default:
			break;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		loadStaticsInfoAsync();

		if (soundPool == null) {
			this.loadAsync();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

	}

	@Override
	protected void onStop() {
		super.onStop();

		if (soundPool != null) {
			soundPool.release();
			soundPool = null;
		}

	}
}