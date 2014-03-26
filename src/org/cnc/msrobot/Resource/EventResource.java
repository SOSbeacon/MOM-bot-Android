package org.cnc.msrobot.resource;

import java.text.ParseException;
import java.util.Date;

import org.cnc.msrobot.provider.DbContract.TableEvent;
import org.cnc.msrobot.utils.DateTimeFormater;

import com.google.gson.annotations.SerializedName;

import android.content.ContentValues;
import android.database.Cursor;

public class EventResource implements BaseResource {
	@SerializedName("_id")
	public String id;
	public String title, content;
	private Date startTime, endTime;
	public String start, end;

	@Override
	public ContentValues prepareContentValue() {
		ContentValues value = new ContentValues();
		value.put(TableEvent._ID, id);
		value.put(TableEvent.TITLE, title);
		value.put(TableEvent.CONTENT, content);
		value.put(TableEvent.START, start.toString());
		value.put(TableEvent.END, end.toString());
		return value;
	}

	public Date start() {
		if (startTime == null) {
			try {
				startTime = DateTimeFormater.timeServerFormat.parse(start);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return startTime;
	}

	public Date end() {
		if (endTime == null) {
			try {
				endTime = DateTimeFormater.timeServerFormat.parse(end);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return endTime;
	}

	public void setStart(Date time) {
		startTime = time;
		start = DateTimeFormater.timeServerFormat.format(time);
	}

	public void setEnd(Date time) {
		endTime = time;
		end = DateTimeFormater.timeServerFormat.format(time);
	}

	public EventResource() {
	}

	public EventResource(Cursor cursor) {
		int indexId = cursor.getColumnIndex(TableEvent._ID);
		int indexSummary = cursor.getColumnIndex(TableEvent.TITLE);
		int indexDesc = cursor.getColumnIndex(TableEvent.CONTENT);
		int indexStart = cursor.getColumnIndex(TableEvent.START);
		int indexEnd = cursor.getColumnIndex(TableEvent.END);

		if (indexId > -1) id = cursor.getString(indexId);
		if (indexSummary > -1) title = cursor.getString(indexSummary);
		if (indexDesc > -1) content = cursor.getString(indexDesc);
		if (indexStart > -1) start = cursor.getString(indexStart);
		if (indexEnd > -1) end = cursor.getString(indexEnd);
		try {
			startTime = DateTimeFormater.timeServerFormat.parse(start);
			endTime = DateTimeFormater.timeServerFormat.parse(end);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}