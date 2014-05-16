package org.cnc.mombotble.adapter;

import java.util.ArrayList;

import org.cnc.mombot.R;
import org.cnc.mombot.provider.DbContract.TableDevice;
import org.cnc.mombotble.resource.DeviceResource;

import android.content.Context;
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

	public BleDevicesRecordAdapter(Context context) {
		super(context, 0, new ArrayList<DeviceResource>());
		inflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		ViewHolder viewHolder;
		// General ListView optimization code.
		if (view == null) {
			view = inflater.inflate(R.layout.li_device_recording, null);
			viewHolder = new ViewHolder();
			viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
			viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
			viewHolder.btnRecord = (Button) view.findViewById(R.id.btnRecord);

			viewHolder.btnRecord.setOnClickListener(this);

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
		viewHolder.deviceAddress.setText(device.address);
		// set address tag for button to remove
		viewHolder.btnRecord.setTag(device.address);
		return view;
	}

	private static class ViewHolder {
		TextView deviceName;
		TextView deviceAddress;
		Button btnRecord;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnRecord:
			String address = (String) v.getTag();
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
			break;
		default:
			break;
		}
	}
}
