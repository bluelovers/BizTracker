package com.xiaolei.android.ui;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.xiaolei.android.BizTracker.R;
import com.xiaolei.android.listener.OnTransactionDateTimeChangedListener;

public class DateTimePickerFragment extends DialogFragment {

	private ViewHolder mViewHolder = new ViewHolder();
	private OnTransactionDateTimeChangedListener mTransactionDateTimeChangedListener;

	public static DateTimePickerFragment newInstance() {
		DateTimePickerFragment result = new DateTimePickerFragment();
		// Bundle args = new Bundle();
		// result.setArguments(args);

		return result;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.choose_transaction_date);
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setView(getContentView());
		builder.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						GregorianCalendar cal = new GregorianCalendar();

						if (mTransactionDateTimeChangedListener != null) {
							mViewHolder.DatePickerTransactionDate.clearFocus();
							cal = new GregorianCalendar(
									mViewHolder.DatePickerTransactionDate
											.getYear(),
									mViewHolder.DatePickerTransactionDate
											.getMonth(),
									mViewHolder.DatePickerTransactionDate
											.getDayOfMonth());

						}
						if (mViewHolder.DatePickerTransactionDate != null) {
							mViewHolder.TimePickerTransactionTime.clearFocus();
							cal.set(Calendar.HOUR_OF_DAY,
									mViewHolder.TimePickerTransactionTime
											.getCurrentHour());
							cal.set(Calendar.MINUTE,
									mViewHolder.TimePickerTransactionTime
											.getCurrentMinute());
						}
						onOnTransactionDateTimeChanged(cal.getTime());
					}
				});
		builder.setNegativeButton(android.R.string.cancel,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				});
		Dialog dialog = builder.create();
		return dialog;

	}

	public void setTransactionDateTimeChangedListener(
			OnTransactionDateTimeChangedListener listener) {
		mTransactionDateTimeChangedListener = listener;
	}

	private void onOnTransactionDateTimeChanged(Date date) {
		if (mTransactionDateTimeChangedListener != null) {
			mTransactionDateTimeChangedListener
					.onTransactionDateTimeChanged(date);
		}
	}

	private View getContentView() {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View result = inflater.inflate(R.layout.datetime_picker_fragment, null);
		if (result != null) {
			mViewHolder.DatePickerTransactionDate = (DatePicker) result
					.findViewById(R.id.datePickerTransactionDate);
			mViewHolder.TimePickerTransactionTime = (TimePicker) result
					.findViewById(R.id.timePickerTransactionTime);
		}

		return result;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	}

	@Override
	public void onDestroy() {
		super.onDestroy();

	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

	}

	private class ViewHolder {

		public DatePicker DatePickerTransactionDate;
		public TimePicker TimePickerTransactionTime;
	}
}
