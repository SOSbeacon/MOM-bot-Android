package org.cnc.mombot.ble.adapter;

import java.util.ArrayList;

import org.cnc.mombot.R;
import org.cnc.mombot.ble.algorithm.DoorStatusAlgorithm;
import org.cnc.mombot.ble.resource.DeviceResource;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Adapter for holding devices found through scanning. Created by steven on 9/5/13.
 */
public class BleDevicesRecordAdapter extends ArrayAdapter<DeviceResource> implements OnClickListener {
	private final LayoutInflater inflater;
	private BleDeviceRecordAdapterCallback callback;

	public interface BleDeviceRecordAdapterCallback {
		public void removeDevice(String deviceAddress, String deviceCode);
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
			viewHolder.deviceCode = (TextView) view.findViewById(R.id.device_name);
			viewHolder.deviceStatus = (TextView) view.findViewById(R.id.device_status);
			viewHolder.tvRemove = (TextView) view.findViewById(R.id.tvRemove);
			viewHolder.tvDoorStatus = (TextView) view.findViewById(R.id.tvDoorStatus);

			viewHolder.tvRemove.setOnClickListener(this);
			viewHolder.deviceCode.setOnClickListener(this);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}

		DeviceResource device = getItem(i);
		final String deviceCode = device.code;
		final String deviceLocation = device.location;
		if (deviceCode != null && deviceCode.length() > 0)
			viewHolder.deviceCode.setText(deviceCode + " - " + deviceLocation);
		else
			viewHolder.deviceCode.setText(getContext().getString(R.string.unknown_device) + " - " + deviceLocation);
		// set device status
		if (device.status == DeviceResource.STATUS_CONNECTED)
			viewHolder.deviceStatus.setText("Connected");
		else if (device.status == DeviceResource.STATUS_CONNECTING)
			viewHolder.deviceStatus.setText("Connecting");
		else if (device.status == DeviceResource.STATUS_RECONNECT)
			viewHolder.deviceStatus.setText("Reconnect. Please power on sensor again!");
		else if (device.status == DeviceResource.STATUS_ERROR)
			viewHolder.deviceStatus.setText("Connect error");
		else if (device.status == DeviceResource.STATUS_DISCONNECTED)
			viewHolder.deviceStatus.setText("Disconnected");
		// set device address
		viewHolder.deviceAddress.setText(device.address);
		// set address tag for button to remove
		viewHolder.tvRemove.setTag(device.address);
		viewHolder.deviceCode.setTag(device.address);
		if (device.doorStatus.equals(DoorStatusAlgorithm.DOOR_STATUS_CLOSE))
			viewHolder.tvDoorStatus.setSelected(false);
		else
			viewHolder.tvDoorStatus.setSelected(true);
		viewHolder.tvDoorStatus.setText(device.doorStatus);
		return view;
	}

	private static class ViewHolder {
		TextView deviceCode;
		TextView deviceAddress;
		TextView deviceStatus;
		TextView tvRemove;
		TextView tvDoorStatus;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		// case R.id.device_name: {
		// String address = (String) v.getTag();
		// Intent intent = new Intent(getContext(), SensorLogActivity.class);
		// intent.putExtra(BleServiceBindingActivity.EXTRAS_DEVICE_ADDRESS, address);
		// getContext().startActivity(intent);
		// break;
		// }
		case R.id.tvRemove:
			String address = (String) v.getTag();
			String deviceCode = null;
			// remove on adapter
			for (int i = 0; i < getCount(); i++) {
				if (getItem(i).address.equals(address)) {
					deviceCode = getItem(i).code;
					remove(getItem(i));
					notifyDataSetChanged();
					break;
				}
			}

			// callback
			if (callback != null) {
				callback.removeDevice(address, deviceCode);
			}
			break;
		}
	}
}
