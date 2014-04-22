package org.cnc.msrobot.resource;

import org.cnc.msrobot.provider.DbContract.TableContact;
import org.cnc.msrobot.provider.DbContract.TableGroupContact;

import android.content.ContentValues;
import android.database.Cursor;

public class GroupContactResource implements BaseResource {
	public int id;
	public String name;
	public ContactResource[] contacts;

	@Override
	public ContentValues prepareContentValue() {
		ContentValues value = new ContentValues();
		value.put(TableGroupContact._ID, id);
		value.put(TableGroupContact.NAME, name);
		return value;
	}

	public ContentValues[] prepareContactContentValue() {
		if (contacts == null) return null;
		ContentValues[] values = new ContentValues[contacts.length];
		for (int i = 0; i < contacts.length; i++) {
			values[i] = contacts[i].prepareContentValue();
			values[i].put(TableContact.GROUP_ID, id);
		}
		return values;
	}

	public GroupContactResource() {
	}

	public GroupContactResource(Cursor cursor) {
		int indexId = cursor.getColumnIndex(TableGroupContact._ID);
		int indexName = cursor.getColumnIndex(TableGroupContact.NAME);

		if (indexId > -1) id = cursor.getInt(indexId);
		if (indexName > -1) name = cursor.getString(indexName);
	}
}
