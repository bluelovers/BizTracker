/**
 * 
 */
package com.xiaolei.android.customControl;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

/**
 * @author xiaolei
 * 
 */
public class AutoCompleteStuffNameTextView extends AutoCompleteTextView {
	private Context mContext;
	protected int MAX_SUGGESTION_ITEM_COUNT = 3;
	protected StuffNameAutoCompleteArrayAdapter suggestionList;

	protected void init() {
		suggestionList = new StuffNameAutoCompleteArrayAdapter(mContext,
				android.R.layout.simple_dropdown_item_1line);
		setAdapter(suggestionList);
	}

	public AutoCompleteStuffNameTextView(Context context) {
		super(context);
		mContext = context;
		init();
	}

	public AutoCompleteStuffNameTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}

	public AutoCompleteStuffNameTextView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		init();
		
	}
}
