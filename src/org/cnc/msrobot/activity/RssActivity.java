package org.cnc.msrobot.activity;

import java.util.ArrayList;

import org.cnc.mombot.R;
import org.mcsoxford.rss.RSSFeed;
import org.mcsoxford.rss.RSSItem;
import org.mcsoxford.rss.RSSReader;
import org.mcsoxford.rss.RSSReaderException;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class RssActivity extends BaseActivity {
	private ListView mListView;
	private RssAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rss);
		mListView = (ListView) findViewById(R.id.lvRss);
		mAdapter = new RssAdapter(this, new ArrayList<RSSItem>());
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				RSSItem rss = mAdapter.getItem(arg2);
				if (rss == null) return;
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(rss.getLink());
				startActivity(intent);
			}
		});
		new ReadRSSTask().execute();
	}

	public class RssAdapter extends ArrayAdapter<RSSItem> {
		private LayoutInflater mInfalter;

		public RssAdapter(Context context, ArrayList<RSSItem> list) {
			super(context, 0, list);
			mInfalter = LayoutInflater.from(context);

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			RSSItem rss = getItem(position);
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInfalter.inflate(R.layout.item_list_rss, parent, false);
				holder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
				holder.tvDesc = (TextView) convertView.findViewById(R.id.tvDesc);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.tvTitle.setText(rss.getTitle());
			holder.tvDesc.setText(rss.getDescription());
			convertView.setTag(holder);
			return convertView;
		}

		public class ViewHolder {
			TextView tvTitle, tvDesc;
		}
	}

	public class ReadRSSTask extends AsyncTask<Void, Void, Void> {
		RSSFeed feed;

		@Override
		protected Void doInBackground(Void... params) {
			try {
				RSSReader reader = new RSSReader();
				String uri = "http://feeds.bbci.co.uk/news/world/rss.xml";
				feed = reader.load(uri);
				reader.close();
			} catch (RSSReaderException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (feed != null && feed.getItems() != null && feed.getItems().size() > 0) {
				mAdapter.clear();
				for (int i = 0; i < feed.getItems().size(); i++)
					mAdapter.add(feed.getItems().get(i));
				mAdapter.notifyDataSetChanged();
			}
		}
	}
}
