package com.xiaolei.android.preference;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.Preference;
import android.util.AttributeSet;

public class ContributeLanguagePreference extends Preference {
	private final String mContributeLanaguageFileUrl = "https://docs.google.com/document/d/1YtfQYtIurg-xwN8G5HOUZLXxPLjZDdhvAgo-z7il4aU/edit";
	
	public ContributeLanguagePreference(Context context) {
		super(context);

	}

	public ContributeLanguagePreference(Context context, AttributeSet attrs) {
		super(context, attrs);

	}

	public ContributeLanguagePreference(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);

	}

	@Override
	protected void onClick() {
		openContributeLanguageWebPage();
	}

	private void openContributeLanguageWebPage() {
		String url = mContributeLanaguageFileUrl;
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(url));
		getContext().startActivity(intent);
	}
}
