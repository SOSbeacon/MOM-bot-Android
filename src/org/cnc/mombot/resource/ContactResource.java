package org.cnc.mombot.resource;

import org.cnc.mombot.provider.DbContract.TableContact;

import android.content.ContentValues;
import android.database.Cursor;

public class ContactResource implements BaseResource {
	public int id, groupId;
	public String name, email, phone;

	@Override
	public ContentValues prepareContentValue() {
		ContentValues value = new ContentValues();
		value.put(TableContact._ID, id);
		value.put(TableContact.NAME, name);
		value.put(TableContact.EMAIL, email);
		value.put(TableContact.MOBILE, phone);
		value.put(TableContact.GROUP_ID, groupId);
		return value;
	}

	public ContactResource() {
	}

	public ContactResource(Cursor cursor) {
		int indexId = cursor.getColumnIndex(TableContact._ID);
		int indexName = cursor.getColumnIndex(TableContact.NAME);
		int indexEmail = cursor.getColumnIndex(TableContact.EMAIL);
		int indexMobile = cursor.getColumnIndex(TableContact.MOBILE);
		int indexGroupId = cursor.getColumnIndex(TableContact.GROUP_ID);

		if (indexId > -1) id = cursor.getInt(indexId);
		if (indexName > -1) name = cursor.getString(indexName);
		if (indexEmail > -1) email = cursor.getString(indexEmail);
		if (indexMobile > -1) phone = cursor.getString(indexMobile);
		if (indexGroupId > -1) groupId = cursor.getInt(indexGroupId);
	}
}
