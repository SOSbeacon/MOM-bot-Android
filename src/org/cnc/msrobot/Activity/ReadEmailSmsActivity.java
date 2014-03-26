package org.cnc.msrobot.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import org.cnc.msrobot.R;
import org.cnc.msrobot.resource.Email;
import org.cnc.msrobot.task.ReadEmailTask;
import org.cnc.msrobot.task.ReadSMSTask;
import org.cnc.msrobot.utils.CustomActionBar;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ReadEmailSmsActivity extends BaseActivity implements OnClickListener {
	private static final String VERB_ASK = "ask";
	private static final String VERB_READ = "read";
	public static final String EXTRA_REC = "extra_rec";

	private ListView mListView;
	private WebView mWebView;
	private ReadEmailAdapter mAdapter;
	private int mPosition = 0;
	private boolean mStop = false;
	private int mType = SendSmsEmailActivity.TYPE_SENT_SMS;
	private boolean isRec = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// set action bar
		isRec = getIntent().getExtras().getBoolean(EXTRA_REC, true);
		if (isRec) {
			mActionbar.setType(CustomActionBar.TYPE_EMAIL);
		}
		mActionbar.setOnClickListener(this);
		setContentView(R.layout.activity_read_email_sms);

		// get type
		mType = getIntent().getExtras().getInt(SendSmsEmailActivity.EXTRA_TYPE);

		// find view and set listener
		mListView = (ListView) findViewById(R.id.lvEmail);
		mWebView = (WebView) findViewById(R.id.webView);
		mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		ArrayList<EmailSmsItem> list = new ArrayList<EmailSmsItem>();
		if (mType == SendSmsEmailActivity.TYPE_SENT_EMAIL) {
			if (ReadEmailTask.emails != null) {
				for (int i = 0; i < ReadEmailTask.emails.size(); i++) {
					list.add(new EmailSmsItem(ReadEmailTask.emails.get(i).from, ReadEmailTask.emails.get(i).subject));
				}
			}
		} else if (mType == SendSmsEmailActivity.TYPE_SENT_SMS) {
			if (ReadSMSTask.mListSMS != null) {
				for (int i = 0; i < ReadSMSTask.mListSMS.size(); i++) {
					String person = ReadSMSTask.mListSMS.get(i).person;
					if (person == null) {
						person = "";
					}
					list.add(new EmailSmsItem(person + " " + ReadSMSTask.mListSMS.get(i).address, ReadSMSTask.mListSMS
							.get(i).body));
				}
			}
		}
		mAdapter = new ReadEmailAdapter(this, list);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
				if (mType == SendSmsEmailActivity.TYPE_SENT_EMAIL) {
					Email email = ReadEmailTask.emails.get(position);
					mWebView.loadData(email.htmlContent, "text/html", "UTF-8");
				} else {
					EmailSmsItem item = mAdapter.getItem(position);
					mWebView.loadData(item.subject, "text/html", "UTF-8");
				}
				mPosition = position;
				stopSpeak();
				readEmail(mPosition);
			}
		});

		mListView.postDelayed(new Runnable() {

			@Override
			public void run() {
				readEmail(0);
			}
		}, 1000);
	}

	private void speak(String msg, String verb) {
		HashMap<String, String> myHashAlarm = new HashMap<String, String>();
		myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_MUSIC));
		myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, verb);
		getTextToSpeech().speak(msg, TextToSpeech.QUEUE_FLUSH, myHashAlarm);
	}

	private void readEmail(int position) {
		if (position >= mAdapter.getCount()) return;
		mPosition = position;
		mListView.setItemChecked(mPosition, true);

		String msg = "";
		if (mType == SendSmsEmailActivity.TYPE_SENT_EMAIL) {
			Email email = ReadEmailTask.emails.get(position);
			mWebView.loadData(email.htmlContent, "text/html", "UTF-8");
			if (isRec) {
				msg = getString(R.string.email_ask_read_message, email.from, email.subject);
			} else {
				msg = getString(R.string.email_read_message, email.from, email.subject, email.content);
			}
		} else {
			EmailSmsItem item = mAdapter.getItem(position);
			mWebView.loadData(item.subject, "text/html", "UTF-8");
			if (isRec) {
				msg = getString(R.string.sms_ask_read_message, item.from);
			} else {
				msg = getString(R.string.sms_read_message, item.from, item.subject);
			}
		}
		if (isRec) {
			speak(msg, VERB_ASK);
		} else {
			speak(msg, VERB_READ);
		}
	}

	@Override
	public void onUtteranceCompleted(String utteranceId) {
		// neu stop tu action bar thi bo qua
		if (mStop) {
			mStop = false;
			return;
		}
		try {
			if (VERB_ASK.equals(utteranceId)) {
				// run listener for 200 ms delay
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						listen();
					}
				}, 200);
			} else if (VERB_READ.equals(utteranceId)) {
				// run listener for 200 ms delay
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						readEmail(mPosition + 1);
					}
				}, 1000);
			}
		} catch (Exception e) {
		}
	}

	@Override
	public void onRecognize(ArrayList<String> data) {
		if (data.size() > 0) {
			String answer = data.get(0);
			showCenterToast(getString(R.string.common_answer, answer));
			if (answer.toUpperCase(Locale.US).equals("YES")) {
				if (mType == SendSmsEmailActivity.TYPE_SENT_EMAIL) {
					Email email = ReadEmailTask.emails.get(mPosition);
					speak(email.content, VERB_READ);
				} else {
					EmailSmsItem item = mAdapter.getItem(mPosition);
					speak(item.subject, VERB_READ);
				}
			} else {
				// run listener for 200 ms delay
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						readEmail(mPosition + 1);
					}
				}, 1000);
			}
		} else {
			Email email = ReadEmailTask.emails.get(mPosition);
			speak(email.content, VERB_READ);
		}
	}

	private class EmailSmsItem {
		public String from, subject;

		public EmailSmsItem(String from, String subject) {
			this.from = from;
			this.subject = subject;
		}
	}

	public class ReadEmailAdapter extends ArrayAdapter<EmailSmsItem> {
		private LayoutInflater mInfalter;

		public ReadEmailAdapter(Context context, ArrayList<EmailSmsItem> list) {
			super(context, 0, list);
			mInfalter = LayoutInflater.from(context);

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			EmailSmsItem item = getItem(position);
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInfalter.inflate(R.layout.item_list_email, parent, false);
				holder.tvEmailFrom = (TextView) convertView.findViewById(R.id.tvEmailFrom);
				holder.tvEmailSubject = (TextView) convertView.findViewById(R.id.tvEmailSubject);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.tvEmailFrom.setText(item.from);
			holder.tvEmailSubject.setText(item.subject);
			convertView.setTag(holder);
			return convertView;
		}

		public class ViewHolder {
			TextView tvEmailFrom, tvEmailSubject;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.imgPlay:
				if (getTextToSpeech().isSpeaking()) {
					mStop = true;
				}
				readEmail(mPosition);
				break;
			case R.id.imgStop:
				if (getTextToSpeech().isSpeaking()) {
					mStop = true;
				}
				getTextToSpeech().stop();
				break;
			case R.id.imgNext:
				if (mPosition < mAdapter.getCount() - 1) {
					if (getTextToSpeech().isSpeaking()) {
						mStop = true;
					}
					readEmail(mPosition + 1);
				}
				break;
			default:
				break;
		}
	}
}
