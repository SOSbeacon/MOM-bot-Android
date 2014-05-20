package org.cnc.mombotble.resource;

import java.text.ParseException;
import java.util.Date;

import org.cnc.mombot.provider.DbContract.TableDevice;
import org.cnc.mombot.utils.DateTimeFormater;

import android.content.ContentValues;
import android.database.Cursor;

public class DeviceResource implements BaseResource {
	public static final int STATUS_CONNECTED = 1;
	public static final int STATUS_DISCONNECTED = 0;
	public int _id;
	public String name, address, code, manufacturer, group, location, locationType, note;
	public int status;
	public Date batteryDate;

	@Override
	public ContentValues prepareContentValue() {
		ContentValues value = new ContentValues();
		value.put(TableDevice._ID, _id);
		value.put(TableDevice.ADDRESS, address);
		value.put(TableDevice.CODE, code);
		value.put(TableDevice.NAME, name);
		value.put(TableDevice.STATUS, status);
		value.put(TableDevice.MANUFACTURER, manufacturer);
		value.put(TableDevice.GROUP, group);
		value.put(TableDevice.LOCATION, location);
		value.put(TableDevice.LOCATION_TYPE, locationType);
		value.put(TableDevice.NOTE, note);
		if (batteryDate != null) {
			value.put(TableDevice.BATTERY_DATE, batteryDate.toString());
		}
		return value;
	}

	public DeviceResource() {
	}

	public DeviceResource(String name, String address, String manufacturer, String code, String group, String location,
			String locationType, String note, Date batteryDate) {
		this.name = name;
		this.address = address;
		this.manufacturer = manufacturer;
		this.code = code;
		this.group = group;
		this.location = location;
		this.locationType = locationType;
		this.note = note;
		this.batteryDate = batteryDate;
	}

	public DeviceResource(Cursor cursor) {
		int indexId = cursor.getColumnIndex(TableDevice._ID);
		int indexAddress = cursor.getColumnIndex(TableDevice.ADDRESS);
		int indexCode = cursor.getColumnIndex(TableDevice.CODE);
		int indexName = cursor.getColumnIndex(TableDevice.NAME);
		int indexManufacturer = cursor.getColumnIndex(TableDevice.MANUFACTURER);
		int indexGroup = cursor.getColumnIndex(TableDevice.GROUP);
		int indexLocation = cursor.getColumnIndex(TableDevice.LOCATION);
		int indexLocationType = cursor.getColumnIndex(TableDevice.LOCATION_TYPE);
		int indexNote = cursor.getColumnIndex(TableDevice.NOTE);
		int indexBatteryDate = cursor.getColumnIndex(TableDevice.BATTERY_DATE);
		int indexStatus = cursor.getColumnIndex(TableDevice.STATUS);

		if (indexId > -1)
			_id = cursor.getInt(indexId);
		if (indexAddress > -1)
			address = cursor.getString(indexAddress);
		if (indexCode > -1)
			code = cursor.getString(indexCode);
		if (indexName > -1)
			name = cursor.getString(indexName);
		if (indexManufacturer > -1)
			manufacturer = cursor.getString(indexManufacturer);
		if (indexGroup > -1)
			group = cursor.getString(indexGroup);
		if (indexLocation > -1)
			location = cursor.getString(indexLocation);
		if (indexLocationType > -1)
			locationType = cursor.getString(indexLocationType);
		if (indexNote > -1)
			note = cursor.getString(indexLocationType);
		if (indexStatus > -1)
			status = cursor.getInt(indexStatus);
		try {
			if (indexBatteryDate > -1)
				batteryDate = DateTimeFormater.timeServerFormat.parse(cursor.getString(indexLocationType));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
}
