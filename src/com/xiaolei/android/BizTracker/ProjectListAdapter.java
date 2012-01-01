/**
 * 
 */
package com.xiaolei.android.BizTracker;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.xiaolei.android.common.Utility;
import com.xiaolei.android.entity.ProjectSchema;
import com.xiaolei.android.service.DataService;

/**
 * @author xiaolei
 * 
 */
public class ProjectListAdapter extends CursorAdapter {
	private LayoutInflater inflater = null;
	private String defaultCurrencyCode = "USD";
	private double defaultCurrencyUSDExchangeRate = 0;

	public ProjectListAdapter(Context context, Cursor cursor) {
		super(context, cursor);
		inflater = LayoutInflater.from(context);
		defaultCurrencyCode = DataService.GetInstance(context)
				.getDefaultCurrencyCode();
		defaultCurrencyUSDExchangeRate = DataService.GetInstance(context)
				.getUSDExchangeRate(defaultCurrencyCode);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView txtName = (TextView) view
				.findViewById(R.id.textViewCurrencyName);
		TextView txtDescription = (TextView) view
				.findViewById(R.id.textViewLastUpdateTime);
		TextView txtCost = (TextView) view
				.findViewById(R.id.textViewProjectTotalCostMoney);
		TextView txtIncome = (TextView) view
				.findViewById(R.id.textViewProjectTotalIncomeMoney);

		long projectId = cursor
				.getLong(cursor.getColumnIndex(ProjectSchema.Id));
		String name = cursor.getString(cursor
				.getColumnIndex(ProjectSchema.Name));
		String createTimeValue = cursor.getString(cursor
				.getColumnIndex(ProjectSchema.CreatedTime));
		String createdTime = Utility.toLocalDateString(createTimeValue);
		double cost = DataService.GetInstance(context)
				.getTotalCostMoneyByProjectId(projectId, defaultCurrencyCode,
						defaultCurrencyUSDExchangeRate);
		double income = DataService.GetInstance(context)
				.getTotalIncomeMoneyByProjectId(projectId, defaultCurrencyCode,
						defaultCurrencyUSDExchangeRate);

		if (txtName != null) {
			txtName.setText(name);
		}
		if (txtDescription != null) {
			txtDescription.setText(createdTime);
		}
		if (txtCost != null) {
			txtCost.setText(Utility.formatCurrency(cost, defaultCurrencyCode));
		}
		if (txtIncome != null) {
			txtIncome.setText(Utility.formatCurrency(income,
					defaultCurrencyCode));
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return inflater.inflate(R.layout.project_item_template, parent, false);
	}

}
