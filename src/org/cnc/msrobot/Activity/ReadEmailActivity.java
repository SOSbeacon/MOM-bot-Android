package org.cnc.msrobot.activity;

import java.util.ArrayList;

import org.cnc.msrobot.R;
import org.cnc.msrobot.resource.Email;
import org.cnc.msrobot.task.ReadEmailTask;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ReadEmailActivity extends BaseActivity {
	private ListView mListView;
	private ReadEmailAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_read_email);
		mListView = (ListView) findViewById(R.id.lvEmail);
		if (ReadEmailTask.emails != null) {
			mAdapter = new ReadEmailAdapter(this, ReadEmailTask.emails);
			mListView.setAdapter(mAdapter);
		}
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

			}
		});
	}

	public class ReadEmailAdapter extends ArrayAdapter<Email> {
		private LayoutInflater mInfalter;

		public ReadEmailAdapter(Context context, ArrayList<Email> list) {
			super(context, 0, list);
			mInfalter = LayoutInflater.from(context);

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			Email email = getItem(position);
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInfalter.inflate(R.layout.item_list_email, parent, false);
				holder.tvEmailFrom = (TextView) convertView.findViewById(R.id.tvEmailFrom);
				holder.tvEmailSubject = (TextView) convertView.findViewById(R.id.tvEmailSubject);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.tvEmailFrom.setText(email.from);
			holder.tvEmailSubject.setText(email.subject);
			convertView.setTag(holder);
			return convertView;
		}

		public class ViewHolder {
			TextView tvEmailFrom, tvEmailSubject;
		}
	}
}
