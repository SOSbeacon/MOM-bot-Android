package org.cnc.mombot.ble.adapter;

import java.util.ArrayList;

import org.cnc.mombot.R;
import org.cnc.mombot.ble.activity.ReportActivity;
import org.cnc.mombot.ble.resource.DeviceResource;
import org.cnc.mombot.provider.DbContract.TableDevice;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

/**
 * Adapter for holding devices found through scanning. Created by steven on 9/5/13.
 */
public class BleDevicesRecordAdapter extends ArrayAdapter<DeviceResource> implements OnClickListener {
	private final LayoutInflater inflater;
	private BleDeviceRecordAdapterCallback callback;

	public interface BleDeviceRecordAdapterCallback {
		public void removeDevice(String deviceAddress);
	}

	public BleDevicesRecordAdapter(Context context, BleDeviceRecordAdapterCallback callback) {
		super(context, 0, new ArrayList<DeviceResource>());
		inflater = LayoutInflater.from(context);
		this.callback = callback;
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		ViewHolder viewHolder;
		// General ListView optimization code.
		if (view == null) {
			view = inflater.inflate(R.layout.item_list_device_recording, null);
			viewHolder = new ViewHolder();
			viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
			viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
			viewHolder.deviceStatus = (TextView) view.findViewById(R.id.device_status);
			viewHolder.btnRemove = (Button) view.findViewById(R.id.btnRemove);
			viewHolder.btnReport = (Button) view.findViewById(R.id.btnReport);

			viewHolder.btnRemove.setOnClickListener(this);
			viewHolder.btnReport.setOnClickListener(this);

			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}

		DeviceResource device = getItem(i);
		final String deviceName = device.name;
		if (deviceName != null && deviceName.length() > 0)
			viewHolder.deviceName.setText(deviceName);
		else
			viewHolder.deviceName.setText(R.string.unknown_device);
		// set device status
		if (device.status == DeviceResource.STATUS_CONNECTED)
			viewHolder.deviceStatus.setText("Connected");
		else
			viewHolder.deviceStatus.setText("Disconnected");
		// set device address
		viewHolder.deviceAddress.setText(device.address);
		// set address tag for button to remove
		viewHolder.btnRemove.setTag(device.address);
		viewHolder.btnReport.setTag(device.address);
		return view;
	}

	private static class ViewHolder {
		TextView deviceName;
		TextView deviceAddress;
		TextView deviceStatus;
		Button btnRemove;
		Button btnReport;
	}

	@Override
	public void onClick(View v) {
		String address = (String) v.getTag();
		switch (v.getId()) {
		case R.id.btnReport:
			Intent intent = new Intent(getContext(), ReportActivity.class);
			intent.putExtra(ReportActivity.EXTRA_DEVICE_ADDRESS, address);
			getContext().startActivity(intent);
			break;
		case R.id.btnRemove:
			// remove on adapter
			for (int i = 0; i < getCount(); i++) {
				if (getItem(i).address.equals(address)) {
					remove(getItem(i));
					notifyDataSetChanged();
					break;
				}
			}
			// remove on database
			String where = TableDevice.ADDRESS + "='" + address + "'";
			getContext().getContentResolver().delete(TableDevice.CONTENT_URI, where, null);
			// callback
			if (callback!=null) {
				callback.removeDevice(address);
			}
			break;
		default:
			break;
		}
	}
}
