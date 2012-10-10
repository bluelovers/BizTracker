package com.xiaolei.android.BizTracker;

import java.util.Date;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.format.DateUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.xiaolei.android.listener.OnStuffIdChangedListener;
import com.xiaolei.android.preference.PreferenceHelper;
import com.xiaolei.android.ui.TransactionRecorderFragment;

public class BizTracker extends FragmentActivity implements OnClickListener,
		OnStuffIdChangedListener {
	private BizTracker mContext;
	private Date mTransactionDate;
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

		mContext = this;

		Intent intent = this.getIntent();
		if (intent != null) {
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				if (bundle.containsKey(KEY_UPDATE_DATE)) {
					Object value = bundle.get(KEY_UPDATE_DATE);
					if (value != null) {
						try {
							mTransactionDate = (Date) value;
							TextView tvTopLeft = (TextView) findViewById(R.id.textViewTopLeft);
							if (tvTopLeft != null) {
								tvTopLeft
										.setText(getString(R.string.transaction_date)
												+ ": "
												+ DateUtils.formatDateTime(
														mContext,
														mTransactionDate.getTime(),
														DateUtils.FORMAT_SHOW_DATE));
							}
						} catch (Exception ex) {
							ex.printStackTrace();
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
			mTransactionRecorderFragment.setTransactionDate(mTransactionDate);
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
			mTransactionRecorderFragment.newStuff();

			break;
		case R.id.buttonDeleteStuff:
			mTransactionRecorderFragment.removeCurrentStuff();

			break;
		default:
			break;
		}
	}

	@Override
	public void onStuffIdChanged(int stuffId) {
		mButtonDeleteStuff
				.setVisibility(stuffId > 0 ? View.VISIBLE : View.GONE);
	}

}