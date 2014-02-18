package org.cnc.msrobot.Utils;

import java.util.ArrayList;

import org.cnc.msrobot.R;
import org.cnc.msrobot.Resource.SMS;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;

public class ReadSMSTask extends AsyncTask<Void, Void, Void> {
	private ArrayList<SMS>	mListSMS;
	private Context			mContext;
	private TextToSpeech	mTts;

	public ReadSMSTask(Context context, TextToSpeech tts) {
		mContext = context;
		mTts = tts;
	}

	@Override
	protected Void doInBackground(Void... params) {
		Uri uri = Uri.parse("content://sms/inbox");
		String where = SMS.COLUMN_READ + "=0";
		Cursor c = mContext.getContentResolver().query(uri, null, where, null, null);
		if (c != null) {
			int unReadCount = c.getCount();
			String unReadString;
			if (unReadCount > 1) {
				unReadString = mContext.getResources().getString(R.string.sms_unreads, unReadCount);
			} else {
				unReadString = mContext.getResources().getString(R.string.sms_unread, unReadCount);
			}
			if (mTts != null) mTts.speak(unReadString, TextToSpeech.QUEUE_FLUSH, null);
			if (c.moveToFirst()) {
				mListSMS = new ArrayList<SMS>();
				do {
					mListSMS.add(new SMS(mContext, c));
				} while (c.moveToNext());
			}
			c.close();
		}
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		if (mTts == null) return;
		if (mListSMS != null && mListSMS.size() > 0) {
			for (int i = 0; i < mListSMS.size(); i++) {
				SMS sms = mListSMS.get(i);
				String readMessage;
				if (TextUtils.isEmpty(sms.person)) {
					readMessage = mContext.getResources().getString(R.string.sms_read_message, sms.address, sms.body);
				} else {
					readMessage = mContext.getResources().getString(R.string.sms_read_message, sms.person, sms.body);
				}
				mTts.speak(readMessage, TextToSpeech.QUEUE_ADD, null);
			}
		}
	}
}
