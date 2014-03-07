package org.cnc.msrobot.task;

import java.util.ArrayList;

import org.cnc.msrobot.R;
import org.cnc.msrobot.fragment.HomeFragment;
import org.cnc.msrobot.resource.SMS;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;

/**
 * read unread sms inbox and speech task
 * 
 * @author cnc
 * 
 */
public class ReadSMSTask extends AsyncTask<Void, Void, Boolean> {
	private ArrayList<SMS> mListSMS;
	private HomeFragment mContext;
	private int unReadCount;

	public ReadSMSTask(HomeFragment context) {
		mContext = context;
	}

	@Override
	protected Boolean doInBackground(Void... params) {
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
						mListSMS.add(new SMS(mContext.getBaseActivity(), c));
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
		if (mContext.getBaseActivity() == null) return;
		if (result && mListSMS != null && mContext.getTextToSpeech() != null) {
			// get string for speech unread sms
			unReadCount = mListSMS.size();
			String unReadString;
			if (unReadCount == 0) {
				unReadString = mContext.getBaseActivity().getString(R.string.sms_no_message);
			} else if (unReadCount > 1) {
				unReadString = mContext.getBaseActivity().getString(R.string.sms_unreads, unReadCount);
			} else {
				unReadString = mContext.getBaseActivity().getString(R.string.sms_unread, unReadCount);
			}
			// speech
			mContext.getTextToSpeech().speak(unReadString, TextToSpeech.QUEUE_ADD, null);
		}
		mContext.changeSmsItem(unReadCount);
	}

	public void speakSmsDetail() {
		if (mContext.getTextToSpeech() == null) return;
		if (mListSMS != null && mListSMS.size() > 0) {
			for (int i = 0; i < mListSMS.size(); i++) {
				SMS sms = mListSMS.get(i);
				// get string for speech sms
				String readMessage;
				if (TextUtils.isEmpty(sms.person)) {
					readMessage = mContext.getBaseActivity()
							.getString(R.string.sms_read_message, sms.address, sms.body);
				} else {
					readMessage = mContext.getBaseActivity().getString(R.string.sms_read_message, sms.person, sms.body);
				}
				// speech
				mContext.getTextToSpeech().speak(readMessage, TextToSpeech.QUEUE_ADD, null);
			}
		}
	}

	/**
	 * return unread sms count
	 * 
	 * @return
	 */
	public int getCount() {
		return unReadCount;
	}
}
