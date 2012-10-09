package com.xiaolei.android.BizTracker;

import java.util.Date;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.xiaolei.android.common.Utility;
import com.xiaolei.android.entity.Stuff;
import com.xiaolei.android.listener.OnStuffIdChangedListener;
import com.xiaolei.android.preference.PreferenceHelper;
import com.xiaolei.android.service.DataService;
import com.xiaolei.android.ui.TransactionRecorderFragment;

public class BizTracker extends FragmentActivity implements OnClickListener,
		OnStuffIdChangedListener {
	private BizTracker context;
	private Date updateDate;
	private TransactionRecorderFragment mTransactionRecorderFragment;
	private Button mButtonDeleteStuff;

	public static final String KEY_UPDATE_DATE = "UpdateDate";
	public static final int REQUEST_CODE = 1;
	public static final String APPLICATION_FOLDER = "BizTracker";
	public static final String PHOTO_PATH = "BizTracker/photo";

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

		PreferenceHelper.createActiveUserRelatedPreferencesIfNeeds(this);
		PreferenceHelper.migrateOldPreferencesIfNeed(this);

		Button buttonNew = (Button) findViewById(R.id.buttonNew);
		if (buttonNew != null) {
			buttonNew.setOnClickListener(this);
		}

		mButtonDeleteStuff = (Button) findViewById(R.id.buttonDeleteStuff);
		if (mButtonDeleteStuff != null) {
			mButtonDeleteStuff.setOnClickListener(this);
		}

		FragmentManager fragmentManager = this.getSupportFragmentManager();
		Fragment fragment = fragmentManager
				.findFragmentById(R.id.fragmentTransactionRecorder);
		if (fragment != null) {
			mTransactionRecorderFragment = (TransactionRecorderFragment) fragment;
			mTransactionRecorderFragment.setOnStuffIdChangedListener(this);
		}
	}

	@Override
	public void onBackPressed() {
		ViewFlipper vf = (ViewFlipper) findViewById(R.id.viewFlipperMain);

		if (vf.getDisplayedChild() == 1) {
			mTransactionRecorderFragment.showStuffPanel();
		} else if (vf.getDisplayedChild() == 2) {
			mTransactionRecorderFragment.showStuffPanel();
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	@Override
	protected void onPause() {
		super.onPause();

	}

	@Override
	protected void onStop() {
		super.onStop();

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.buttonNew:
			newStuff();

			break;
		case R.id.buttonDeleteStuff:
			Utility.showConfirmDialog(
					this,
					getString(R.string.delete_stuff),
					String.format(getString(R.string.confirm_delete_stuff),
							mTransactionRecorderFragment.getCurrentStuffName()),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							DataService.GetInstance(context).deleteStuffById(
									mTransactionRecorderFragment
											.getCurrentStuffId());
							loadStuffsAsync();
							mTransactionRecorderFragment.clear();
						}
					});

			break;
		default:
			break;
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
	}

	private void loadStuffsAsync() {
		mTransactionRecorderFragment.loadStuffsAsync();
	}

	@Override
	public void onStuffIdChanged(int stuffId) {
		mButtonDeleteStuff
				.setVisibility(stuffId > 0 ? View.VISIBLE : View.GONE);
	}

}