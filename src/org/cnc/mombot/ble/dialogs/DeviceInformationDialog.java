package org.cnc.mombot.ble.dialogs;

import java.util.Date;

import org.ble.sensortag.dialogs.AppDialog;
import org.cnc.mombot.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class DeviceInformationDialog extends AppDialog<DeviceInformationDialog.DeviceInformationDialogListener> {
	public static final String TAG = DeviceInformationDialog.class.getSimpleName();
	public static final String ARG_DEVICE_NAME = "arg_device_name";
	public static final String ARG_DEVICE_ADDRESS = "arg_device_address";
	private String deviceName, deviceAddress;

	public interface DeviceInformationDialogListener {
		public void onOk(String name, String address, String code, String group, String location, String locationType,
				String node, Date batteryDate);

		public void onCancel();
	}

	public DeviceInformationDialog() {
		setCancelable(false);
	}

	@Override
	protected boolean isListenerOptional() {
		return false;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Bundle bundle = getArguments();
		if (bundle != null) {
			deviceName = bundle.getString(ARG_DEVICE_NAME);
			deviceAddress = bundle.getString(ARG_DEVICE_ADDRESS);
		}
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View root = inflater.inflate(R.layout.dialog_device_information, null);
		final TextView tvName, tvAddress;
		final EditText etCode, etGroup, etLocation, etLocationType, etNote;
		tvName = (TextView) root.findViewById(R.id.tvName);
		tvAddress = (TextView) root.findViewById(R.id.tvAddress);
		etCode = (EditText) root.findViewById(R.id.etCode);
		etGroup = (EditText) root.findViewById(R.id.etGroup);
		etLocation = (EditText) root.findViewById(R.id.etLocation);
		etLocationType = (EditText) root.findViewById(R.id.etLocationType);
		etNote = (EditText) root.findViewById(R.id.etNote);
		// set Text
		tvName.setText(deviceName);
		tvAddress.setText(deviceAddress);
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
				.setTitle("Complete device information").setView(root)
				.setPositiveButton(R.string.turn_on, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						getListener().onOk(deviceName, deviceAddress, etCode.getText().toString(),
								etGroup.getText().toString(), etLocation.getText().toString(),
								etLocationType.getText().toString(), etNote.getText().toString(), null);
					}
				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						getListener().onCancel();
					}
				}).setCancelable(false);

		return builder.create();
	}
}