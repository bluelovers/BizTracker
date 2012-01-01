package com.xiaolei.android.customControl;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import com.xiaolei.android.entity.StuffSchema;
import com.xiaolei.android.service.DataService;

public class StuffNameAutoCompleteArrayAdapter extends ArrayAdapter<String> {
	private Boolean isBusy = false;
	private Context mContext;
	private Filter mFilter;
	protected int MAX_SUGGESTION_ITEM_COUNT = 3;
	private List<String> mData = new ArrayList<String>();

	protected void init() {
		setNotifyOnChange(false);

		mFilter = new Filter() {
			private FilterResults mFilterResults = new FilterResults();

			// This method is called in a worker thread
			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				if (isBusy == true) {
					return mFilterResults;
				}
				isBusy = true;
				mData.clear();

				mFilterResults = new FilterResults();
				if (constraint != null) {
					Cursor cursor = DataService.GetInstance(mContext)
							.searchStuff(constraint.toString(),
									MAX_SUGGESTION_ITEM_COUNT);
					if (cursor != null) {
						try {
							if (cursor.moveToFirst()) {
								do {
									String stuffName = cursor.getString(cursor
											.getColumnIndex(StuffSchema.Name));
									mData.add(stuffName);
								} while (cursor.moveToNext());
							}
						} finally {
							cursor.close();
							cursor = null;
						}
					}

					mFilterResults.values = mData;
					mFilterResults.count = mData.size();
				}
				isBusy = false;
				return mFilterResults;
			}

			@Override
			protected void publishResults(CharSequence contraint,
					FilterResults results) {
				if (results != null && results.count > 0) {
					notifyDataSetChanged();
				} else {
					notifyDataSetInvalidated();
				}
			}
		};
	}

	public StuffNameAutoCompleteArrayAdapter(Context context,
			int textViewResourceId) {
		super(context, textViewResourceId);
		mContext = context;
		init();
	}

	public StuffNameAutoCompleteArrayAdapter(Context context, int resource,
			int textViewResourceId) {
		super(context, resource, textViewResourceId);
		mContext = context;
		init();
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public String getItem(int position) {
		if (position < 0 || position > mData.size()) {
			return "";
		} else {
			return mData.get(position);
		}
	}

	@Override
	public Filter getFilter() {
		return mFilter;
	}
}
