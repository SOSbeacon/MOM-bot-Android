package org.cnc.msrobot.fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import org.cnc.msrobot.R;
import org.cnc.msrobot.activity.AddOrEditEventActivity;
import org.cnc.msrobot.activity.BaseActivity.SpeakAnimationListener;
import org.cnc.msrobot.activity.EmailSetupActivity;
import org.cnc.msrobot.activity.MainActivity;
import org.cnc.msrobot.adapter.ChatAdapter;
import org.cnc.msrobot.adapter.MainAdapter;
import org.cnc.msrobot.adapter.MainAdapter.OnFunctionDoListener;
import org.cnc.msrobot.provider.DbContract.TableEvent;
import org.cnc.msrobot.recognizemodule.RecoginizeIds;
import org.cnc.msrobot.resource.EventResource;
import org.cnc.msrobot.resource.ItemListFunction;
import org.cnc.msrobot.resource.TaskTime;
import org.cnc.msrobot.resource.WeatherResource;
import org.cnc.msrobot.task.ReadEmailTask;
import org.cnc.msrobot.task.ReadSMSTask;
import org.cnc.msrobot.utils.Actions;
import org.cnc.msrobot.utils.AppUtils;
import org.cnc.msrobot.utils.Consts;
import org.cnc.msrobot.utils.Consts.RequestCode;
import org.cnc.msrobot.utils.Consts.URLConsts;
import org.cnc.msrobot.utils.CustomActionBar;
import org.cnc.msrobot.utils.DateTimeFormater;
import org.cnc.msrobot.utils.LocationUtils;
import org.cnc.msrobot.utils.LocationUtils.LocationUtilsListener;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TimePicker;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

public class HomeFragment extends BaseFragment implements LoaderCallbacks<Cursor>, SpeakAnimationListener,
		OnFunctionDoListener {

	private static final int DELAY_SPEAKING_ANIMATION = 300;
	private static final int LOADER_GET_LIST_EVENT = 1;
	private static final int FUNCTION_ITEM_COUNT = 4;
	private View mLayout;
	private MainAdapter adapterMain;
	private ChatAdapter adapterChat;
	private ReadSMSTask readSmsTask;
	private ReadEmailTask readEmailTask;
	private LocationUtils mLocationUtils;
	private Location mLastLocation;
	private ImageView mImgSecretary;
	private boolean readFail;
	private int mMenuIndex = 0;
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
			if (getBaseActivity() != null) {
				getBaseActivity().hideActionBarProgressBar();
			}
		}
	};
	private ErrorListener mErrorListener = new ErrorListener() {

		@Override
		public void onErrorResponse(VolleyError error) {
			// hide action bar progress loading
			if (getBaseActivity() != null) {
				getBaseActivity().hideActionBarProgressBar();
			}
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

		ListView lvChat = (ListView) mLayout.findViewById(R.id.lvChat);
		lvChat.setAdapter(adapterChat);

		ListView lv = (ListView) mLayout.findViewById(R.id.lvMain);
		lv.setAdapter(adapterMain);

		mImgSecretary = (ImageView) mLayout.findViewById(R.id.imgSecretary);
		mImgSecretary.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				adapterChat.clear();
				adapterChat.add(getString(R.string.command_example));
				((MainActivity) getBaseActivity()).doRecognizeModule(RecoginizeIds.MODULE_COMMAND);
			}
		});

		// init task read sms
		readSmsTask = new ReadSMSTask((MainActivity) getBaseActivity());
		readSmsTask.execute();
		// init task read email
		String user = mSharePrefs.getGmailUsername();
		String pass = mSharePrefs.getGmailPass();
		if (!TextUtils.isEmpty(user) && !TextUtils.isEmpty(pass)) {
			readEmailTask = new ReadEmailTask((MainActivity) getBaseActivity());
			readEmailTask.execute(user, pass);
		} else {
			changeEmailItem(0, true);
		}
		return mLayout;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// change type of Action bar
		getBaseActivity().getCusomActionBar().setType(CustomActionBar.TYPE_HOME);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// init adapter
		adapterChat = new ChatAdapter(getBaseActivity(), new ArrayList<String>());
		adapterMain = new MainAdapter(getBaseActivity(), new ArrayList<ItemListFunction>(), this);
		// init & request weather
		initWeahter();
		// show main menu
		showMainMenu();
		// init cursor loader
		initCursorLoader();
		// request list event from server
		requestListEvent();
		setOnSpeakAnimationListener(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		readSmsTask.cancel(true);
		if (readEmailTask != null) {
			readEmailTask.cancel(true);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		adapterChat.clear();
	}

	@Override
	public void onDetach() {
		super.onDetach();
		readSmsTask.cancel(true);
		if (readEmailTask != null) {
			readEmailTask.cancel(true);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == RequestCode.REQUEST_EMAIL_SETUP && resultCode == Activity.RESULT_OK) {
			String user = data.getStringExtra(EmailSetupActivity.EXTRA_EMAIL);
			String pass = data.getStringExtra(EmailSetupActivity.EXTRA_PASS);
			readEmailTask = new ReadEmailTask((MainActivity) getBaseActivity());
			readEmailTask.execute(user, pass);
		}
	}

	public void changeSmsItem(int count) {
		ItemListFunction item = adapterMain.getItem(0);
		if (item.itemClickId != ItemListFunction.FUNCTION_READ_SMS) return;
		item.notifyCount = count;
		if (item.notifyCount > 0) {
			item.desc = getString(R.string.function_desc_unread_sms, item.notifyCount);
		} else {
			item.desc = getString(R.string.function_desc_no_sms);
		}
		adapterMain.notifyDataSetChanged();
	}

	public void changeEmailLoading() {
		if (adapterMain.getCount() <= 1) return;
		ItemListFunction item = adapterMain.getItem(1);
		if (item.itemClickId != ItemListFunction.FUNCTION_READ_EMAIL
				&& item.itemClickId != ItemListFunction.FUNCTION_SETUP_EMAIL_ACCOUNT) return;
		item.notifyCount = 0;
		item.desc = getString(R.string.function_desc_getting_data);
		adapterMain.notifyDataSetChanged();
	}

	public void changeEmailItem(int count, boolean readFail) {
		this.readFail = readFail;
		if (adapterMain.getCount() <= 1) return;
		ItemListFunction item = adapterMain.getItem(1);
		if (item.itemClickId != ItemListFunction.FUNCTION_READ_EMAIL
				&& item.itemClickId != ItemListFunction.FUNCTION_SETUP_EMAIL_ACCOUNT) return;
		if (!readFail) {
			item.notifyCount = count;
			if (item.notifyCount > 0) {
				item.desc = getString(R.string.function_desc_unread_email, item.notifyCount);
			} else {
				item.desc = getString(R.string.function_desc_no_email);
			}
			item.itemClickId = ItemListFunction.FUNCTION_READ_EMAIL;
		} else {
			item.notifyCount = 0;
			item.desc = getString(R.string.function_desc_read_email_fail);
			item.itemClickId = ItemListFunction.FUNCTION_SETUP_EMAIL_ACCOUNT;
		}
		adapterMain.notifyDataSetChanged();
	}

	private void initWeahter() {
		mLocationUtils = new LocationUtils();
		requestLocation();
	}

	private void showWeather(WeatherResource response) {
		String location = "";
		if (getBaseActivity() == null) return;
		ItemListFunction item = adapterMain.getItem(2);
		if (item.itemClickId != ItemListFunction.FUNCTION_CHECK_WEATHER) return;
		if (!TextUtils.isEmpty(response.cityName) || !TextUtils.isEmpty(response.country())) {
			location = response.cityName + ", " + response.country() + "\n";
		}
		String condition = response.condition.get(0).main;
		String degress = getString(R.string.common_degress);
		String tempp = Math.round(response.temperature.temp) + " " + degress + "C";

		item.notifyCount = Math.round(response.temperature.temp);
		item.desc = location + condition + ", " + tempp;
		item.iconUrl = URLConsts.WEATHER_ICON_URL.replace(Consts.HOLDER_ID_PARAM, response.condition.get(0).icon);
		adapterMain.notifyDataSetChanged();
	}

	private void requestLocation() {
		mLastLocation = mLocationUtils.getLocation(getBaseActivity(), mLocationRecall, false);
		if (mLastLocation != null) {
			requestWeather(mLastLocation);
		}
	}

	private void requestWeather(Location location) {
		Bundle bundle = new Bundle();
		bundle.putString(Consts.PARAMS_LON, location.getLongitude() + "");
		bundle.putString(Consts.PARAMS_LAT, location.getLatitude() + "");
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
	 * return true for success
	 * 
	 * @return
	 */
	public boolean checkBackPress() {
		if (mMenuIndex == 0) {
			return true;
		} else if (mMenuIndex == 1) {
			showMainMenu();
		} else if (mMenuIndex >= 2 && mMenuIndex <= 4) {
			showCommandMenu();
		}
		return false;
	}

	private void showMainMenu() {
		mMenuIndex = 0;
		adapterMain.clear();
		// add sms status
		adapterMain.add(new ItemListFunction.Builder(getBaseActivity()).setType(ItemListFunction.TYPE_FUNCTION)
				.setIconResId(R.drawable.img_icon_sms).setDescResId(R.string.function_desc_getting_data)
				.setItemClickId(ItemListFunction.FUNCTION_READ_SMS).setColorResId(R.color.event1_color).build());
		// add email status
		adapterMain.add(new ItemListFunction.Builder(getBaseActivity()).setType(ItemListFunction.TYPE_FUNCTION)
				.setIconResId(R.drawable.img_icon_email).setDescResId(R.string.function_desc_getting_data)
				.setItemClickId(ItemListFunction.FUNCTION_READ_EMAIL).setColorResId(R.color.event2_color).build());
		// add weather status
		adapterMain.add(new ItemListFunction.Builder(getBaseActivity()).setType(ItemListFunction.TYPE_FUNCTION)
				.setIconResId(R.drawable.img_icon_weather).setDescResId(R.string.function_desc_getting_data)
				.setItemClickId(ItemListFunction.FUNCTION_CHECK_WEATHER).setColorResId(R.color.event3_color).build());
		// add command group
		adapterMain.add(new ItemListFunction.Builder(getBaseActivity()).setType(ItemListFunction.TYPE_FUNCTION)
				.setIconResId(R.drawable.img_icon_command).setDescResId(R.string.function_desc_command)
				.setItemClickId(ItemListFunction.FUNCTION_COMMAND).setColorResId(R.color.black).build());
		if (ReadSMSTask.mListSMS != null) {
			changeSmsItem(ReadSMSTask.mListSMS.size());
		} else {
			changeSmsItem(0);
		}
		if (ReadEmailTask.emails != null) {
			changeEmailItem(ReadEmailTask.emails.size(), readFail);
		} else {
			changeEmailItem(0, readFail);
		}
		WeatherResource currentWeather = mSharePrefs.getCurrentWeather();
		showWeather(currentWeather);
	}

	private void showCommandMenu() {
		mMenuIndex = 1;
		adapterMain.clear();
		// add back item
		adapterMain.add(new ItemListFunction.Builder(getBaseActivity()).setType(ItemListFunction.TYPE_FUNCTION)
				.setDescResId(R.string.function_desc_back).setItemClickId(ItemListFunction.FUNCTION_BACK_TO_MAIN)
				.setColorResId(R.color.black).build());
		// add communication item
		adapterMain.add(new ItemListFunction.Builder(getBaseActivity()).setType(ItemListFunction.TYPE_FUNCTION)
				.setDescResId(R.string.function_desc_communication)
				.setItemClickId(ItemListFunction.FUNCTION_GROUP_COMUNICATION).setColorResId(R.color.black).build());
		// add information item
		adapterMain.add(new ItemListFunction.Builder(getBaseActivity()).setType(ItemListFunction.TYPE_FUNCTION)
				.setDescResId(R.string.function_desc_information)
				.setItemClickId(ItemListFunction.FUNCTION_GROUP_INFORMATION).setColorResId(R.color.black).build());
		// add emergency item
		adapterMain.add(new ItemListFunction.Builder(getBaseActivity()).setType(ItemListFunction.TYPE_FUNCTION)
				.setDescResId(R.string.function_desc_emergency)
				.setItemClickId(ItemListFunction.FUNCTION_GROUP_EMERGENCY).setColorResId(R.color.black).build());
		// add administration
		adapterMain.add(new ItemListFunction.Builder(getBaseActivity()).setType(ItemListFunction.TYPE_FUNCTION)
				.setDescResId(R.string.function_desc_admin).setItemClickId(ItemListFunction.FUNCTION_GROUP_ADMIN)
				.setColorResId(R.color.black).build());
		adapterMain.notifyDataSetChanged();
	}

	private void showCommunicationMenu() {
		mMenuIndex = 2;
		adapterMain.clear();
		// add back item
		adapterMain.add(new ItemListFunction.Builder(getBaseActivity()).setType(ItemListFunction.TYPE_FUNCTION)
				.setDescResId(R.string.function_desc_back).setItemClickId(ItemListFunction.FUNCTION_BACK_TO_COMMAND)
				.setColorResId(R.color.black).build());
		// add sent text message
		adapterMain.add(new ItemListFunction.Builder(getBaseActivity()).setType(ItemListFunction.TYPE_FUNCTION)
				.setDescResId(R.string.function_sent_text_sms).setItemClickId(ItemListFunction.FUNCTION_SENT_TEXT_SMS)
				.setColorResId(R.color.black).build());
		// add image message
		adapterMain.add(new ItemListFunction.Builder(getBaseActivity()).setType(ItemListFunction.TYPE_FUNCTION)
				.setDescResId(R.string.function_sent_picture_mms)
				.setItemClickId(ItemListFunction.FUNCTION_SENT_PICTURE_MMS).setColorResId(R.color.black).build());
		// add information item
		adapterMain.add(new ItemListFunction.Builder(getBaseActivity()).setType(ItemListFunction.TYPE_FUNCTION)
				.setDescResId(R.string.function_sent_text_email)
				.setItemClickId(ItemListFunction.FUNCTION_SENT_TEXT_EMAIL).setColorResId(R.color.black).build());
		// add emergency item
		adapterMain.add(new ItemListFunction.Builder(getBaseActivity()).setType(ItemListFunction.TYPE_FUNCTION)
				.setDescResId(R.string.function_sent_picture_email)
				.setItemClickId(ItemListFunction.FUNCTION_SENT_PICTURE_EMAIL).setColorResId(R.color.black).build());
		adapterMain.notifyDataSetChanged();
	}

	private void showInformationMenu() {
		mMenuIndex = 3;
		adapterMain.clear();
		// add back item
		adapterMain.add(new ItemListFunction.Builder(getBaseActivity()).setType(ItemListFunction.TYPE_FUNCTION)
				.setDescResId(R.string.function_desc_back).setItemClickId(ItemListFunction.FUNCTION_BACK_TO_COMMAND)
				.setColorResId(R.color.black).build());
		// add sent text message
		adapterMain.add(new ItemListFunction.Builder(getBaseActivity()).setType(ItemListFunction.TYPE_FUNCTION)
				.setDescResId(R.string.function_desc_news).setItemClickId(ItemListFunction.FUNCTION_CHECK_NEWS)
				.setColorResId(R.color.black).build());
		// add image message
		adapterMain.add(new ItemListFunction.Builder(getBaseActivity()).setType(ItemListFunction.TYPE_FUNCTION)
				.setDescResId(R.string.function_desc_weather).setItemClickId(ItemListFunction.FUNCTION_CHECK_WEATHER)
				.setColorResId(R.color.black).build());
		adapterMain.notifyDataSetChanged();
	}

	private void showAdminMenu() {
		mMenuIndex = 4;
		adapterMain.clear();
		// add back item
		adapterMain.add(new ItemListFunction.Builder(getBaseActivity()).setType(ItemListFunction.TYPE_FUNCTION)
				.setDescResId(R.string.function_desc_back).setItemClickId(ItemListFunction.FUNCTION_BACK_TO_COMMAND)
				.setColorResId(R.color.black).build());
		// add sent text message
		adapterMain.add(new ItemListFunction.Builder(getBaseActivity()).setType(ItemListFunction.TYPE_FUNCTION)
				.setDescResId(R.string.function_desc_set_alarm).setItemClickId(ItemListFunction.FUNCTION_SET_ALARM)
				.setColorResId(R.color.black).build());
		// add image message
		adapterMain.add(new ItemListFunction.Builder(getBaseActivity()).setType(ItemListFunction.TYPE_FUNCTION)
				.setDescResId(R.string.function_desc_set_reminder)
				.setItemClickId(ItemListFunction.FUNCTION_SET_REMINDER).setColorResId(R.color.black).build());
		adapterMain.notifyDataSetChanged();
	}

	protected void initCursorLoader() {
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
				if (mMenuIndex > 0) return;
				for (int i = adapterMain.getCount() - 1; i >= FUNCTION_ITEM_COUNT; i--) {
					adapterMain.remove(adapterMain.getItem(i));
				}
				int pos = 0;
				if (cursor != null && cursor.moveToFirst()) {
					ArrayList<TaskTime> times = new ArrayList<TaskTime>();
					do {
						EventResource event = new EventResource(cursor);
						if (event.start() != null || event.end() != null) {
							String date = DateTimeFormater.dateFormater.format(event.start());
							if (pos == 0) {
								times.add(new TaskTime(event.start(), event.end(), event.title));
							} else {
								String previousDate = DateTimeFormater.dateFormater.format(times.get(0).start);
								if (date.equals(previousDate)) {
									times.add(new TaskTime(event.start(), event.end(), event.title));
								} else {
									ItemListFunction item = new ItemListFunction.Builder(getBaseActivity())
											.setType(ItemListFunction.TYPE_EVENT).setTimes(times).build();
									adapterMain.add(item);
									times = new ArrayList<TaskTime>();
									times.add(new TaskTime(event.start(), event.end(), event.title));
								}
							}
							pos++;
						}
					} while (cursor.moveToNext());
					adapterMain.notifyDataSetChanged();
				}
				break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}

	@Override
	public void refresh() {
		requestListEvent();
	}

	int resSpeakingId = R.drawable.img_secretary_close;
	final Handler handler = new Handler();
	final Random random = new Random();
	final Runnable mRunnableSpeaking = new Runnable() {

		@Override
		public void run() {
			if (resSpeakingId == R.drawable.img_secretary_close) {
				resSpeakingId = R.drawable.img_secretary_open;
			} else {
				resSpeakingId = R.drawable.img_secretary_close;
			}
			if (mImgSecretary != null) {
				mImgSecretary.setImageResource(resSpeakingId);
			}
			handler.postDelayed(this, (int) (DELAY_SPEAKING_ANIMATION * random.nextFloat()));
		}
	};
	final Runnable mRunnableStopSpeaking = new Runnable() {

		@Override
		public void run() {
			if (mImgSecretary != null) {
				mImgSecretary.setImageResource(R.drawable.img_secretary_close);
			}
		}
	};

	@Override
	public void startSpeakingAnimation() {
		handler.removeCallbacks(mRunnableSpeaking);
		handler.postDelayed(mRunnableSpeaking, (int) (DELAY_SPEAKING_ANIMATION * random.nextFloat()));
	}

	@Override
	public void stopSpeakingAnimation() {
		handler.removeCallbacks(mRunnableSpeaking);
		handler.post(mRunnableStopSpeaking);
	}

	public void addChatListView(String text, int pos) {
		if (adapterChat.getCount() <= pos) {
			adapterChat.add(text);
		} else {
			String old = adapterChat.getItem(pos);
			adapterChat.remove(old);
			adapterChat.insert(text, pos);
		}
		adapterChat.notifyDataSetChanged();
	}

	@Override
	public void doFunction(int id) {
		switch (id) {
			case ItemListFunction.FUNCTION_BACK_TO_MAIN:
				showMainMenu();
				break;
			case ItemListFunction.FUNCTION_BACK_TO_COMMAND:
			case ItemListFunction.FUNCTION_COMMAND:
				showCommandMenu();
				break;
			case ItemListFunction.FUNCTION_GROUP_COMUNICATION:
				showCommunicationMenu();
				break;
			case ItemListFunction.FUNCTION_GROUP_INFORMATION:
				showInformationMenu();
				break;
			case ItemListFunction.FUNCTION_GROUP_ADMIN:
				showAdminMenu();
				break;
			case ItemListFunction.FUNCTION_SET_ALARM:
				openTimePickerDialog(true);
				break;
			case ItemListFunction.FUNCTION_SET_REMINDER:
				getBaseActivity().startActivityForResult(new Intent(getBaseActivity(), AddOrEditEventActivity.class),
						RequestCode.REQUEST_ADD_OR_EDIT_EVENT);
				break;
		}
	}

	TimePickerDialog timePickerDialog;

	private void openTimePickerDialog(boolean is24r) {
		Calendar calendar = Calendar.getInstance();

		timePickerDialog = new TimePickerDialog(getBaseActivity(), onTimeSetListener,
				calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), is24r);
		timePickerDialog.setTitle("Set Alarm Time");

		timePickerDialog.show();

	}

	OnTimeSetListener onTimeSetListener = new OnTimeSetListener() {

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

			Calendar calNow = Calendar.getInstance();
			Calendar calSet = (Calendar) calNow.clone();

			calSet.set(Calendar.HOUR_OF_DAY, hourOfDay);
			calSet.set(Calendar.MINUTE, minute);
			calSet.set(Calendar.SECOND, 0);
			calSet.set(Calendar.MILLISECOND, 0);

			if (calSet.compareTo(calNow) <= 0) {
				// Today Set time passed, count to tomorrow
				calSet.add(Calendar.DATE, 1);
			}

			AppUtils.setAlarm(getBaseActivity(), calSet);
		}
	};
}
