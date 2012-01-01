/**
 * 
 */
package com.xiaolei.android.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ViewSwitcher;

import com.xiaolei.android.BizTracker.ProjectListAdapter;
import com.xiaolei.android.BizTracker.R;
import com.xiaolei.android.common.Utility;
import com.xiaolei.android.entity.Project;
import com.xiaolei.android.service.DataService;

/**
 * @author xiaolei
 * 
 */
public class ProjectManager extends Activity implements OnClickListener {

	private ProjectManager context = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.project);

		context = this;

		Button btnCreateProject = (Button) findViewById(R.id.buttonCreateProject);
		btnCreateProject.setOnClickListener(this);

		fillDataAsync();
	}

	private void fillDataAsync() {
		AsyncTask<Boolean, Void, Cursor> task = new AsyncTask<Boolean, Void, Cursor>() {

			@Override
			protected Cursor doInBackground(Boolean... params) {
				Cursor cursor = DataService.GetInstance(context)
						.getAllProjects();
				return cursor;
			}

			@Override
			protected void onPostExecute(Cursor result) {
				fillData(result);
			}
		};

		task.execute();
	}

	private void fillData(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		ViewSwitcher viewSwitcher = (ViewSwitcher) this
				.findViewById(R.id.viewSwitcherProject);

		if (cursor.getCount() == 0) {
			viewSwitcher.setDisplayedChild(0);
		} else {
			viewSwitcher.setDisplayedChild(1);
			ListView lv = (ListView) findViewById(R.id.listViewProjectList);
			ProjectListAdapter listAdapter = (ProjectListAdapter) lv
					.getAdapter();
			if (listAdapter == null) {
				lv.setAdapter(new ProjectListAdapter(this, cursor));
			} else {
				listAdapter.changeCursor(cursor);
			}
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.buttonCreateProject:
			newProject();

			break;
		default:
			break;
		}
	}

	private void newProject() {
		Utility.showDialog(this, R.layout.project_editor,
				getString(R.string.project),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						AlertDialog dlg = (AlertDialog) dialog;
						EditText txtName = (EditText) dlg
								.findViewById(R.id.editTextProjectName);
						String name = txtName.getText().toString();

						Project proj = new Project();
						proj.setName(name);

						long result = DataService.GetInstance(context)
								.addProject(proj);
						if (result > 0) {
							fillDataAsync();
						}
					}
				}, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.project_list_menu, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.itemNewProject:
			newProject();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
