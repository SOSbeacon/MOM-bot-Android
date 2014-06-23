package org.cnc.mombot.ble.resource;

import org.cnc.mombot.provider.DbContract.TableDataRecorded;

import android.content.ContentValues;
import android.database.Cursor;

public class DataRecordedResource implements BaseResource {
	public static final int STATE_SYNCED = 1;
	public static final int STATE_NOT_SYNCED = 0;
	public String address, data, timeSaved, serviceUUID;
	public int synced;

	@Override
	public ContentValues prepareContentValue() {
		ContentValues value = new ContentValues();
		value.put(TableDataRecorded.ADDRESS, address);
		value.put(TableDataRecorded.DATA, data);
		value.put(TableDataRecorded.TIME_SAVED, timeSaved);
		value.put(TableDataRecorded.SERVICE_UUID, serviceUUID);
		value.put(TableDataRecorded.SYNCED, synced);
		return value;
	}

	public DataRecordedResource() {
	}

	public DataRecordedResource(String address, String serviceUUID, String timeSaved, String data) {
		this.address = address;
		this.serviceUUID = serviceUUID;
		this.timeSaved = timeSaved;
		this.data = data;
	}

	public DataRecordedResource(Cursor cursor) {
		int indexAddress = cursor.getColumnIndex(TableDataRecorded.ADDRESS);
		int indexServiceUUID = cursor.getColumnIndex(TableDataRecorded.SERVICE_UUID);
		int indexTimeSaved = cursor.getColumnIndex(TableDataRecorded.TIME_SAVED);
		int indexData = cursor.getColumnIndex(TableDataRecorded.DATA);
		int indexSynced = cursor.getColumnIndex(TableDataRecorded.SYNCED);

		if (indexAddress > -1)
			address = cursor.getString(indexAddress);
		if (indexServiceUUID > -1)
			serviceUUID = cursor.getString(indexServiceUUID);
		if (indexTimeSaved > -1)
			timeSaved = cursor.getString(indexTimeSaved);
		if (indexData > -1)
			data = cursor.getString(indexData);
		if (indexSynced > -1)
			synced = cursor.getInt(indexSynced);
	}
}
