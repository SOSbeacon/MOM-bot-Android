package org.cnc.mombot.ble.activity;

import org.cnc.mombot.R;
import org.cnc.mombot.ble.adapter.BleDevicesReportAdapter;
import org.cnc.mombot.ble.resource.DataRecordedResource;
import org.cnc.mombot.provider.DbContract.TableDataRecorded;

import android.app.ListActivity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;

public class ReportActivity extends ListActivity implements LoaderCallbacks<Cursor> {
	public static final String EXTRA_DEVICE_ADDRESS = "device_address";
	private static final int LOADER_GET_REPORT = 1;
	private BleDevicesReportAdapter mAdapter;
	private String mDeviceAddress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.title_report);
		setContentView(R.layout.device_scan_activity);
		final View emptyView = findViewById(R.id.empty_view);
		getListView().setEmptyView(emptyView);

		// init adapter
		if (mAdapter == null) {
			mAdapter = new BleDevicesReportAdapter(this);
			setListAdapter(mAdapter);
		}

		mDeviceAddress = getIntent().getStringExtra(EXTRA_DEVICE_ADDRESS);

		// init loader
		getLoaderManager().initLoader(LOADER_GET_REPORT, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
		switch (id) {
		case LOADER_GET_REPORT:
			String where = TableDataRecorded.ADDRESS + "='" + mDeviceAddress + "'";
			return new CursorLoader(this, TableDataRecorded.CONTENT_URI, null, where, null, null);
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		int id = loader.getId();
		switch (id) {
		case LOADER_GET_REPORT:
			mAdapter.clear();
			if (cursor != null && cursor.moveToFirst()) {
				do {
					DataRecordedResource report = new DataRecordedResource(cursor);
					mAdapter.add(report);
				} while (cursor.moveToNext());
			}
			mAdapter.notifyDataSetChanged();
			break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
	}
}
