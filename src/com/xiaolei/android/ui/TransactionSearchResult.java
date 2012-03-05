/**
 * 
 */
package com.xiaolei.android.ui;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ViewFlipper;
import android.widget.ViewSwitcher;

import com.xiaolei.android.BizTracker.DailyTransactionListCursorAdapter;
import com.xiaolei.android.BizTracker.R;
import com.xiaolei.android.listener.OnCostValueChangedListener;

/**
 * @author xiaolei
 * 
 */
public class TransactionSearchResult extends FragmentActivity implements
		OnCostValueChangedListener {

	private Activity context;
	private String searchKeyword = "";
	public static final int REQUEST_CODE = 10;
	public static final String KEY_SEARCH_KEYWORD = "searchKeyword";
	private DailyTransactionListCursorAdapter adpt;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		context = this;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.transaction_search_result);

		try {
			Intent intent = this.getIntent();
			if (intent != null) {
				Bundle extras = intent.getExtras();
				if (extras != null) {
					if (extras.containsKey(KEY_SEARCH_KEYWORD)) {
						searchKeyword = extras.getString(KEY_SEARCH_KEYWORD);
					}
				}
			}
		} catch (Exception ex) {
		}

		ViewSwitcher viewSwitcherTitleBar = (ViewSwitcher) findViewById(R.id.viewSwitcherTitleBar);
		if (viewSwitcherTitleBar != null) {
			viewSwitcherTitleBar.setDisplayedChild(1);
			EditText txtKeyword = (EditText) findViewById(R.id.editTextKeyword);
			if (txtKeyword != null) {
				txtKeyword.addTextChangedListener(new TextWatcher() {

					@Override
					public void afterTextChanged(Editable s) {

					}

					@Override
					public void beforeTextChanged(CharSequence s, int start,
							int count, int after) {

					}

					@Override
					public void onTextChanged(CharSequence s, int start,
							int before, int count) {
						searchKeyword = s.toString();
						showSearchResult();
					}
				});
			}

			ImageButton btnClear = (ImageButton) findViewById(R.id.imageButtonClear);
			if (btnClear != null) {
				btnClear.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						EditText txtKeyword = (EditText) findViewById(R.id.editTextKeyword);
						if (txtKeyword != null) {
							txtKeyword.setText("");
						}
					}
				});
			}
		}

		showSearchResult();
	}

	private void showSearchResult() {
		ViewFlipper viewFlipper = (ViewFlipper) context
				.findViewById(R.id.viewFlipperSearchResult);
		if (viewFlipper != null) {
			// If no result, display the empty text.
			TextView tvEmpty = (TextView) context
					.findViewById(R.id.textViewEmpty);
			if (tvEmpty != null) {
				tvEmpty.setText(getString(R.string.search_help));
			}

			if (!TextUtils.isEmpty(searchKeyword)) {
				viewFlipper.setDisplayedChild(1);
				FragmentManager fragMan = this.getSupportFragmentManager();
				if (fragMan != null) {
					TransactionListFragment fragment = (TransactionListFragment) fragMan
							.findFragmentById(R.id.fragmentSearchTransactionList);
					if (fragment != null) {
						fragment.search(searchKeyword);
					}
				}
			} else {
				FragmentManager fragMan = this.getSupportFragmentManager();
				if (fragMan != null) {
					TransactionListFragment fragment = (TransactionListFragment) fragMan
							.findFragmentById(R.id.fragmentSearchTransactionList);
					if (fragment != null) {
						fragment.clearSearchKeyword();
					}
				}
				viewFlipper.setDisplayedChild(0);
			}
		}
	}

	@Override
	public void OnCostValueChanged() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStop() {
		super.onStop();

		if (adpt != null) {
			Cursor cursor = adpt.getCursor();
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
	}
}
