package com.xiaolei.android.BizTracker;

import java.util.Date;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
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
		OnStuffIdChangedListener, OnPageChangeListener {
	private BizTracker mContext;
	private Date mTransactionDate;
	private ViewHolder mViewHolder = new ViewHolder();
	private boolean mSaveAndClose = false;

	public static final String KEY_UPDATE_DATE = "UpdateDate";
	public static final String SAVE_AND_CLOSE = "save_and_close";
	public static final int REQUEST_CODE = 1;
	public static final String APPLICATION_FOLDER = "BizTracker";
	public static final String PHOTO_PATH = "BizTracker/photo";
	private final int MAIN_ITEM_INDEX = 0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.main);

		init();
	}

	private void init() {
		mContext = this;

		Intent intent = this.getIntent();
		if (intent != null) {
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				mSaveAndClose = bundle.getBoolean(SAVE_AND_CLOSE, false);
				if (bundle.containsKey(KEY_UPDATE_DATE)) {
					Object value = bundle.get(KEY_UPDATE_DATE);
					if (value != null) {
						try {
							mTransactionDate = (Date) value;
							showTransactionDateTime(mTransactionDate);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}
			}
		}

		PreferenceHelper.createActiveUserRelatedPreferencesIfNeeds(this);
		PreferenceHelper.migrateOldPreferencesIfNeed(this);

		mViewHolder.ButtonNewStuff = (Button) findViewById(R.id.buttonNew);
		if (mViewHolder.ButtonNewStuff != null) {
			mViewHolder.ButtonNewStuff.setOnClickListener(this);
		}

		mViewHolder.ButtonDeleteStuff = (Button) findViewById(R.id.buttonDeleteStuff);
		if (mViewHolder.ButtonDeleteStuff != null) {
			mViewHolder.ButtonDeleteStuff.setOnClickListener(this);
		}

		mViewHolder.ViewPagerMain = (ViewPager) findViewById(R.id.viewPaperMain);
		if (mViewHolder.ViewPagerMain != null) {
			MainFragmentPagerAdapter adpt = new MainFragmentPagerAdapter(
					this.getSupportFragmentManager());
			mViewHolder.ViewPagerMain.setOnPageChangeListener(this);
			mViewHolder.ViewPagerMain.setAdapter(adpt);
			mViewHolder.ViewPagerMain.setCurrentItem(MAIN_ITEM_INDEX);
		}
	}

	private void showTransactionDateTime(Date date) {
		TextView tvTopLeft = (TextView) findViewById(R.id.textViewTopLeft);
		if (tvTopLeft != null) {
			if (date != null) {
				tvTopLeft.setText(getString(R.string.transaction_date)
						+ ": "
						+ DateUtils.formatDateTime(mContext, date.getTime(),
								DateUtils.FORMAT_SHOW_DATE));
			} else {
				tvTopLeft.setText("");
			}
		}
	}

	@Override
	public void onBackPressed() {
		try {
			getRecorderFragment();
			if (mViewHolder.RecorderFragment != null) {
				ViewFlipper vf = (ViewFlipper) findViewById(R.id.viewFlipperMain);

				if (vf.getDisplayedChild() == 1) {
					mViewHolder.RecorderFragment.showStuffPanel();
				} else if (vf.getDisplayedChild() == 2) {
					mViewHolder.RecorderFragment.showStuffPanel();
				} else {
					super.onBackPressed();
				}
			} else {
				super.onBackPressed();
			}
		} catch (Exception e) {
			e.printStackTrace();
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
			getRecorderFragment();
			if (mViewHolder.RecorderFragment != null
					&& mViewHolder.RecorderFragment.isAdded()) {
				mViewHolder.RecorderFragment.newStuff();
			}

			break;
		case R.id.buttonDeleteStuff:
			getRecorderFragment();
			if (mViewHolder.RecorderFragment != null
					&& mViewHolder.RecorderFragment.isAdded()) {
				mViewHolder.RecorderFragment.removeCurrentStuff();
			}

			break;
		default:
			break;
		}
	}

	private void getRecorderFragment() {
		if (mViewHolder.RecorderFragment == null) {
			if (mViewHolder.ViewPagerMain != null) {
				MainFragmentPagerAdapter adpt = (MainFragmentPagerAdapter) mViewHolder.ViewPagerMain
						.getAdapter();
				if (adpt != null) {
					mViewHolder.RecorderFragment = (TransactionRecorderFragment) adpt
							.getFragmentAtPosition(MAIN_ITEM_INDEX);

					if (mViewHolder.RecorderFragment != null) {
						mViewHolder.RecorderFragment
								.setOnStuffIdChangedListener(this);
						mViewHolder.RecorderFragment
								.setTransactionDate(mTransactionDate);
						mViewHolder.RecorderFragment
								.setSaveAndClose(mSaveAndClose);
						mSaveAndClose = false;
					}
				}
			}
		}
		
	}

	@Override
	public void onStuffIdChanged(int stuffId) {
		mViewHolder.ButtonDeleteStuff.setVisibility(stuffId > 0 ? View.VISIBLE
				: View.GONE);
	}

	private class ViewHolder {
		public ViewPager ViewPagerMain;
		public Button ButtonDeleteStuff;
		public Button ButtonNewStuff;
		public TransactionRecorderFragment RecorderFragment;
	}

	@Override
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageScrollStateChanged(int state) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageSelected(int position) {

	}

}