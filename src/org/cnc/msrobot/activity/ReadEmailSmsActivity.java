package org.cnc.msrobot.activity;

import java.util.ArrayList;

import org.cnc.msrobot.R;
import org.cnc.msrobot.module.ReadMessageModule;
import org.cnc.msrobot.module.ReadMessageModule.ReadMessageModuleListener;
import org.cnc.msrobot.resource.Email;
import org.cnc.msrobot.task.MarkEmailSeenTask;
import org.cnc.msrobot.task.ReadEmailTask;
import org.cnc.msrobot.task.ReadSMSTask;

import android.content.Context;
import android.os.Bundle;
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
	public static final String EXTRA_TYPE = "extra_type";
	public static final int TYPE_SENT_SMS = 0;
	public static final int TYPE_SENT_EMAIL = 1;

	private ListView mListView;
	private WebView mWebView;
	private ReadEmailAdapter mAdapter;
	private int mPosition = 0;
	private int mType = TYPE_SENT_SMS;
	private ReadMessageModule mAskModule;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// set action bar
		mActionbar.setOnClickListener(this);
		setContentView(R.layout.activity_read_email_sms);

		// get type
		mType = getIntent().getExtras().getInt(EXTRA_TYPE);

		// find view and set listener
		mListView = (ListView) findViewById(R.id.lvEmail);
		mWebView = (WebView) findViewById(R.id.webView);
		mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		ArrayList<EmailSmsItem> list = new ArrayList<EmailSmsItem>();
		if (mType == TYPE_SENT_EMAIL) {
			if (ReadEmailTask.emails != null) {
				for (int i = 0; i < ReadEmailTask.emails.size(); i++) {
					list.add(new EmailSmsItem(ReadEmailTask.emails.get(i).from, ReadEmailTask.emails.get(i).subject));
				}
			}
		} else if (mType == TYPE_SENT_SMS) {
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

		// init adapter and set adapter to list
		mAdapter = new ReadEmailAdapter(this, list);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
				if (mType == TYPE_SENT_EMAIL) {
					Email email = ReadEmailTask.emails.get(position);
					mWebView.loadData(email.htmlContent, "text/html", "UTF-8");
				} else {
					EmailSmsItem item = mAdapter.getItem(position);
					mWebView.loadData(item.subject, "text/html", "UTF-8");
				}
				mPosition = position;
				getTextToSpeech().stopSpeak();
				readEmail(mPosition);
			}
		});

		// init ask module
		mAskModule = new ReadMessageModule(this, input, output);
		mAskModule.setListener(new ReadMessageModuleListener() {
			@Override
			public void onNext() {
				getTextToSpeech().stopSpeak();
				readEmail(mPosition + 1);
			}
		});

		mListView.postDelayed(new Runnable() {

			@Override
			public void run() {
				readEmail(0);
			}
		}, 1000);
	}

	@Override
	protected void onStop() {
		super.onStop();
		mStt.stopListening();
	}

	private void readEmail(int position) {
		if (position >= mAdapter.getCount()) return;
		mPosition = position;
		mListView.setItemChecked(mPosition, true);
		mListView.setSelection(mPosition);

		String ask = "", content = "";
		if (mType == TYPE_SENT_EMAIL) {
			Email email = ReadEmailTask.emails.get(position);
			mWebView.loadData(email.htmlContent, "text/html", "UTF-8");
			ask = getString(R.string.email_ask_read_message, email.from, email.subject);
			content = ReadEmailTask.emails.get(mPosition).content;
			new MarkEmailSeenTask(position).execute();
		} else {
			EmailSmsItem item = mAdapter.getItem(position);
			mWebView.loadData(item.subject, "text/html", "UTF-8");
			ask = getString(R.string.sms_ask_read_message, item.from);
			content = mAdapter.getItem(mPosition).subject;
			// new MarkSmsSeenTask(this, position).execute();
		}
		mAskModule.readAsk(ask, content);
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
				readEmail(mPosition);
				break;
			case R.id.imgStop:
				getTextToSpeech().stopSpeak();
				break;
			case R.id.imgNext:
				if (mPosition < mAdapter.getCount() - 1) {
					readEmail(mPosition + 1);
				}
				break;
			default:
				break;
		}
	}
}
