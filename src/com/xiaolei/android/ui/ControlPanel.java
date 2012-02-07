/**
 * 
 */
package com.xiaolei.android.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.xiaolei.android.BizTracker.ControlPanelAdapter;
import com.xiaolei.android.BizTracker.R;
import com.xiaolei.android.entity.ControlPanelItem;

/**
 * @author xiaolei
 * 
 */
public class ControlPanel extends Activity implements OnItemClickListener {

	public static final int REQUEST_CODE = 11;
	private ControlPanelAdapter adapter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.control_panel);

		GridView gv = (GridView) findViewById(R.id.gridViewControlPanel);
		if (gv != null) {
			gv.setOnItemClickListener(this);
		}

		load();
	}

	private void load() {
		GridView gv = (GridView) findViewById(R.id.gridViewControlPanel);
		if (gv != null) {
			adapter = new ControlPanelAdapter(this);
			gv.setAdapter(adapter);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position,
			long id) {
		if (adapter == null) {
			return;
		}
		ControlPanelItem item = (ControlPanelItem) adapter.getItem(position);
		if (item != null) {
			Intent intent = item.getIntent();
			if (intent != null) {
				this.startActivity(intent);
			}
		}
	}
}
