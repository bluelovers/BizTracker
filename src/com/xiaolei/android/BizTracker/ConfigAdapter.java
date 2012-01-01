/**
 * 
 */
package com.xiaolei.android.BizTracker;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.xiaolei.android.common.Utility;
import com.xiaolei.android.entity.Parameter;
import com.xiaolei.android.entity.ParameterKeys;
import com.xiaolei.android.service.DataService;

/**
 * @author xiaolei
 * 
 */
public class ConfigAdapter extends BaseAdapter {

	private String[] items;
	private LayoutInflater inflater;
	private Activity context;
	private Parameter paramPassword;

	public ConfigAdapter(Activity context) {
		this.context = context;

		String default_currency = "";
		Parameter param = DataService.GetInstance(context).getParameterByKey(
				ParameterKeys.DefaultCurrencyCode);
		if (param != null) {
			default_currency = param.getValue();
		}
		String currency = context.getString(R.string.default_currency);
		if (!TextUtils.isEmpty(default_currency)) {
			currency = String.format("%s (%s)", currency, default_currency);
		}

		items = new String[] { context.getString(R.string.password), currency,
				context.getString(R.string.config_exchange_rate),
				context.getString(R.string.volumn),
				context.getString(R.string.send_feedback),
				context.getString(R.string.go_to_android_market),
				context.getString(R.string.version) };

		inflater = LayoutInflater.from(context);

		paramPassword = DataService.GetInstance(context).getParameterByKey(
				"password");

	}

	@Override
	public int getCount() {
		return items.length;
	}

	@Override
	public Object getItem(int arg0) {
		return items[arg0];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.config_item_template,
					parent, false);
		}

		String name = items[position];
		TextView tvName = (TextView) convertView
				.findViewById(R.id.textViewConfigItemName);
		TextView tvDescription = (TextView) convertView
				.findViewById(R.id.textViewConfigItemDescription);
		tvDescription.setText("");

		tvName.setText(name);

		switch (position) {
		case 0:
			tvDescription.setText(context
					.getString(R.string.password_protection_description));
			String status = context.getString(R.string.enabled);
			if (paramPassword == null) {
				status = context.getString(R.string.disabled);
			} else {
				String value = paramPassword.getValue();
				if (paramPassword == null || value == null
						|| value.length() == 0) {
					status = context.getString(R.string.disabled);
				}
			}

			tvName.setText(String.format("%s (%s)", name, status));

			break;
		case 1:
			tvDescription.setText(context
					.getString(R.string.default_currency_description));

			break;
		case 2:
			tvDescription.setText(context
					.getString(R.string.config_exchange_rate_description));

			break;
		case 3:
			tvDescription.setText(context
					.getString(R.string.volumn_description));
			break;
		case 4:
			tvDescription.setText(context
					.getString(R.string.send_feedback_desc));

			break;
		case 5:
			tvDescription.setText(context
					.getString(R.string.go_to_android_market_description));

			break;
		case 6:
			String version = Utility.getVersion(context);

			tvDescription = (TextView) convertView
					.findViewById(R.id.textViewConfigItemDescription);
			tvDescription.setText(version);

			break;

		default:
			break;
		}

		return convertView;
	}
}