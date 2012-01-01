/**
 * 
 */
package com.xiaolei.android.entity;

import android.content.Context;
import android.text.TextUtils;

import com.xiaolei.android.service.DataService;

/**
 * @author xiaolei
 * 
 */
public class ParameterUtils {
	public static int getIntParameterValue(Context context, String key,
			int defaultValue) {
		int result = defaultValue;
		if (!TextUtils.isEmpty(key)) {
			Parameter param = DataService.GetInstance(context)
					.getParameterByKey(key);
			if (param != null && !param.isEmpty()) {
				result = param.getIntegerValue(defaultValue);
			}
		} else {
			return result;
		}

		return result;
	}

	public static double getDoubleParameterValue(Context context, String key,
			double defaultValue) {
		double result = defaultValue;
		if (!TextUtils.isEmpty(key)) {
			Parameter param = DataService.GetInstance(context)
					.getParameterByKey(key);
			if (param != null && !param.isEmpty()) {
				result = param.getDoubleValue(defaultValue);
			}
		} else {
			return result;
		}

		return result;
	}

	public static float getFloatParameterValue(Context context, String key,
			float defaultValue) {
		float result = defaultValue;
		if (!TextUtils.isEmpty(key)) {
			Parameter param = DataService.GetInstance(context)
					.getParameterByKey(key);
			if (param != null && !param.isEmpty()) {
				result = param.getFloatValue(defaultValue);
			}
		} else {
			return result;
		}

		return result;
	}

	public static String getParameterValue(Context context, String key,
			String defaultValue) {
		String result = defaultValue;
		if (!TextUtils.isEmpty(key)) {
			Parameter param = DataService.GetInstance(context)
					.getParameterByKey(key);
			if (param != null && !param.isEmpty()) {
				result = param.getValue();
			}
		} else {
			return result;
		}

		return result;
	}

	public static void saveParameterValue(Context context, String key,
			String value) {
		if (!TextUtils.isEmpty(key)) {
			DataService ds = DataService.GetInstance(context);
			Parameter param = new Parameter();
			param.setKey(key);
			param.setValue(value);

			ds.saveParameter(param);
		}
	}
}
