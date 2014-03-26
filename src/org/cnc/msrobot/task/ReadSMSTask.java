package org.cnc.msrobot.task;

import java.util.ArrayList;

import org.cnc.msrobot.R;
import org.cnc.msrobot.activity.MainActivity;
import org.cnc.msrobot.resource.SMS;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;

/**
 * read unread sms inbox and speech task
 * 
 * @author cnc
 * 
 */
public class ReadSMSTask extends AsyncTask<Void, Void, Boolean> {
	public static ArrayList<SMS> mListSMS;
	private MainActivity mContext;

	public ReadSMSTask(MainActivity context) {
		mContext = context;
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		if (mContext == null) return false;
		try {
			// uri for sms inbox
			Uri uri = Uri.parse("content://sms/inbox");
			// condition for unread sms
			String where = SMS.COLUMN_READ + "=0";
			// query sms
			Cursor c = mContext.getContentResolver().query(uri, null, where, null, null);
			if (c != null) {
				if (c.moveToFirst()) {
					// add unread sms to list
					mListSMS = new ArrayList<SMS>();
					do {
						mListSMS.add(new SMS(mContext, c));
					} while (c.moveToNext());
				}
				c.close();
			}
			// update sms read status
			if (mListSMS != null && mListSMS.size() > 0) {
				// set read status is 1 (read)
				ContentValues value = new ContentValues();
				value.put(SMS.COLUMN_READ, 1);
				mContext.getContentResolver().update(uri, value, where, null);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		if (mContext == null) return;
		int unReadCount = 0;
		if (result && mListSMS != null) {
			// get string for speech unread sms
			unReadCount = mListSMS.size();
			String unReadString;
			if (unReadCount == 0) {
				unReadString = mContext.getString(R.string.sms_no_message);
			} else if (unReadCount > 1) {
				unReadString = mContext.getString(R.string.sms_unreads, unReadCount);
			} else {
				unReadString = mContext.getString(R.string.sms_unread, unReadCount);
			}
			// speech
			mContext.speak(unReadString, TextToSpeech.QUEUE_ADD);
		}
		mContext.changeSmsItem(unReadCount);
	}
}
