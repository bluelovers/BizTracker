package com.xiaolei.android.BizTracker;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.xiaolei.android.ui.TransactionDetailsFragment;
import com.xiaolei.android.ui.VoiceNotesFragment;

public class TransactionDetailPagerAdapter extends FragmentPagerAdapter {

	private final int mItemCount = 2;
	private long mTransactionId = 0;

	public TransactionDetailPagerAdapter(FragmentManager fm, long transactionId) {
		super(fm);
		mTransactionId = transactionId;
	}

	@Override
	public Fragment getItem(int position) {
		Fragment result = null;
		switch (position) {
		case 0:
			TransactionDetailsFragment detailsFragment = new TransactionDetailsFragment();
			result = detailsFragment;
			detailsFragment.showTransactionDetails(mTransactionId);

			break;
		case 1:
			VoiceNotesFragment voiceNotesFragment = new VoiceNotesFragment();
			result = voiceNotesFragment;
			voiceNotesFragment.showVoiceNotes(mTransactionId);

			break;
		default:
			break;
		}

		return result;
	}

	@Override
	public int getCount() {
		return mItemCount;
	}

}
