package org.cnc.mombot.ble.adapter;

import java.util.ArrayList;

import org.cnc.mombot.R;
import org.cnc.mombot.ble.resource.DataRecordedResource;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class BleDevicesReportAdapter extends ArrayAdapter<DataRecordedResource> {
	private final LayoutInflater inflater;

	public BleDevicesReportAdapter(Context context) {
		super(context, 0, new ArrayList<DataRecordedResource>());
		inflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		// General ListView optimization code.
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_list_device_report, null);
			viewHolder = new ViewHolder();
			viewHolder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
			viewHolder.tvData = (TextView) convertView.findViewById(R.id.tvData);

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		DataRecordedResource report = getItem(position);
		viewHolder.tvTime.setText(report.timeSaved);
		viewHolder.tvData.setText(report.data);
		return convertView;
	}

	private static class ViewHolder {
		TextView tvTime;
		TextView tvData;
	}
}
