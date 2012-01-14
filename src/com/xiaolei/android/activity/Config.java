/**
 * 
 */
package com.xiaolei.android.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.xiaolei.android.BizTracker.ConfigAdapter;
import com.xiaolei.android.BizTracker.R;
import com.xiaolei.android.common.PasswordStrengthChecker;
import com.xiaolei.android.common.PasswordStrengthChecker.PasswordStrength;
import com.xiaolei.android.common.Utility;
import com.xiaolei.android.entity.CurrencySchema;
import com.xiaolei.android.entity.Parameter;
import com.xiaolei.android.entity.ParameterKeys;
import com.xiaolei.android.entity.ParameterUtils;
import com.xiaolei.android.entity.Parameters;
import com.xiaolei.android.service.DataService;

/**
 * @author xiaolei
 * 
 */
public class Config extends Activity implements OnItemClickListener {

	private Config context;
	private ListView lv;
	private String defaultCurrencyCode = "";
	private final int VOLUMN_INC = 5;
	public static final int REQUEST_CODE = 10;
	private String FEEDBACK_EMAIL = "xiaolei.android.feedback@gmail.com";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		context = this;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.config);

		lv = (ListView) findViewById(R.id.listViewConfig);
		lv.setOnItemClickListener(this);

		fillData();
	}

	private void fillData() {
		ConfigAdapter configAdapter = new ConfigAdapter(this);
		lv.setAdapter(configAdapter);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position,
			long arg3) {
		switch (position) {
		case 0:
			showInputPasswordDialog();
			break;
		case 1:
			showDefaultCurrencyDialog();
			break;
		case 2:
			showExchangeRateView();
			break;
		case 3:
			showVolumnConfigDialog();
			break;
		case 4:
			sendFeedback();
			break;
		case 5:
			gotoAndroidMarket();
			break;
		}

	}

	/**
	 * Show the volumn config dialog
	 */
	private void showVolumnConfigDialog() {
		final AlertDialog dlg = Utility.showDialog(context,
				R.layout.volumn_editor, R.drawable.audio_volume_high,
				context.getString(R.string.volumn),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						SeekBar seekBar = (SeekBar) ((AlertDialog) dialog)
								.findViewById(R.id.seekBarVolume);
						int volume = seekBar.getProgress();
						ParameterUtils.saveParameterValue(context,
								Parameter.VOLUME, String.valueOf(volume));
						dialog.dismiss();
					}
				}, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}, null);

		// Get the max audio volumn
		AudioManager audioManager = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		final float streamVolumeMax = audioManager
				.getStreamMaxVolume(AudioManager.STREAM_RING);

		final SeekBar seekBarVolume = (SeekBar) dlg
				.findViewById(R.id.seekBarVolume);
		int savedVolumn = ParameterUtils.getIntParameterValue(context,
				Parameter.VOLUME, (int) streamVolumeMax);
		if (savedVolumn > streamVolumeMax) {
			savedVolumn = (int) streamVolumeMax;
		}

		seekBarVolume.setMax((int) streamVolumeMax);
		seekBarVolume.setKeyProgressIncrement(VOLUMN_INC);
		seekBarVolume.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar sender, int progress,
					boolean fromUser) {
				int percentage = (int) ((progress / streamVolumeMax) * 100);
				dlg.setTitle(context.getString(R.string.volumn) + " ("
						+ String.valueOf(percentage) + "%)");
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}
		});
		seekBarVolume.setProgress(savedVolumn);
	}

	private void gotoAndroidMarket() {
		Utility.goToAndroidMarket(this);
	}

	private void showExchangeRateView() {
		Intent intent = new Intent(this, CurrencySettings.class);
		this.startActivityForResult(intent, 0);
	}

	private void showDefaultCurrencyDialog() {
		Cursor cursor = DataService.GetInstance(this).getAllActiveCurrency();
		if (cursor == null || cursor.getCount() == 0) {
			return;
		}

		String value = "-1";
		int selectedItemIndex = -1;
		Parameter param = DataService.GetInstance(this).getParameterByKey(
				ParameterKeys.DefaultCurrencyCode);
		if (param != null) {
			value = param.getValue();
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.choose_default_currency);
		builder.setCancelable(true);

		// Ok
		builder.setPositiveButton(context.getString(android.R.string.ok),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (!TextUtils.isEmpty(defaultCurrencyCode)) {
							Parameter param = new Parameter();
							param.setKey(ParameterKeys.DefaultCurrencyCode);
							param.setValue(defaultCurrencyCode);
							DataService.GetInstance(context).saveParameter(
									param);
							DataService
									.GetInstance(context)
									.updateEmptyCurrencyCodeToDefaultCurrencyCode();
							context.setResult(RESULT_OK);

							fillData();
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
		if (cursor.moveToFirst()) {
			do {
				String currencyCode = cursor.getString(cursor
						.getColumnIndex(CurrencySchema.Code));
				String currency = String.format("%s (%s)", cursor
						.getString(cursor.getColumnIndex(CurrencySchema.Name)),
						currencyCode);
				strings.add(currency);
				if (selectedItemIndex == -1
						&& value.equalsIgnoreCase(currencyCode)) {
					selectedItemIndex = index;
				}

				index++;
			} while (cursor.moveToNext());
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

	private void sendFeedback() {
		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		emailIntent.setType("message/rfc822");
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
				new String[] { FEEDBACK_EMAIL });
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
				getString(R.string.feedback));
		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "");

		// this.startActivity(Intent.createChooser(emailIntent,
		// getString(R.string.feedback)));
		try {
			this.startActivity(emailIntent);
		} catch (Exception ex) {
			Toast.makeText(this,
					getString(R.string.no_email_send_app_installed),
					Toast.LENGTH_SHORT).show();
		}
	}

	private void showInputPasswordDialog() {
		final AlertDialog dialog = Utility.showDialog(this,
				R.layout.password_editor, R.drawable.lock32,
				getString(R.string.change_password),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						AlertDialog dlg = (AlertDialog) dialog;
						EditText txtPassword = (EditText) dlg
								.findViewById(R.id.editTextNewPassword);

						String password = txtPassword.getText().toString();
						String encryptedPassword = password;
						if (password.length() > 0) {
							encryptedPassword = Utility.toMD5String(password);
						}

						Parameter param = new Parameter();
						param.setKey(Parameters.Password);
						param.setValue(encryptedPassword);
						DataService.GetInstance(context).saveParameter(param);

						fillData();
					}
				}, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}, new DialogInterface.OnShowListener() {

					@Override
					public void onShow(DialogInterface dialog) {
						AlertDialog dlg = (AlertDialog) dialog;
						if (dlg != null) {
							EditText txtPassword = (EditText) dlg
									.findViewById(R.id.editTextNewPassword);
							if (txtPassword != null) {
								try {
									//Show soft keyboard
									InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
									imm.showSoftInput(txtPassword,
											InputMethodManager.SHOW_IMPLICIT);
								} catch (Exception ex) {
								}
							}
						}
					}
				});
		EditText txtPassword = (EditText) dialog
				.findViewById(R.id.editTextNewPassword);
		if (txtPassword != null) {
			txtPassword.addTextChangedListener(new TextWatcher() {

				@Override
				public void afterTextChanged(Editable s) {
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
				}

				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {
					PasswordStrength strength = PasswordStrengthChecker
							.getInstance().EvaluatePasswordStrength(
									s.toString());
					TextView tvWeak = (TextView) dialog
							.findViewById(R.id.textViewWeak);
					TextView tvMedium = (TextView) dialog
							.findViewById(R.id.textViewMedium);
					TextView tvStrong = (TextView) dialog
							.findViewById(R.id.textViewStrong);
					TextView tvBest = (TextView) dialog
							.findViewById(R.id.textViewBest);
					TextView tvStrength = (TextView) dialog
							.findViewById(R.id.textViewStrength);

					tvWeak.setBackgroundColor(Color.parseColor("#EBEBEB"));
					tvMedium.setBackgroundColor(Color.parseColor("#EBEBEB"));
					tvStrong.setBackgroundColor(Color.parseColor("#EBEBEB"));
					tvBest.setBackgroundColor(Color.parseColor("#EBEBEB"));

					tvWeak.setText("");
					tvMedium.setText("");
					tvStrong.setText("");
					tvBest.setText("");
					tvStrength.setText("");

					switch (strength) {
					case WEAK:
						tvWeak.setBackgroundColor(Color.parseColor("#FF4545"));
						tvStrength.setText(R.string.weak);

						break;
					case MEDIUM:
						tvWeak.setBackgroundColor(Color.parseColor("#FFD35E"));
						tvMedium.setBackgroundColor(Color.parseColor("#FFD35E"));
						tvStrength.setText(R.string.medium);

						break;
					case STRONG:
						tvWeak.setBackgroundColor(Color.parseColor("#267A12"));
						tvMedium.setBackgroundColor(Color.parseColor("#267A12"));
						tvStrong.setBackgroundColor(Color.parseColor("#267A12"));
						tvStrength.setText(R.string.strong);

						break;
					case BEST:
						tvWeak.setBackgroundColor(Color.parseColor("#3ABB1C"));
						tvMedium.setBackgroundColor(Color.parseColor("#3ABB1C"));
						tvStrong.setBackgroundColor(Color.parseColor("#3ABB1C"));
						tvBest.setBackgroundColor(Color.parseColor("#3ABB1C"));
						tvStrength.setText(R.string.best);

						break;
					case NOT_RATED:
						tvWeak.setBackgroundColor(Color.parseColor("#FF4545"));
						tvMedium.setBackgroundColor(Color.parseColor("#FFD35E"));
						tvStrong.setBackgroundColor(Color.parseColor("#3ABB1C"));
						tvBest.setBackgroundColor(Color.parseColor("#267A12"));

						tvWeak.setText(R.string.weak);
						tvMedium.setText(R.string.medium);
						tvStrong.setText(R.string.strong);
						tvBest.setText(R.string.best);

						break;
					}
				}
			});
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == 0 && resultCode == RESULT_OK) {
			this.setResult(RESULT_OK);
		}
	}
}
