package org.cnc.mombot.ble.activity;

import org.ble.sensortag.BleServiceBindingActivity;
import org.ble.sensortag.sensor.TiRangeSensors;
import org.ble.sensortag.sensor.TiSensor;
import org.ble.sensortag.sensor.TiSensors;
import org.cnc.mombot.R;
import org.cnc.mombot.ble.algorithm.AccelerometerCompass;
import org.cnc.mombot.ble.algorithm.SensorAlgorithmInterface;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.LineGraphView;

public class SensorLogActivity extends BleServiceBindingActivity implements SensorAlgorithmInterface {
	private final static String TAG = SensorLogActivity.class.getSimpleName();

	private LineGraphView graphView;
	private AccelerometerCompass algorithm;
	private GraphViewSeries seriesX;
	private GraphViewSeries seriesY;
	private GraphViewSeries seriesZ;
	private GraphViewSeries seriesValue;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sensor_log);
		graphView = new LineGraphView(this // context
				, "GraphViewDemo" // heading
		);
		GraphViewData[] data = new GraphViewData[MAX_SAMPLING];
		for (int i = 0; i < MAX_SAMPLING; i++) {
			data[i] = new GraphViewData(i, 0);
		}
		GraphViewSeriesStyle xStyle = new GraphViewSeriesStyle(Color.BLUE, 3);
		GraphViewSeriesStyle yStyle = new GraphViewSeriesStyle(Color.GREEN, 3);
		GraphViewSeriesStyle zStyle = new GraphViewSeriesStyle(Color.MAGENTA, 3);
		GraphViewSeriesStyle valueStyle = new GraphViewSeriesStyle(Color.RED, 5);
		seriesX = new GraphViewSeries("azimuth - Z axis", xStyle, data);
		seriesY = new GraphViewSeries("pitch - X axis", yStyle, data);
		seriesZ = new GraphViewSeries("roll - Y axis", zStyle, data);
		seriesValue = new GraphViewSeries("absolute value", valueStyle, data);

		// set styles
		// graphView.getGraphViewStyle().setGridColor(Color.GREEN);
		// graphView.getGraphViewStyle().setHorizontalLabelsColor(Color.RED);
		// graphView.getGraphViewStyle().setNumHorizontalLabels(10);
		graphView.getGraphViewStyle().setVerticalLabelsColor(Color.BLACK);
		graphView.getGraphViewStyle().setNumVerticalLabels(10);
		graphView.getGraphViewStyle().setVerticalLabelsWidth(100);
		graphView.setManualYAxisBounds(1, -1);
		graphView.addSeries(seriesX);
		graphView.addSeries(seriesY);
		graphView.addSeries(seriesZ);
		graphView.addSeries(seriesValue);
		graphView.setScrollable(true);
		// optional - activate scaling / zooming
		graphView.setScalable(true);
		// optional - legend
		graphView.setShowLegend(true);

		LinearLayout layoutContent = (LinearLayout) findViewById(R.id.layoutContent);
		layoutContent.addView(graphView);
	}

	@Override
	public void onDisconnected(String deviceAddress) {
		// finish();
	}

	@Override
	public void onServiceDiscovered(String deviceAddress) {
		algorithm = new AccelerometerCompass(this, deviceAddress);
	}

	@Override
	public void onDataAvailable(String deviceAddress, String serviceUuid, String characteristicUUid, String text,
			byte[] data) {
		final TiRangeSensors<float[], Float> sensor = (TiRangeSensors<float[], Float>) TiSensors.getSensor(serviceUuid);
		final float[] values = sensor.getData();
		algorithm.update(deviceAddress, serviceUuid, values);
	}

	@Override
	public void updateSensor(String deviceAddress, TiSensor<?> sensor) {
		getBleService().updateSensor(sensor);
	}

	@Override
	public void enableSensor(String deviceAddress, TiSensor<?> sensor, boolean enabled) {
		getBleService().enableSensor(sensor, enabled);
	}

	// we will draw graph after get sampling 50 times (5s)
	private static final int MAX_SAMPLING = 50;
	private int count = 0;
	GraphViewData[] dataValue = new GraphViewData[MAX_SAMPLING];
	GraphViewData[] dataX = new GraphViewData[MAX_SAMPLING];
	GraphViewData[] dataY = new GraphViewData[MAX_SAMPLING];
	GraphViewData[] dataZ = new GraphViewData[MAX_SAMPLING];

	@Override
	public void onOrientation(String deviceAddress, float[] values) {
		dataValue[count] = new GraphViewData(count, Math.sqrt(values[0] * values[0] + values[1] * values[1] + values[2]
				* values[2]));
		dataX[count] = new GraphViewData(count, values[0]);
		dataY[count] = new GraphViewData(count, values[1]);
		dataZ[count] = new GraphViewData(count, values[2]);
		count++;
		if (count == MAX_SAMPLING) {
			count = 0;
			seriesValue.resetData(dataValue);
			seriesX.resetData(dataX);
			seriesY.resetData(dataY);
			seriesZ.resetData(dataZ);
			phantich(dataZ);
		}
	}

	private static final double delta = 0.05d;

	private void phantich(GraphViewData[] dataY) {
		// Tim diem cao nhat
		int indexMax = 0, indexMin = 0;
		double max = -10, min = 10; // min 10 because value alway < g (9.81)
		for (int i = 0; i < dataY.length; i++) {
			if (dataY[i].getY() > max) {
				max = dataY[i].getY();
				indexMax = i;
			}
			if (dataY[i].getY() < min) {
				min = dataY[i].getY();
				indexMin = i;
			}
		}
		// Neu diem cao nhat truoc diem thap nhat -> dong cua
		// Neu diem cao nhat sau diem thap nhap -> mo cua
		// Va do lech phai lon hon delta
		if (Math.abs(max - min) > delta) {
			// showCenterToast("min: " + min + ", max: " + max + ", indexMin: " + indexMin + ", indexMax: " + indexMax);
			if (indexMax < indexMin) {
				// kiem tra do lech so voi 2s truoc do
				double check = 0;
				if (indexMax > 20) {
					check = dataY[indexMax - 20].getY();
				} else {
					check = dataY[0].getY();
				}
				if (Math.abs(max - check) > delta / 2 && check < max) {
					showCenterToast("Door close");
				} else {
					showCenterToast("Door open");
				}
			} else {
				showCenterToast("Door open");
			}
		} else {
			showCenterToast("no change");
		}
	}
}
