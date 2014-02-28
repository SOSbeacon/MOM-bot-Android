package org.cnc.msrobot.fragment;

import java.util.ArrayList;

import org.cnc.msrobot.R;
import org.cnc.msrobot.activity.CalendarEventActivity;
import org.cnc.msrobot.activity.RssActivity;
import org.cnc.msrobot.activity.WeatherActivity;
import org.cnc.msrobot.resource.ItemListFunction;
import org.cnc.msrobot.resource.WeatherResource;
import org.cnc.msrobot.task.ReadEmailTask;
import org.cnc.msrobot.task.ReadSMSTask;
import org.cnc.msrobot.utils.Actions;
import org.cnc.msrobot.utils.AppUtils;
import org.cnc.msrobot.utils.Consts;
import org.cnc.msrobot.utils.Consts.URLConsts;
import org.cnc.msrobot.utils.LocationUtils;
import org.cnc.msrobot.utils.LocationUtils.LocationUtilsListener;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response.Listener;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

public class HomeFragment extends BaseFragment {
	private static final int REC_SMS = 0;
	private static final int REC_EMAIL = 1;
	private static final int REC_SEARCH = 2;
	/**
	 * regconize index
	 */
	private int mRecIndex = 0;

	private View mLayout;
	private MainAdapter adapter;
	private ReadSMSTask readSmsTask;
	private ReadEmailTask readEmailTask;
	private LocationUtils mLocationUtils;
	private Location mLastLocation;

	private ImageLoader mImageLoader = ImageLoader.getInstance();
	private static final DisplayImageOptions imageDisplayOptions = new DisplayImageOptions.Builder()
			.bitmapConfig(Bitmap.Config.RGB_565).imageScaleType(ImageScaleType.IN_SAMPLE_INT)
			.resetViewBeforeLoading(true).cacheInMemory(true).cacheOnDisc(true).build();

	private Listener<WeatherResource> mRequestWeatherListener = new Listener<WeatherResource>() {
		@Override
		public void onResponse(WeatherResource response) {
			showWeather(response);
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
		return mLayout;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initFunctionList();

		// init task
		readSmsTask = new ReadSMSTask(this);
		readSmsTask.execute();
		readEmailTask = new ReadEmailTask(this);
		readEmailTask.execute("thanhle@cnc.vn", "28/20hoangdieu");

		initWeahter();
	}

	private void initFunctionList() {
		ListView lv = (ListView) mLayout.findViewById(R.id.lvMain);
		adapter = new MainAdapter(getBaseActivity(), new ArrayList<ItemListFunction>());
		// add sms status
		adapter.add(new ItemListFunction.Builder(getBaseActivity()).setIconResId(R.drawable.img_icon_sms)
				.setDescResId(R.string.function_desc_getting_data).setItemClickId(ItemListFunction.FUNCTION_READ_SMS)
				.setFunction1ClickId(ItemListFunction.FUNCTION_SENT_TEXT_SMS)
				.setFunction1TextResId(R.string.function_sent_text_sms)
				.setFunction2ClickId(ItemListFunction.FUNCTION_SENT_PICTURE_MMS)
				.setFunction2TextResId(R.string.function_sent_picture_mms).build());
		// add email status
		adapter.add(new ItemListFunction.Builder(getBaseActivity()).setIconResId(R.drawable.img_icon_email)
				.setDescResId(R.string.function_desc_getting_data).setItemClickId(ItemListFunction.FUNCTION_READ_EMAIL)
				.setFunction1ClickId(ItemListFunction.FUNCTION_SENT_TEXT_EMAIL)
				.setFunction1TextResId(R.string.function_sent_text_email)
				.setFunction2ClickId(ItemListFunction.FUNCTION_SENT_PICTURE_EMAIL)
				.setFunction2TextResId(R.string.function_sent_picture_email).build());
		// add weather status
		adapter.add(new ItemListFunction.Builder(getBaseActivity()).setIconResId(R.drawable.img_icon_weather)
				.setDescResId(R.string.function_desc_getting_data)
				.setItemClickId(ItemListFunction.FUNCTION_CHECK_WEATHER).build());
		lv.setAdapter(adapter);

	}

	public void changeSmsItem(int count) {
		ItemListFunction item = adapter.getItem(0);
		item.notifyCount = count;
		if (item.notifyCount > 0) {
			item.desc = getString(R.string.function_desc_unread_sms, item.notifyCount);
		} else {
			item.desc = getString(R.string.function_desc_no_sms);
		}
		adapter.notifyDataSetChanged();
	}

	public void changeEmailItem(int count) {
		ItemListFunction item = adapter.getItem(1);
		item.notifyCount = count;
		if (item.notifyCount > 0) {
			item.desc = getString(R.string.function_desc_unread_email, item.notifyCount);
		} else {
			item.desc = getString(R.string.function_desc_no_email);
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
		String location = response.cityName + ", " + response.country();
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

	@Override
	public void onRecognize(final ArrayList<String> data) {
		if (data == null || data.size() == 0) return;
		switch (mRecIndex) {
			case REC_SMS:
				if (data.size() == 1) {
					AppUtils.showSentSmsIntent(getBaseActivity(), data.get(0));
				} else {
					mDialog.showSelectionDialog(R.string.dialog_choose_message_title,
							data.toArray(new String[data.size()]), new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									AppUtils.showSentSmsIntent(getBaseActivity(), data.get(which));
								}
							});
				}
				break;
			case REC_EMAIL:
				if (data.size() == 1) {
					AppUtils.showSentEmailIntent(getBaseActivity(), "", data.get(0));
				} else {
					mDialog.showSelectionDialog(R.string.dialog_choose_message_title,
							data.toArray(new String[data.size()]), new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									AppUtils.showSentEmailIntent(getBaseActivity(), "", data.get(which));
								}
							});
				}
				break;
			case REC_SEARCH:
				if (data.size() == 1) {
					AppUtils.showGoogleSearchIntent(getBaseActivity(), data.get(0));
				} else {
					mDialog.showSelectionDialog(R.string.dialog_choose_search_title,
							data.toArray(new String[data.size()]), new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									AppUtils.showGoogleSearchIntent(getBaseActivity(), data.get(which));
								}
							});
				}
		}
	}

	/**
	 * @author cnc Adapter for list main
	 */
	public class MainAdapter extends ArrayAdapter<ItemListFunction> implements OnClickListener {
		private LayoutInflater mInfalter;

		public MainAdapter(Context context, ArrayList<ItemListFunction> list) {
			super(context, 0, list);
			mInfalter = LayoutInflater.from(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ItemListFunction item = getItem(position);
			ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInfalter.inflate(R.layout.item_list_main, parent, false);
				holder.tvFunction1 = (TextView) convertView.findViewById(R.id.tvFunc1);
				holder.tvFunction2 = (TextView) convertView.findViewById(R.id.tvFunc2);
				holder.tvDesc = (TextView) convertView.findViewById(R.id.tvDesc);
				holder.tvNotifyCount = (TextView) convertView.findViewById(R.id.tvNotifyCount);
				holder.imgIcon = (ImageView) convertView.findViewById(R.id.imgIcon);
				holder.tvFunction1.setOnClickListener(this);
				holder.tvFunction2.setOnClickListener(this);
				convertView.setOnClickListener(this);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.item = item;
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
			holder.tvDesc.setText(item.desc);
			convertView.setTag(holder);
			return convertView;
		}

		public class ViewHolder {
			TextView tvFunction1, tvFunction2, tvDesc, tvNotifyCount;
			ImageView imgIcon;
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
					readEmailTask.speakEmailDetail();
					break;
				case ItemListFunction.FUNCTION_SENT_TEXT_SMS:
					mRecIndex = REC_SMS;
					listen();
					break;
				case ItemListFunction.FUNCTION_SENT_TEXT_EMAIL:
					mRecIndex = REC_EMAIL;
					listen();
					break;
				case ItemListFunction.FUNCTION_CHECK_WEATHER:
					startActivity(new Intent(getContext(), WeatherActivity.class));
					break;
				case ItemListFunction.FUNCTION_CHECK_NEWS:
					startActivity(new Intent(getContext(), RssActivity.class));
					break;
				case ItemListFunction.FUNCTION_SEARCH:
					mRecIndex = REC_SEARCH;
					listen();
					break;
				case ItemListFunction.FUNCTION_SPEAK_TIME:
					if (getTextToSpeech() != null) {
						getTextToSpeech().speak(AppUtils.getCurrentTimeForSpeech(getContext()),
								TextToSpeech.QUEUE_FLUSH, null);
					}
					break;
				case ItemListFunction.FUNCTION_CHECK_MY_CALENDAR:
					startActivity(new Intent(getContext(), CalendarEventActivity.class));
					break;
			}
		}
	}
}
