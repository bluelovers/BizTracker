package com.xiaolei.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.ViewSwitcher;

import com.xiaolei.android.BizTracker.R;

public class FunctionTypes extends FragmentActivity implements
		OnEditorActionListener {

	protected static final int REQUEST_CODE = 1224;
	private TransactionHistoryFragment mTransactionHistoryFragment;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.function_types);

		EditText txtKeyword = (EditText) findViewById(R.id.editTextKeyword);
		txtKeyword.setOnEditorActionListener(this);

		FragmentManager fragmentManager = this.getSupportFragmentManager();
		if (fragmentManager != null) {
			mTransactionHistoryFragment = (TransactionHistoryFragment) fragmentManager
					.findFragmentById(R.id.fragmentTransactionHistory);
		}
	}

	private void showSeachView() {
		Intent intent = new Intent(this, TransactionSearchResult.class);
		this.startActivityForResult(intent,
				TransactionSearchResult.REQUEST_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			mTransactionHistoryFragment.loadDataAsync();
		}
	}

	@Override
	public boolean onSearchRequested() {
		this.showSeachView();

		return true;
	}

	@Override
	public boolean onEditorAction(TextView textView, int arg1, KeyEvent evt) {
		if (evt.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
			ViewSwitcher v = (ViewSwitcher) findViewById(R.id.viewSwitcherTitleBar);
			v.setDisplayedChild(0);

			String searchKeyword = textView.getText().toString();

			Intent intent = new Intent(this, TransactionList.class);
			intent.putExtra("title", searchKeyword);
			intent.putExtra("searchKeyword", searchKeyword);
			this.startActivityForResult(intent, 0);

			EditText etKeyword = (EditText) findViewById(R.id.editTextKeyword);
			etKeyword.setText("");

			return true;
		}
		return false;
	}
}
