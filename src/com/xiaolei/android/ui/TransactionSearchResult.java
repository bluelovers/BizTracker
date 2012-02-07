/**
 * 
 */
package com.xiaolei.android.ui;

import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.xiaolei.android.BizTracker.DailyTransactionListCursorAdapter;
import com.xiaolei.android.BizTracker.Helper;
import com.xiaolei.android.BizTracker.R;
import com.xiaolei.android.entity.BizLog;
import com.xiaolei.android.event.OnGotResultListener;
import com.xiaolei.android.event.OnLoadCursorCompletedListener;
import com.xiaolei.android.listener.OnCostValueChangedListener;
import com.xiaolei.android.listener.OnStarImageViewClickListener;
import com.xiaolei.android.service.DataService;

/**
 * @author xiaolei
 * 
 */
public class TransactionSearchResult extends Activity implements
		OnCostValueChangedListener {

	private Activity context;
	private String searchKeyword = "";
	public static final int REQUEST_CODE = 10;
	public static final String KEY_SEARCH_KEYWORD = "searchKeyword";
	private OnCostValueChangedListener onCostValueChangedListener;
	private DailyTransactionListCursorAdapter adpt;
	private TextView tvHeader;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		context = this;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.transaction_search_result);

		onCostValueChangedListener = this;

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
						if (!TextUtils.isEmpty(searchKeyword) && adpt != null) {
							adpt.setSearchKeyword(searchKeyword);
							adpt.loadDataAsync();
						} else {
							showSearchHelpView();
						}
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

		showSearchHelpView();

		loadData();
	}

	private void showSearchHelpView() {
		ViewSwitcher viewSwitcher = (ViewSwitcher) context
				.findViewById(R.id.viewSwitcherDayBizLog);
		if (viewSwitcher != null) {
			// If no result, display the empty text.
			TextView tvEmpty = (TextView) context
					.findViewById(R.id.textViewEmpty);
			if (tvEmpty != null) {
				tvEmpty.setText(getString(R.string.search_help));
			}

			viewSwitcher.setDisplayedChild(1);
		}
	}

	private void loadData() {
		ListView lv = (ListView) findViewById(R.id.listViewBizLogByDay);
		View header = this.getLayoutInflater().inflate(
				R.layout.list_view_header, null);
		tvHeader = (TextView) header.findViewById(R.id.textViewListViewHeader);
		if (tvHeader != null) {
			tvHeader.setText(getString(R.string.search_result));
			lv.addHeaderView(header);
		}

		adpt = new DailyTransactionListCursorAdapter(context, lv, new Date(),
				new OnLoadCursorCompletedListener() {

					@Override
					public void onLoadCursorCompleted(View sender, Cursor result) {
						if (result != null) {
							int count = result.getCount();
							if (count > 0) {
								if (tvHeader != null) {
									tvHeader.setText(String
											.format(getString(R.string.search_result_count),
													count));
								}

								ViewSwitcher viewSwitcher = (ViewSwitcher) context
										.findViewById(R.id.viewSwitcherDayBizLog);
								if (viewSwitcher.getDisplayedChild() != 0) {
									viewSwitcher.showNext();
								}

								ViewSwitcher viewSwitcherContent = (ViewSwitcher) context
										.findViewById(R.id.viewSwitcherContent);
								viewSwitcherContent.setDisplayedChild(1);
							} else {
								if (tvHeader != null) {
									tvHeader.setText(getString(R.string.search_result));
								}

								ViewSwitcher viewSwitcher = (ViewSwitcher) context
										.findViewById(R.id.viewSwitcherDayBizLog);

								// If no result, display the empty text.
								TextView tvEmpty = (TextView) context
										.findViewById(R.id.textViewEmpty);
								if (tvEmpty != null) {
									tvEmpty.setText(getString(R.string.no_search_result));
								}

								viewSwitcher.setDisplayedChild(1);
							}
						} else {
							showSearchHelpView();
						}
					}

				}, new OnStarImageViewClickListener() {

					@Override
					public void onStarImageViewClick(ImageView imageView,
							BizLog bizLog, DailyTransactionListCursorAdapter listViewAdapter) {
						if (bizLog != null && listViewAdapter != null) {
							if (bizLog.getStar() == true) {
								DataService.GetInstance(context).removeStar(
										bizLog.getId());
							} else {
								DataService.GetInstance(context).addStar(
										bizLog.getId());
							}
							listViewAdapter.loadDataAsync();
						}
					}

				});

		lv.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View item,
					int position, long _id) {
				BizLog instance = (BizLog) item.getTag();

				if (instance != null) {
					Helper.showBizLogItemOptionsMenu(context, instance,
							new OnGotResultListener() {

								@Override
								public void onGotResult(Boolean result,
										Boolean effectCostValue) {
									if (result == true) {
										// Reload ListView
										if (adpt != null) {
											adpt.loadDataAsync();
										}

										// If operations effect the cost value,
										// notify another views to reload
										// themselves.
										if (effectCostValue == true
												&& onCostValueChangedListener != null) {
											onCostValueChangedListener
													.OnCostValueChanged();
										}
									}
								}

							});
				}

				return true;
			}

		});

		adpt.setShowFullDateTime(true);
		// adpt.loadDataAsync();
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
