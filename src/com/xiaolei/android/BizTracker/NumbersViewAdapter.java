package com.xiaolei.android.BizTracker;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;

public class NumbersViewAdapter extends BaseAdapter {

	private Context context;
	private String[] data;

	public NumbersViewAdapter(Context context) {
		super();

		this.context = context;
		data = new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "0",
				".", context.getString(R.string.ok) };
	}

	@Override
	public int getCount() {
		return data.length;
	}

	@Override
	public Object getItem(int position) {
		return data[position];
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Button result;
		if (convertView == null) {
			String value = data[position];
			result = new Button(context);
			result.setId(-2);
			result.setOnClickListener((OnClickListener) context);
			result.setTextSize(20);
			result.setText(value);
			result.setTag(value);
			result.setLayoutParams(new GridView.LayoutParams(
					GridView.LayoutParams.FILL_PARENT,
					GridView.LayoutParams.FILL_PARENT));
		} else {
			result = (Button) convertView;
		}

		return result;
	}

}
