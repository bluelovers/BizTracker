package com.xiaolei.android.preference;

import com.xiaolei.android.BizTracker.R;
import com.xiaolei.android.common.PasswordStrengthChecker;
import com.xiaolei.android.common.PasswordStrengthChecker.PasswordStrength;
import com.xiaolei.android.common.Utility;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.preference.DialogPreference;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class PasswordPreference extends DialogPreference {
	private String mPassword;
	private String mNewPlainTextPassword = "";

	public PasswordPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setDialogLayoutResource(R.layout.password_editor);
	}

	public PasswordPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.setDialogLayoutResource(R.layout.password_editor);
	}

	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);

		final View container = view;
		EditText txtPassword = (EditText) view
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
					mNewPlainTextPassword = s.toString();

					PasswordStrength strength = PasswordStrengthChecker
							.getInstance().EvaluatePasswordStrength(
									s.toString());
					TextView tvWeak = (TextView) container
							.findViewById(R.id.textViewWeak);
					TextView tvMedium = (TextView) container
							.findViewById(R.id.textViewMedium);
					TextView tvStrong = (TextView) container
							.findViewById(R.id.textViewStrong);
					TextView tvBest = (TextView) container
							.findViewById(R.id.textViewBest);
					TextView tvStrength = (TextView) container
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

	/**
	 * Display soft input method when dialog is shown.
	 * 
	 * @return
	 */
	protected boolean needInputMethod() {
		return true;
	}

	/**
	 * Encrypt then save the password.
	 * 
	 * @param plainText
	 *            Plain text.
	 */
	protected void setPassword(String plainText) {
		final boolean wasBlocking = shouldDisableDependents();

		mPassword = encrypt(plainText);

		persistString(mPassword);
		
		//If password changed, change the title
		refreshTitle();

		//Notify dependents change
		final boolean isBlocking = shouldDisableDependents();
		if (isBlocking != wasBlocking) {
			notifyDependencyChange(isBlocking);
		}
	}

	private void refreshTitle() {
		String persisedString = getPersistedString("");
		if (!TextUtils.isEmpty(persisedString)) {
			this.setTitle(String.format("%s (%s)",
					this.getContext().getString(R.string.password), this
							.getContext().getString(R.string.enabled)));
		} else {
			this.setTitle(String.format("%s (%s)",
					this.getContext().getString(R.string.password), this
							.getContext().getString(R.string.disabled)));
		}
	}

	/**
	 * Gets the encrypted password.
	 * 
	 * @return
	 */
	public String getPassword() {
		return mPassword;
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);

		if (positiveResult) {
			if (callChangeListener(mNewPlainTextPassword)) {
				setPassword(mNewPlainTextPassword);
			}
		}
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		super.onSetInitialValue(restoreValue, defaultValue);
		
		refreshTitle();
	}

	@Override
	public boolean shouldDisableDependents() {
		return TextUtils.isEmpty(mPassword) || super.shouldDisableDependents();
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		// Always use empty string as the default password.
		return "";
	}

	protected String encrypt(String plainText) {
		if (!TextUtils.isEmpty(plainText)) {
			return Utility.encrypt(plainText);
		} else {
			return "";
		}
	}
}
