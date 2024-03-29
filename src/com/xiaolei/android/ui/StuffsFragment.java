package com.xiaolei.android.ui;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ViewFlipper;

import com.xiaolei.android.BizTracker.R;
import com.xiaolei.android.entity.StuffSchema;
import com.xiaolei.android.service.DataService;

public class StuffsFragment extends Fragment {
	private int mLimit = 0;
	private int mOffset = 0;
	private OnClickListener mOnButtonClickListener;
	private OnLongClickListener mOnStuffLongClickListener;
	private ViewHolder mViewHolder = new ViewHolder();

	private static final String LIMIT = "limit";
	private static final String OFFSET = "offset";
	private static final String TAG_PREFIX = "buttonStuff";

	public static StuffsFragment newInstance(int limit, int offset) {
		StuffsFragment result = new StuffsFragment();
		Bundle args = new Bundle();
		args.putInt(LIMIT, limit);
		args.putInt(OFFSET, offset);
		result.setArguments(args);

		return result;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (getArguments() != null) {
			mLimit = getArguments().getInt(LIMIT);
			mOffset = getArguments().getInt(OFFSET);
		}

		View result = inflater.inflate(R.layout.stuffs_fragment, container,
				false);
		if (result != null) {
			ViewFlipper flipper = (ViewFlipper) result
					.findViewById(R.id.viewFlipperStuffsFragment);
			View linearLayoutStuffsButtons = result
					.findViewById(R.id.linearLayoutStuffsButtons);

			mViewHolder.ViewFlipperStuffsFlagment = flipper;
			mViewHolder.Container = linearLayoutStuffsButtons;
		}

		return result;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		loadDataAsync();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	/**
	 * Set stuff button click listener.
	 * 
	 * @param onStuffClickListener
	 */
	public void setOnStuffClickListener(OnClickListener onStuffClickListener) {
		mOnButtonClickListener = onStuffClickListener;
	}

	/**
	 * Set stuff button long click listener.
	 * 
	 * @param onStuffLongClickListener
	 */
	public void setOnStuffLongClickListener(
			OnLongClickListener onStuffLongClickListener) {
		mOnStuffLongClickListener = onStuffLongClickListener;
	}

	private void showData(Cursor cursor) {
		if (getView() == null) {
			if (cursor != null) {
				cursor.close();
			}
			return;
		}

		if (cursor != null) {
			try {
				if (mViewHolder.Container != null) {
					if (cursor.moveToFirst()) {
						int index = 0;
						do {
							Button btnStuff = (Button) mViewHolder.Container
									.findViewWithTag(TAG_PREFIX + (index + 1));

							if (btnStuff != null) {
								String stuffName = cursor.getString(cursor
										.getColumnIndex(StuffSchema.Name));
								int stuffId = cursor.getInt(cursor
										.getColumnIndex(StuffSchema.Id));

								btnStuff.setBackgroundResource(R.drawable.button);
								btnStuff.setText(stuffName);
								btnStuff.setTag(stuffId);
								btnStuff.setLongClickable(true);

								if (mOnButtonClickListener != null) {
									btnStuff.setOnClickListener(mOnButtonClickListener);
								}

								if (mOnStuffLongClickListener != null) {
									btnStuff.setOnLongClickListener(mOnStuffLongClickListener);
								}
							}
							index++;
						} while (cursor.moveToNext());
					}
				}
			} finally {
				cursor.close();
			}
		}

		stopWaiting();
	}

	private void stopWaiting() {
		ViewFlipper flipper = mViewHolder.ViewFlipperStuffsFlagment;
		if (flipper != null) {
			flipper.setDisplayedChild(1);
		}
	}

	private void waiting() {
		ViewFlipper flipper = mViewHolder.ViewFlipperStuffsFlagment;
		if (flipper != null) {
			flipper.setDisplayedChild(0);
		}
	}

	private void loadDataAsync() {
		waiting();
		AsyncTask<Void, Void, Cursor> task = new AsyncTask<Void, Void, Cursor>() {

			@Override
			protected Cursor doInBackground(Void... params) {
				Cursor result = DataService.GetInstance(getActivity())
						.getAllStuffs(mLimit, mOffset);
				return result;
			}

			@Override
			protected void onPostExecute(Cursor result) {
				showData(result);
			}
		};
		task.execute();
	}

	private final static class ViewHolder {
		private ViewHolder() {
		}

		public View Container;
		public ViewFlipper ViewFlipperStuffsFlagment;
	}
}
