package org.cnc.msrobot.fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import org.cnc.msrobot.R;
import org.cnc.msrobot.activity.EmailSetupActivity;
import org.cnc.msrobot.activity.MainActivity;
import org.cnc.msrobot.activity.ReadEmailActivity;
import org.cnc.msrobot.activity.RssActivity;
import org.cnc.msrobot.activity.WeatherActivity;
import org.cnc.msrobot.provider.DbContract.TableEvent;
import org.cnc.msrobot.resource.EventResource;
import org.cnc.msrobot.resource.ItemListFunction;
import org.cnc.msrobot.resource.WeatherResource;
import org.cnc.msrobot.task.ReadEmailTask;
import org.cnc.msrobot.task.ReadSMSTask;
import org.cnc.msrobot.utils.Actions;
import org.cnc.msrobot.utils.AppUtils;
import org.cnc.msrobot.utils.Consts;
import org.cnc.msrobot.utils.Consts.RequestCode;
import org.cnc.msrobot.utils.Consts.URLConsts;
import org.cnc.msrobot.utils.DateTimeFormater;
import org.cnc.msrobot.utils.LocationUtils;
import org.cnc.msrobot.utils.LocationUtils.LocationUtilsListener;
import org.cnc.msrobot.utils.Logger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

public class HomeFragment extends BaseFragment implements LoaderCallbacks<Cursor> {

	private static final int LOADER_GET_LIST_EVENT = 1;
	private static final int FUNCTION_ITEM_COUNT = 3;
	private View mLayout;
	private MainAdapter adapter;
	private ReadSMSTask readSmsTask;
	private LocationUtils mLocationUtils;
	private Location mLastLocation;

	private ImageLoader mImageLoader = ImageLoader.getInstance();
	private static final DisplayImageOptions imageDisplayOptions = new DisplayImageOptions.Builder()
			.bitmapConfig(Bitmap.Config.RGB_565).imageScaleType(ImageScaleType.IN_SAMPLE_INT)
			.resetViewBeforeLoading(true).cacheInMemory(true).cacheOnDisc(true).build();

	private static SimpleDateFormat timeFormater = new SimpleDateFormat("HH:mm", Locale.US);
	public SimpleDateFormat dateFormater = new SimpleDateFormat("EEE, MMM d", Locale.US);
	private Listener<WeatherResource> mRequestWeatherListener = new Listener<WeatherResource>() {
		@Override
		public void onResponse(WeatherResource response) {
			showWeather(response);
		}
	};
	private Listener<EventResource[]> mRequestEventListener = new Listener<EventResource[]>() {

		@Override
		public void onResponse(EventResource[] response) {
			// hide action bar progress loading
			getBaseActivity().hideActionBarProgressBar();
		}
	};
	private ErrorListener mErrorListener = new ErrorListener() {

		@Override
		public void onErrorResponse(VolleyError error) {
			// hide action bar progress loading
			getBaseActivity().hideActionBarProgressBar();
		}
	};

	private LocationUtilsListener mLocationRecall = new LocationUtilsListener() {
		@Override
		public void locationSettingCancel() {
		}

		@Override
		public void locationChanged(Location location) {
			boolean bestLocation = mLocationUtils.isBetterLocation(location, mLastLocation);
			if (bestLocation) {
				requestWeather(location);
			} else if (mLastLocation != null) {
				requestWeather(mLastLocation);
			}
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mLayout = inflater.inflate(R.layout.fragment_home, container, false);

		ListView lv = (ListView) mLayout.findViewById(R.id.lvMain);
		lv.setAdapter(adapter);

		// init task read sms
		readSmsTask = new ReadSMSTask(this);
		readSmsTask.execute();
		// init task read email
		String user = mSharePrefs.getGmailUsername();
		String pass = mSharePrefs.getGmailPass();
		if (!TextUtils.isEmpty(user) && !TextUtils.isEmpty(pass)) {
			new ReadEmailTask(this).execute(user, pass);
		} else {
			changeEmailItem(0, true);
		}

		initWeahter();
		return mLayout;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initFunctionList();
		initCursorLoader();
		requestListEvent();
	}

	@Override
	public void onDetach() {
		super.onDetach();
		readSmsTask.cancel(true);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == RequestCode.REQUEST_EMAIL_SETUP && resultCode == Activity.RESULT_OK) {
			String user = data.getStringExtra(EmailSetupActivity.EXTRA_EMAIL);
			String pass = data.getStringExtra(EmailSetupActivity.EXTRA_PASS);
			new ReadEmailTask(this).execute(user, pass);
		}
	}

	private void initFunctionList() {
		adapter = new MainAdapter(getBaseActivity(), new ArrayList<ItemListFunction>());
		// add sms status
		adapter.add(new ItemListFunction.Builder(getBaseActivity()).setType(ItemListFunction.TYPE_FUNCTION)
				.setIconResId(R.drawable.img_icon_sms).setDescResId(R.string.function_desc_getting_data)
				.setItemClickId(ItemListFunction.FUNCTION_READ_SMS)
				.setFunction1ClickId(ItemListFunction.FUNCTION_SENT_TEXT_SMS)
				.setFunction1TextResId(R.string.function_sent_text_sms)
				.setFunction2ClickId(ItemListFunction.FUNCTION_SENT_PICTURE_MMS)
				.setFunction2TextResId(R.string.function_sent_picture_mms).build());
		// add email status
		adapter.add(new ItemListFunction.Builder(getBaseActivity()).setType(ItemListFunction.TYPE_FUNCTION)
				.setIconResId(R.drawable.img_icon_email).setDescResId(R.string.function_desc_getting_data)
				.setItemClickId(ItemListFunction.FUNCTION_READ_EMAIL)
				.setFunction1ClickId(ItemListFunction.FUNCTION_SENT_TEXT_EMAIL)
				.setFunction1TextResId(R.string.function_sent_text_email)
				.setFunction2ClickId(ItemListFunction.FUNCTION_SENT_PICTURE_EMAIL)
				.setFunction2TextResId(R.string.function_sent_picture_email).build());
		// add weather status
		adapter.add(new ItemListFunction.Builder(getBaseActivity()).setType(ItemListFunction.TYPE_FUNCTION)
				.setIconResId(R.drawable.img_icon_weather).setDescResId(R.string.function_desc_getting_data)
				.setItemClickId(ItemListFunction.FUNCTION_CHECK_WEATHER).build());

	}

	public void changeSmsItem(int count) {
		if (adapter.getCount() <= 0) return;
		ItemListFunction item = adapter.getItem(0);
		item.notifyCount = count;
		if (item.notifyCount > 0) {
			item.desc = getString(R.string.function_desc_unread_sms, item.notifyCount);
		} else {
			item.desc = getString(R.string.function_desc_no_sms);
		}
		adapter.notifyDataSetChanged();
	}

	public void changeEmailLoading() {
		if (adapter.getCount() <= 1) return;
		ItemListFunction item = adapter.getItem(1);
		item.notifyCount = 0;
		item.desc = getString(R.string.function_desc_getting_data);
		adapter.notifyDataSetChanged();
	}

	public void changeEmailItem(int count, boolean readFail) {
		if (adapter.getCount() <= 1) return;
		ItemListFunction item = adapter.getItem(1);
		if (!readFail) {
			item.notifyCount = count;
			if (item.notifyCount > 0) {
				item.desc = getString(R.string.function_desc_unread_email, item.notifyCount);
			} else {
				item.desc = getString(R.string.function_desc_no_email);
			}
			item.itemClickId = ItemListFunction.FUNCTION_READ_EMAIL;
			item.function1TextResId = R.string.function_sent_text_email;
			item.function1ClickId = ItemListFunction.FUNCTION_SENT_TEXT_EMAIL;
			item.function2TextResId = R.string.function_sent_picture_email;
			item.function2ClickId = ItemListFunction.FUNCTION_SENT_PICTURE_EMAIL;
		} else {
			item.notifyCount = 0;
			item.desc = getString(R.string.function_desc_read_email_fail);
			item.itemClickId = ItemListFunction.FUNCTION_SETUP_EMAIL_ACCOUNT;
			item.function1TextResId = R.string.function_setup_email_account;
			item.function1ClickId = ItemListFunction.FUNCTION_SETUP_EMAIL_ACCOUNT;
			item.function2TextResId = 0;
			item.function2ClickId = 0;
		}
		adapter.notifyDataSetChanged();
	}

	private void initWeahter() {
		mLocationUtils = new LocationUtils();
		WeatherResource currentWeather = mSharePrefs.getCurrentWeather();
		showWeather(currentWeather);
		requestLocation();
	}

	private void showWeather(WeatherResource response) {
		String location = "";
		if (!TextUtils.isEmpty(response.cityName) || !TextUtils.isEmpty(response.country())) {
			location = response.cityName + ", " + response.country();
		}
		String condition = response.condition.get(0).main + "(" + response.condition.get(0).description + ")";
		String degress = getString(R.string.common_degress);
		String tempp = Math.round(response.temperature.temp) + " " + degress + "C";

		ItemListFunction item = adapter.getItem(2);
		item.notifyCount = Math.round(response.temperature.temp);
		item.desc = location + "\n" + condition + ", " + tempp;
		item.iconUrl = URLConsts.WEATHER_ICON_URL.replace(Consts.HOLDER_ID_PARAM, response.condition.get(0).icon);
		adapter.notifyDataSetChanged();
	}

	private void requestLocation() {
		mLastLocation = mLocationUtils.getLocation(getBaseActivity(), mLocationRecall, false);
		if (mLastLocation != null) {
			requestWeather(mLastLocation);
		}
	}

	private void requestWeather(Location location) {
		Bundle bundle = new Bundle();
		bundle.putString(Consts.LON_PARAM, location.getLongitude() + "");
		bundle.putString(Consts.LAT_PARAM, location.getLatitude() + "");
		mRequestManager.request(Actions.ACTION_GET_WEATHER, bundle, mRequestWeatherListener, null);
	}

	private void requestListEvent() {
		Bundle bundle = new Bundle();
		// show action bar progress loading
		getBaseActivity().showActionBarProgressBar();
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		bundle.putLong(Consts.PARAMS_QUERY_START, calendar.getTime().getTime());
		// next 7 days
		calendar.add(Calendar.DATE, 7);
		bundle.putLong(Consts.PARAMS_QUERY_END, calendar.getTime().getTime());
		mRequestManager.request(Actions.ACTION_GET_LIST_EVENT, bundle, mRequestEventListener, mErrorListener);
	}

	/**
	 * @author cnc Adapter for list main
	 */
	public class MainAdapter extends ArrayAdapter<ItemListFunction> implements OnClickListener {
		private LayoutInflater mInfalter;
		private int[] eventColor = new int[10];

		public MainAdapter(Context context, ArrayList<ItemListFunction> list) {
			super(context, 0, list);
			mInfalter = LayoutInflater.from(context);
			Resources res = context.getResources();
			eventColor[0] = res.getColor(R.color.event1_color);
			eventColor[1] = res.getColor(R.color.event2_color);
			eventColor[2] = res.getColor(R.color.event3_color);
			eventColor[3] = res.getColor(R.color.event4_color);
			eventColor[4] = res.getColor(R.color.event5_color);
			eventColor[5] = res.getColor(R.color.event6_color);
			eventColor[6] = res.getColor(R.color.event7_color);
			eventColor[7] = res.getColor(R.color.event8_color);
			eventColor[8] = res.getColor(R.color.event9_color);
			eventColor[9] = res.getColor(R.color.event10_color);
		}

		@Override
		public int getViewTypeCount() {
			return ItemListFunction.TYPE_COUNT;
		}

		@Override
		public int getItemViewType(int position) {
			return getItem(position).type;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ItemListFunction item = getItem(position);
			ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				if (getItemViewType(position) == ItemListFunction.TYPE_FUNCTION) {
					convertView = mInfalter.inflate(R.layout.item_list_main_function, parent, false);
					holder.tvFunction1 = (TextView) convertView.findViewById(R.id.tvFunc1);
					holder.tvFunction2 = (TextView) convertView.findViewById(R.id.tvFunc2);
					holder.tvDesc = (TextView) convertView.findViewById(R.id.tvDesc);
					holder.tvNotifyCount = (TextView) convertView.findViewById(R.id.tvNotifyCount);
					holder.imgIcon = (ImageView) convertView.findViewById(R.id.imgIcon);
					holder.tvFunction1.setOnClickListener(this);
					holder.tvFunction2.setOnClickListener(this);
				} else {
					convertView = mInfalter.inflate(R.layout.item_list_main_event, parent, false);
					holder.imgIcon = (ImageView) convertView.findViewById(R.id.imgIcon);
					holder.tvDesc = (TextView) convertView.findViewById(R.id.tvDesc);
					holder.tvTimer = (TextView) convertView.findViewById(R.id.tvTimer);
					holder.tvDate = (TextView) convertView.findViewById(R.id.tvDate);
					holder.line = convertView.findViewById(R.id.viewLine);
				}
				convertView.setOnClickListener(this);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.item = item;
			holder.tvDesc.setText(item.desc);
			if (getItemViewType(position) == ItemListFunction.TYPE_FUNCTION) {
				if (item.function1TextResId != 0) {
					holder.tvFunction1.setText(item.function1TextResId);
					holder.tvFunction1.setVisibility(View.VISIBLE);
					holder.tvFunction1.setTag(holder);
				} else {
					holder.tvFunction1.setVisibility(View.GONE);
				}
				if (item.function2TextResId != 0) {
					holder.tvFunction2.setText(item.function2TextResId);
					holder.tvFunction2.setVisibility(View.VISIBLE);
					holder.tvFunction2.setTag(holder);
				} else {
					holder.tvFunction2.setVisibility(View.GONE);
				}
				if (item.iconResId != 0) {
					holder.imgIcon.setImageResource(item.iconResId);
					holder.imgIcon.setVisibility(View.VISIBLE);
				} else {
					holder.imgIcon.setVisibility(View.INVISIBLE);
				}
				if (!TextUtils.isEmpty(item.iconUrl)) {
					mImageLoader.displayImage(item.iconUrl, holder.imgIcon, imageDisplayOptions);
					holder.imgIcon.setVisibility(View.VISIBLE);
				}
				if (item.notifyCount > 0) {
					holder.tvNotifyCount.setText(item.notifyCount + "");
					holder.tvNotifyCount.setVisibility(View.VISIBLE);
				} else {
					holder.tvNotifyCount.setVisibility(View.GONE);
				}
			} else {
				String time = timeFormater.format(item.startTime) + " - " + timeFormater.format(item.endTime);
				String date = dateFormater.format(item.startTime);
				if (item.eventColorId == 0) {
					holder.tvDate.setVisibility(View.VISIBLE);
					holder.tvDate.setText(date);
					holder.line.setVisibility(View.GONE);
				} else {
					holder.tvDate.setVisibility(View.GONE);
					holder.line.setVisibility(View.VISIBLE);
				}
				if (item.eventColorId > 9) {
					holder.imgIcon.setBackgroundColor(eventColor[0]);
				} else {
					holder.imgIcon.setBackgroundColor(eventColor[item.eventColorId]);
				}
				holder.tvTimer.setText(time);
			}
			convertView.setTag(holder);
			return convertView;
		}

		public class ViewHolder {
			TextView tvFunction1, tvFunction2, tvDesc, tvNotifyCount, tvTimer, tvDate;
			ImageView imgIcon;
			View line;
			ItemListFunction item;
		}

		@Override
		public void onClick(View v) {
			// first, stop speech
			getTextToSpeech().stop();
			ViewHolder holder = (ViewHolder) v.getTag();
			switch (v.getId()) {
				case R.id.rlItemList:
					doFunction(holder.item.itemClickId);
					break;
				case R.id.tvFunc1:
					doFunction(holder.item.function1ClickId);
					break;
				case R.id.tvFunc2:
					doFunction(holder.item.function2ClickId);
					break;

			}
		}

		private void doFunction(int funcId) {
			switch (funcId) {
				case ItemListFunction.FUNCTION_READ_SMS:
					readSmsTask.speakSmsDetail();
					break;
				case ItemListFunction.FUNCTION_READ_EMAIL:
					startActivity(new Intent(getBaseActivity(), ReadEmailActivity.class));
					break;
				case ItemListFunction.FUNCTION_SENT_TEXT_SMS:
					((MainActivity) getBaseActivity()).listen(MainActivity.REC_SMS);
					break;
				case ItemListFunction.FUNCTION_SENT_TEXT_EMAIL:
					((MainActivity) getBaseActivity()).listen(MainActivity.REC_EMAIL);
					break;
				case ItemListFunction.FUNCTION_CHECK_WEATHER:
					startActivity(new Intent(getContext(), WeatherActivity.class));
					break;
				case ItemListFunction.FUNCTION_CHECK_NEWS:
					startActivity(new Intent(getContext(), RssActivity.class));
					break;
				case ItemListFunction.FUNCTION_SEARCH:
					((MainActivity) getBaseActivity()).listen(MainActivity.REC_SEARCH);
					break;
				case ItemListFunction.FUNCTION_SPEAK_TIME:
					if (getTextToSpeech() != null) {
						getTextToSpeech().speak(AppUtils.getCurrentTimeForSpeech(getContext()),
								TextToSpeech.QUEUE_FLUSH, null);
					}
					break;
				case ItemListFunction.FUNCTION_CHECK_MY_CALENDAR:
					startActivity(new Intent(getContext(), CalendarEventFragment.class));
					break;
				case ItemListFunction.FUNCTION_SETUP_EMAIL_ACCOUNT:
					startActivityForResult(new Intent(getContext(), EmailSetupActivity.class),
							RequestCode.REQUEST_EMAIL_SETUP);
					break;
			}
		}
	}

	protected void initCursorLoader() {
		getContentResolver().delete(TableEvent.CONTENT_URI, null, null);
		getLoaderManager().initLoader(LOADER_GET_LIST_EVENT, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
		switch (id) {
			case LOADER_GET_LIST_EVENT:
				Calendar calendar = Calendar.getInstance();
				calendar.set(Calendar.HOUR_OF_DAY, 0);
				calendar.set(Calendar.MINUTE, 0);
				calendar.set(Calendar.SECOND, 0);
				String start = DateTimeFormater.compareFormater.format(calendar.getTime());
				// next 3 days
				calendar.add(Calendar.DATE, 7);
				String end = DateTimeFormater.compareFormater.format(calendar.getTime());
				String where = TableEvent.START + ">=datetime('" + start + "') AND " + TableEvent.START
						+ "<=datetime('" + end + "')";
				return new CursorLoader(getBaseActivity(), TableEvent.CONTENT_URI, null, where, null, TableEvent.START
						+ " asc");
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		int id = loader.getId();
		switch (id) {
			case LOADER_GET_LIST_EVENT:
				// clear all item from 3st
				for (int i = adapter.getCount() - 1; i >= FUNCTION_ITEM_COUNT; i--) {
					adapter.remove(adapter.getItem(i));
				}
				int pos = 0;
				if (cursor != null && cursor.moveToFirst()) {
					do {
						EventResource event = new EventResource(cursor);
						if (event.start() != null || event.end() != null) {
							ItemListFunction item = new ItemListFunction.Builder(getBaseActivity())
									.setType(ItemListFunction.TYPE_EVENT).setDesc(event.title)
									.setStartTime(event.start()).setEndTime(event.end()).build();
							String date = dateFormater.format(event.start());
							Logger.info("zzz", "date: " + date + event.title);
							if (pos == 0) {
								item.eventColorId = 0;
							} else {
								ItemListFunction previousItem = adapter.getItem(FUNCTION_ITEM_COUNT + pos - 1);
								String previousDate = dateFormater.format(previousItem.startTime);
								if (date.equals(previousDate)) {
									item.eventColorId = previousItem.eventColorId + 1;;
								} else {
									item.eventColorId = 0;
								}
							}
							pos++;
							adapter.add(item);
						}
					} while (cursor.moveToNext());
					adapter.notifyDataSetChanged();
				}
				break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}
}
