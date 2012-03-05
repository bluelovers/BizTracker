/**
 * 
 */
package com.xiaolei.android.ui;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import com.xiaolei.android.BizTracker.R;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

/**
 * @author xiaolei
 * 
 */
public class LineChartFragment extends Fragment {

	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
	private String mDateFormat;
	private GraphicalView mChartView;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (savedInstanceState != null) {
			Serializable dataset = savedInstanceState
					.getSerializable("dataset");
			Serializable renderer = savedInstanceState
					.getSerializable("renderer");

			if (dataset != null) {
				mDataset = (XYMultipleSeriesDataset) dataset;
			}
			if (renderer != null) {
				mRenderer = (XYMultipleSeriesRenderer) renderer;
			}

			mDateFormat = savedInstanceState.getString("date_format");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View result = inflater.inflate(R.layout.line_chart_fragment, container,
				false);
		if (result != null) {
			configRenderer();
		}

		return result;
	}

	private void configRenderer() {
		mRenderer.setApplyBackgroundColor(true);
		mRenderer.setBackgroundColor(Color.argb(100, 50, 50, 50));
		mRenderer.setAxisTitleTextSize(16);
		mRenderer.setChartTitleTextSize(20);
		mRenderer.setLabelsTextSize(10);
		mRenderer.setLegendTextSize(15);
		mRenderer.setMargins(new int[] { 20, 30, 15, 20 });
		mRenderer.setZoomButtonsVisible(false);
		mRenderer.setXLabelsAngle(10);
		mRenderer.setShowGrid(true);
		mRenderer.setAntialiasing(true);
		mRenderer.setXTitle(getString(R.string.transaction_date));
		mRenderer.setYTitle(getString(R.string.money));

		mRenderer.setYLabels(10);
		mRenderer.setXLabels(5);
	}

	public void clear() {
		if (mDataset != null) {
			int seriesCount = mDataset.getSeriesCount();
			for (int i = seriesCount - 1; i >= 0; i--) {
				mDataset.removeSeries(i);
			}
		}
		if (mRenderer != null) {
			int rendererCount = mRenderer.getSeriesRendererCount();
			for (int i = rendererCount - 1; i >= 0; i--) {
				SimpleSeriesRenderer renderer = mRenderer
						.getSeriesRendererAt(i);
				if (renderer != null) {
					mRenderer.removeSeriesRenderer(renderer);
				}
			}
		}

		if (mChartView != null) {
			mChartView.repaint();
		}
	}

	/**
	 * Sets a few of the series renderer settings.
	 * 
	 * @param renderer
	 *            the renderer to set the properties to
	 * @param chartTitle
	 *            the chart title
	 * @param xTitle
	 *            the title for the X axis
	 * @param yTitle
	 *            the title for the Y axis
	 * @param xMin
	 *            the minimum value on the X axis
	 * @param xMax
	 *            the maximum value on the X axis
	 * @param yMin
	 *            the minimum value on the Y axis
	 * @param yMax
	 *            the maximum value on the Y axis
	 * @param axesColor
	 *            the axes color
	 * @param labelsColor
	 *            the labels color
	 */
	public void setChartSettings(String chartTitle, String xTitle,
			String yTitle, double xMin, double xMax, double yMin, double yMax,
			int axesColor, int labelsColor) {
		if (mRenderer != null) {
			mRenderer.setChartTitle(chartTitle);
			mRenderer.setXTitle(xTitle);
			mRenderer.setYTitle(yTitle);
			mRenderer.setXAxisMin(xMin);
			mRenderer.setXAxisMax(xMax);
			mRenderer.setYAxisMin(yMin);
			mRenderer.setYAxisMax(yMax);
			mRenderer.setAxesColor(axesColor);
			mRenderer.setLabelsColor(labelsColor);
		}
	}

	public void addLine(String title, Date[] xValues, double[] yValues,
			int lineColor) {
		if (mDataset == null) {
			mDataset = new XYMultipleSeriesDataset();
		}
		if (mRenderer == null) {
			mRenderer = new XYMultipleSeriesRenderer();
		}

		TimeSeries series = new TimeSeries(title);
		int seriesLength = xValues.length;
		for (int k = 0; k < seriesLength; k++) {
			series.add(xValues[k], yValues[k]);
		}

		XYSeriesRenderer renderer = new XYSeriesRenderer();
		renderer.setFillPoints(false);
		renderer.setColor(lineColor);

		mRenderer.addSeriesRenderer(renderer);
		mDataset.addSeries(series);

		if (mChartView == null) {
			LinearLayout container = (LinearLayout) getView().findViewById(
					R.id.linearLayoutChart);
			if (container != null) {
				mChartView = ChartFactory.getTimeChartView(getActivity(),
						mDataset, mRenderer, mDateFormat);

				container.addView(mChartView, new LayoutParams(
						LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
			}
		} else {
			mChartView.repaint();
		}
	}

	public void addLines(Cursor cursor, String positiveNumberLineName,
			String negativeNumberLineName, String dateTypeColumnName,
			String doubleTypeColumnName) throws ParseException {
		addLines(cursor, positiveNumberLineName, negativeNumberLineName,
				dateTypeColumnName, doubleTypeColumnName, getActivity()
						.getResources().getColor(R.color.incomeColor),
				getActivity().getResources().getColor(R.color.expenseColor));
	}

	public void addLines(Cursor cursor, String positiveNumberLineName,
			String negativeNumberLineName, String dateTypeColumnName,
			String doubleTypeColumnName, int positiveLineColor,
			int negativeLineColor) throws ParseException {
		if (cursor == null || cursor.isClosed()) {
			return;
		}
		
		if (mDataset == null) {
			mDataset = new XYMultipleSeriesDataset();
		}

		TimeSeries positiveNumberSeries = new TimeSeries(positiveNumberLineName);
		TimeSeries negativeNumberSeries = new TimeSeries(negativeNumberLineName);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		if (cursor.moveToFirst()) {
			do {
				double value = cursor.getDouble(cursor
						.getColumnIndex(doubleTypeColumnName));
				String dateString = cursor.getString(cursor
						.getColumnIndex(dateTypeColumnName));
				Date date = format.parse(dateString);

				if (value >= 0) {
					positiveNumberSeries.add(date, value);
				} else {
					negativeNumberSeries.add(date, Math.abs(value));
				}
			} while (cursor.moveToNext());
		}

		XYSeriesRenderer positiveNumberSeriesRenderer = new XYSeriesRenderer();
		positiveNumberSeriesRenderer.setFillPoints(false);
		positiveNumberSeriesRenderer.setColor(positiveLineColor);

		XYSeriesRenderer negativeNumberSeriesRenderer = new XYSeriesRenderer();
		negativeNumberSeriesRenderer.setFillPoints(false);
		negativeNumberSeriesRenderer.setColor(negativeLineColor);

		mRenderer.addSeriesRenderer(positiveNumberSeriesRenderer);
		mDataset.addSeries(positiveNumberSeries);
		mRenderer.addSeriesRenderer(negativeNumberSeriesRenderer);
		mDataset.addSeries(negativeNumberSeries);
		
		if (mChartView == null) {
			LinearLayout container = (LinearLayout) getView().findViewById(
					R.id.linearLayoutChart);
			if (container != null) {
				mChartView = ChartFactory.getTimeChartView(getActivity(),
						mDataset, mRenderer, mDateFormat);

				container.addView(mChartView, new LayoutParams(
						LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
			}
		}else{
			mChartView.repaint();
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mChartView == null) {
			LinearLayout container = (LinearLayout) getView().findViewById(
					R.id.linearLayoutChart);
			if (container != null) {
				mChartView = ChartFactory.getTimeChartView(getActivity(),
						mDataset, mRenderer, mDateFormat);

				container.addView(mChartView, new LayoutParams(
						LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
			}
		} else {
			mChartView.repaint();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putSerializable("dataset", mDataset);
		outState.putSerializable("renderer", mRenderer);
		outState.putString("date_format", mDateFormat);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

	}

	@Override
	public void onDestroyView() {

		super.onDestroyView();
	}

	public void showBusyIndicator() {
		if (getView() != null) {
			ViewFlipper viewFlipper = (ViewFlipper) getView().findViewById(
					R.id.viewFlipperLineChart);
			if (viewFlipper != null) {
				viewFlipper.setDisplayedChild(0);
			}
		}
	}

	public void showData() {
		if (getView() != null) {
			ViewFlipper viewFlipper = (ViewFlipper) getView().findViewById(
					R.id.viewFlipperLineChart);
			if (viewFlipper != null) {
				viewFlipper.setDisplayedChild(1);
			}
		}
	}

	public void showMessage(String message) {
		if (getView() != null) {
			ViewFlipper viewFlipper = (ViewFlipper) getView().findViewById(
					R.id.viewFlipperLineChart);
			TextView tvEmpty = (TextView) getView().findViewById(
					R.id.textViewEmpty);
			if (viewFlipper != null && tvEmpty != null) {
				tvEmpty.setText(message);
				viewFlipper.setDisplayedChild(2);
			}
		}
	}
}
