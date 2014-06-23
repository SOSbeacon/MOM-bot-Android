package org.cnc.mombot.ble.dialogs;

import java.util.Date;
import java.util.regex.Pattern;

import org.ble.sensortag.dialogs.AppDialog;
import org.cnc.mombot.R;
import org.cnc.mombot.provider.DbContract.TableDevice;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class DeviceInformationDialog extends AppDialog<DeviceInformationDialog.DeviceInformationDialogListener> {
	public static final String TAG = DeviceInformationDialog.class.getSimpleName();
	public static final String ARG_DEVICE_NAME = "arg_device_name";
	public static final String ARG_DEVICE_ADDRESS = "arg_device_address";
	private String deviceName, deviceAddress;
	private TextView tvName, tvAddress;
	private EditText etCode, etGroup, etLocation, etLocationType, etNote;

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
	public void onStart() {
		super.onStart();
		AlertDialog d = (AlertDialog) getDialog();
		if (d != null) {
			Button positiveButton = (Button) d.getButton(Dialog.BUTTON_POSITIVE);
			positiveButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// validation
					if (!TextUtils.isEmpty(etCode.getText())
							&& !Pattern.matches("^[a-zA-Z0-9_]*$", etCode.getText().toString())
							&& etCode.getText().toString().trim().length() > 0
							&& etCode.getText().toString().trim().length() < 50) {
						etCode.setError("The sensor code must be between 1 and 50 characters in the set [a-z,A-Z,0-9]");
						etCode.requestFocus();
					} else {
						// validation for exists code
						String where = TableDevice.CODE + " = '" + etCode.getText().toString().trim() + "'";
						Cursor c = getActivity().getContentResolver().query(TableDevice.CONTENT_URI, null, where, null, null);
						if (c!=null ) {
							if (c.getCount()>0) {
								etCode.setError("The sensor code has existed. Please enter another sensor code.");
								etCode.requestFocus();
								c.close();
								return;
							}
							c.close();
						}
						getListener().onOk(deviceName, deviceAddress, etCode.getText().toString(),
								etGroup.getText().toString(), etLocation.getText().toString(),
								etLocationType.getText().toString(), etNote.getText().toString(), null);
						dismiss();
					}
				}
			});
		}
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