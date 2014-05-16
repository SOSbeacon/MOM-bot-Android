package org.cnc.mombot.task;

import org.cnc.mombot.activity.BaseActivity;
import org.cnc.mombot.resource.SMS;

import android.content.ContentValues;
import android.net.Uri;
import android.os.AsyncTask;

public class MarkSmsSeenTask extends AsyncTask<Void, Void, Boolean> {
	private int pos;
	private BaseActivity mContext;

	/**
	 * @param context
	 *            MainActivity
	 * @param pos
	 *            position of SMS want to mark seen
	 */
	public MarkSmsSeenTask(BaseActivity context, int pos) {
		mContext = context;
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		try {
			if (ReadSMSTask.mListSMS != null && pos >= 0 && pos < ReadSMSTask.mListSMS.size()) {
				SMS sms = ReadSMSTask.mListSMS.get(pos);
				String id = sms.id;
				// uri for sms inbox
				Uri uri = Uri.parse("content://sms/inbox");
				// condition for unread sms
				String where = SMS.COLUMN_ID + "='" + id + "'";
				// update sms read status
				// set read status is 1 (read)
				ContentValues value = new ContentValues();
				value.put(SMS.COLUMN_READ, 1);
				mContext.getContentResolver().update(uri, value, where, null);
				sms.seen = 1;
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		if (result) {
		}
	}
}
