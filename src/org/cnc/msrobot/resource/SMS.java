package org.cnc.msrobot.resource;

import java.util.Date;

import org.cnc.msrobot.utils.Logger;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.PhoneLookup;
import android.text.TextUtils;

public class SMS {
	public static final String	COLUMN_ID			= "_id";
	public static final String	COLUMN_ADDRESS		= "address";
	public static final String	COLUMN_DATE			= "date";
	public static final String	COLUMN_SIZE			= "m_size";
	public static final String	COLUMN_DATE_SENT	= "date_sent";
	public static final String	COLUMN_READ			= "read";
	public static final String	COLUMN_STATUS		= "status";
	public static final String	COLUMN_TYPE			= "type";
	public static final String	COLUMN_SUBJECT		= "subject";
	public static final String	COLUMN_BODY			= "body";
	public static final String	COLUMN_SEEN			= "seen";

	// property of sms
	public String				id;
	public String				address;
	public String				person;
	public Date					date;
	public Date					dateSent;
	public int					size;
	public int					read;
	public int					status;
	public int					type;
	public String				subject;
	public String				body;
	public int					seen;

	public SMS(Context context, Cursor c) {
		int indexId = c.getColumnIndex(COLUMN_ID);
		int indexAddress = c.getColumnIndex(COLUMN_ADDRESS);
		int indexDate = c.getColumnIndex(COLUMN_DATE);
		int indexSize = c.getColumnIndex(COLUMN_SIZE);
		int indexDateSent = c.getColumnIndex(COLUMN_DATE_SENT);
		int indexRead = c.getColumnIndex(COLUMN_READ);
		int indexStatus = c.getColumnIndex(COLUMN_STATUS);
		int indexType = c.getColumnIndex(COLUMN_TYPE);
		int indexSubject = c.getColumnIndex(COLUMN_SUBJECT);
		int indexBody = c.getColumnIndex(COLUMN_BODY);
		int indexSeen = c.getColumnIndex(COLUMN_SEEN);

		if (indexId > -1) id = c.getString(indexId);
		if (indexAddress > -1) {
			address = c.getString(indexAddress);
			if (!TextUtils.isEmpty(address)) {
				person = getContactName(context, address);
			}
		}
		if (indexDate > -1) {
			date = new Date(c.getLong(indexDate));
		}
		if (indexSize > -1) size = c.getInt(indexSize);
		if (indexDateSent > -1) {
			dateSent = new Date(c.getLong(indexDateSent));
		}
		if (indexRead > -1) read = c.getInt(indexRead);
		if (indexStatus > -1) status = c.getInt(indexStatus);
		if (indexType > -1) type = c.getInt(indexStatus);
		if (indexSubject > -1) subject = c.getString(indexStatus);
		if (indexBody > -1) body = c.getString(indexBody);
		if (indexSeen > -1) seen = c.getInt(indexSeen);
	}

	public String getContactName(Context context, String phoneNumber) {
		ContentResolver cr = context.getContentResolver();
		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
		Cursor cursor = cr.query(uri, new String[] { PhoneLookup.DISPLAY_NAME }, null, null, null);
		if (cursor == null) { return null; }
		String contactName = null;
		if (cursor.moveToFirst()) {
			contactName = cursor.getString(cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME));
		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		return contactName;
	}

	public void printLogger() {
		Logger.info("SMS", "id: " + id + "; address: " + address + "; person: " + person + "; date: " + date
				+ "; dateSent: " + dateSent + "; size: " + size + "; read: " + read + "; status: " + status
				+ "; type: " + type + "; subject: " + subject + "; body: " + body + "; seen: " + seen);
	}
}
