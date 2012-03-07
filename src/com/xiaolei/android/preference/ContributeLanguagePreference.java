package com.xiaolei.android.preference;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.Preference;
import android.util.AttributeSet;

public class ContributeLanguagePreference extends Preference {
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
		String url = "https://skydrive.live.com/redir.aspx?cid=9107ba7a12d48ece&resid=9107BA7A12D48ECE!107&parid=9107BA7A12D48ECE!105&authkey=!AIin8YCUV5ov3Q0";
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(url));
		getContext().startActivity(intent);
	}
}
