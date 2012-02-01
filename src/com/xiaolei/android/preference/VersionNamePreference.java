/**
 * 
 */
package com.xiaolei.android.preference;

import com.xiaolei.android.common.Utility;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;

/**
 * @author xiaolei
 * 
 */
public class VersionNamePreference extends Preference {

	public VersionNamePreference(Context context) {
		super(context);

	}

	public VersionNamePreference(Context context, AttributeSet attrs) {
		super(context, attrs);

	}

	public VersionNamePreference(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);

	}

	@Override
	public CharSequence getSummary() {
		return Utility.getVersion(getContext());
	}
}
