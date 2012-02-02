/**
 * 
 */
package com.xiaolei.android.BizTracker;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiaolei.android.activity.FunctionTypes;
import com.xiaolei.android.activity.ProjectManager;
import com.xiaolei.android.activity.TransactionSearchResult;
import com.xiaolei.android.entity.ControlPanelItem;

/**
 * @author xiaolei
 * 
 */
public class ControlPanelAdapter extends BaseAdapter {

	@SuppressWarnings("unused")
	private Context context;
	private ArrayList<ControlPanelItem> itemsSource;
	private LayoutInflater inflater;

	public ControlPanelAdapter(Context context) {
		this.context = context;
		inflater = LayoutInflater.from(context);

		itemsSource = new ArrayList<ControlPanelItem>();

		ControlPanelItem itemViewHistory = new ControlPanelItem();
		itemViewHistory.setName(context.getString(R.string.view_cost_history));
		itemViewHistory.setIconResourceId(R.drawable.eye);
		itemViewHistory.setIntent(new Intent(context, FunctionTypes.class));

		ControlPanelItem itemProject = new ControlPanelItem();
		itemProject.setName(context.getString(R.string.project));
		itemProject.setIconResourceId(R.drawable.ic_project);
		itemProject.setIntent(new Intent(context, ProjectManager.class));

		ControlPanelItem itemSearch = new ControlPanelItem();
		itemSearch.setName(context.getString(R.string.search));
		itemSearch.setIconResourceId(R.drawable.ic_search);
		itemSearch
				.setIntent(new Intent(context, TransactionSearchResult.class));

		ControlPanelItem itemConfig = new ControlPanelItem();
		itemConfig.setName(context.getString(R.string.config));
		itemConfig.setIconResourceId(R.drawable.config);
		itemConfig.setIntent(new Intent(context,
				com.xiaolei.android.activity.Settings.class));

		itemsSource.add(itemViewHistory);
		itemsSource.add(itemProject);
		itemsSource.add(itemSearch);
		itemsSource.add(itemConfig);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		return itemsSource.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public Object getItem(int index) {
		Object result = null;
		int count = itemsSource.size();
		if (index >= 0 && index < count) {
			result = itemsSource.get(index);
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId(int position) {
		return position;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getView(int, android.view.View,
	 * android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(
					R.layout.control_panel_item_template, parent, false);
		}

		ImageView ivIcon = (ImageView) convertView
				.findViewById(R.id.imageViewControlPanelItem);
		TextView tvName = (TextView) convertView
				.findViewById(R.id.textViewControlPanelItemName);
		ControlPanelItem item = itemsSource.get(position);

		if (ivIcon != null) {
			ivIcon.setImageResource(item.getIconResourceId());
		}
		if (tvName != null) {
			tvName.setText(item.getName());
		}

		return convertView;
	}
}
