package com.xiaolei.android.ui;

import com.xiaolei.android.BizTracker.R;
import com.xiaolei.android.common.GroupType;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class GroupTransactionListFragment extends Fragment {
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (savedInstanceState != null) {
			// Serializable dataset = savedInstanceState
			// .getSerializable("dataset");

		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View result = inflater.inflate(
				R.layout.group_transaction_list_fragment, container, false);
		if (result != null) {

		}

		return result;
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// outState.putSerializable("dataset", mDataset);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

	}

	@Override
	public void onDestroyView() {

		super.onDestroyView();
	}

	public void groupTransactionList(GroupType groupType) {
		if (getView() != null) {
			switch (groupType) {
			case ByDay:
				groupByDayAsync();
				break;
			case ByMonth:
				groupByMonthAsync();
				break;
			case ByYear:
				groupByYearAsync();
				break;
			default:
				break;
			}
		}
	}

	public void groupByDayAsync() {
		ListView lv = (ListView) getView().findViewById(
				R.id.listViewGroupTransactionList);
		if (lv != null) {

		}
	}

	public void groupByMonthAsync() {

	}

	public void groupByYearAsync() {

	}
}
