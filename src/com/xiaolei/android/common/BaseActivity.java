/**
 * 
 */
package com.xiaolei.android.common;

import com.xiaolei.android.BizTracker.R;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.view.Window;

/**
 * @author x
 * 
 */
public class BaseActivity extends Activity {

	protected Context mContext;
	public static int RequestCode = -1;

	public BaseActivity() {

		RequestCode = this.getClass().toString().hashCode();
	}

	protected void initialize(int contentLayoutResourceId) {
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		this.setContentView(contentLayoutResourceId);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.title_bar);
	}

	public void loadAsync() {
		AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(Void... params) {
				load();

				return true;
			}

			protected void onPostExecute(Boolean result) {
				if (result == true) {
					onLoadCompleted(result);
				}
			}

		};
		task.execute();
	}

	protected void load() {

	}

	protected void onLoadCompleted(Boolean success) {

	}
}
