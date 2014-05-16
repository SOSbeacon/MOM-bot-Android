package org.cnc.mombot.fragment;

import java.util.ArrayList;
import java.util.Calendar;

import org.cnc.mombot.R;
import org.cnc.mombot.activity.EmailSetupActivity;
import org.cnc.mombot.activity.MainActivity;
import org.cnc.mombot.adapter.MainAdapter;
import org.cnc.mombot.adapter.MainAdapter.OnFunctionDoListener;
import org.cnc.mombot.inputoutput.TextInput;
import org.cnc.mombot.inputoutput.VoiceToastOutput;
import org.cnc.mombot.module.Module;
import org.cnc.mombot.module.ModuleManager;
import org.cnc.mombot.resource.ItemListFunction;
import org.cnc.mombot.resource.WeatherResource;
import org.cnc.mombot.task.ReadEmailTask;
import org.cnc.mombot.task.ReadSMSTask;
import org.cnc.mombot.utils.Actions;
import org.cnc.mombot.utils.AppUtils;
import org.cnc.mombot.utils.Consts;
import org.cnc.mombot.utils.CustomActionBar;
import org.cnc.mombot.utils.LocationUtils;
import org.cnc.mombot.utils.Consts.RequestCode;
import org.cnc.mombot.utils.LocationUtils.LocationUtilsListener;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TimePicker;

import com.android.volley.Response.Listener;

public class ClassicFragment extends BaseFragment implements OnFunctionDoListener {
	private static final int DELAY_CHECK_SMS_EMAIL = 1000 * 60 * 5; // 5 minute
	private View mLayout;
	private MainAdapter adapterMain;
	private ReadSMSTask readSmsTask;
	private ReadEmailTask readEmailTask;
	private LocationUtils mLocationUtils;
	private Location mLastLocation;
	private boolean readFail;
	private int mMenuIndex = 0;
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
	private Handler handler = new Handler();
	private Runnable mCheckSmsEmailRunnable = new Runnable() {
		@Override
		public void run() {
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
			handler.postDelayed(this, DELAY_CHECK_SMS_EMAIL);
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mLayout = inflater.inflate(R.layout.fragment_classic, container, false);

		ListView lv = (ListView) mLayout.findViewById(R.id.lvMain);
		lv.setAdapter(adapterMain);

		return mLayout;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// change type of Action bar
		getBaseActivity().getCusomActionBar().setType(CustomActionBar.TYPE_CLASSIC);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// change input, output
		getBaseActivity().changeIO(new TextInput(getBaseActivity()), new VoiceToastOutput(getBaseActivity()));

		// init adapter
		adapterMain = new MainAdapter(getBaseActivity(), new ArrayList<ItemListFunction>(), this);
		// init & request weather
		initWeahter();
		// show main menu
		showCommandMenu();
	}

	@Override
	public void onStop() {
		super.onStop();
		if (readSmsTask != null) {
			readSmsTask.cancel(true);
		}
		if (readEmailTask != null) {
			readEmailTask.cancel(true);
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		if (readSmsTask != null) {
			readSmsTask.cancel(true);
		}
		if (readEmailTask != null) {
			readEmailTask.cancel(true);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		// update unread sms, email when activity resume
		int unread;
		if (ReadSMSTask.mListSMS != null) {
			unread = 0;
			for (int i = 0; i < ReadSMSTask.mListSMS.size(); i++) {
				if (ReadSMSTask.mListSMS.get(i).seen == 0) {
					unread++;
				}
			}
			changeSmsItem(unread);
		} else {
			changeSmsItem(0);
		}
		if (ReadEmailTask.emails != null) {
			unread = 0;
			for (int i = 0; i < ReadEmailTask.emails.size(); i++) {
				if (!ReadEmailTask.emails.get(i).seen) {
					unread++;
				}
			}
			changeEmailItem(unread, readFail);
		} else {
			changeEmailItem(0, readFail);
		}
		handler.post(mCheckSmsEmailRunnable);
	}

	@Override
	public void onPause() {
		super.onPause();
		handler.removeCallbacks(mCheckSmsEmailRunnable);
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
		if (mMenuIndex != 1) return; // not communication menu
		ItemListFunction item = adapterMain.getItem(2);
		if (item.itemClickId != ItemListFunction.FUNCTION_READ_SMS) return;
		item.notifyCount = count;
		if (item.notifyCount > 0) {
			item.desc = getString(R.string.function_desc_unread_sms, "");
		} else {
			item.desc = getString(R.string.function_desc_no_sms);
		}
		adapterMain.notifyDataSetChanged();
	}

	public void changeEmailLoading() {
		if (mMenuIndex != 1) return; // not communication menu
		if (adapterMain.getCount() <= 1) return;
		ItemListFunction item = adapterMain.getItem(3);
		if (item.itemClickId != ItemListFunction.FUNCTION_READ_EMAIL
				&& item.itemClickId != ItemListFunction.FUNCTION_SETUP_EMAIL_ACCOUNT) return;
		item.notifyCount = 0;
		item.desc = getString(R.string.function_desc_getting_data);
		adapterMain.notifyDataSetChanged();
	}

	public void changeEmailItem(int count, boolean readFail) {
		if (mMenuIndex != 1) return; // not communication menu
		this.readFail = readFail;
		if (adapterMain.getCount() <= 1) return;
		ItemListFunction item = adapterMain.getItem(3);
		if (item.itemClickId != ItemListFunction.FUNCTION_READ_EMAIL
				&& item.itemClickId != ItemListFunction.FUNCTION_SETUP_EMAIL_ACCOUNT) return;
		if (!readFail) {
			item.notifyCount = count;
			if (item.notifyCount > 0) {
				item.desc = getString(R.string.function_desc_unread_email, "");
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
		if (getBaseActivity() == null) return;
		CustomActionBar actionBar = getBaseActivity().getCusomActionBar();
		if (actionBar == null) return;
		String degress = getString(R.string.common_degress);
		String tempp = Math.round(response.temperature.temp) + degress + "C";
		actionBar.setWeatherText(tempp);
	}

	private void requestLocation() {
		mLastLocation = mLocationUtils.getLocation(getBaseActivity(), mLocationRecall, false);
		if (mLastLocation != null) {
			requestWeather(mLastLocation);
		}
	}

	private void requestWeather(Location location) {
		// save current location
		mSharePrefs.saveCurrentLocation(location);
		Bundle bundle = new Bundle();
		bundle.putString(Consts.PARAMS_LON, location.getLongitude() + "");
		bundle.putString(Consts.PARAMS_LAT, location.getLatitude() + "");
		mRequestManager.request(Actions.ACTION_GET_WEATHER, bundle, mRequestWeatherListener, null);
	}

	/**
	 * return true for success
	 * 
	 * @return
	 */
	public boolean checkBackPress() {
		if (mMenuIndex == 0) {
			return true;
		} else if (mMenuIndex >= 1 && mMenuIndex <= 3) {
			showCommandMenu();
		}
		return false;
	}

	private void showCommandMenu() {
		mMenuIndex = 0;
		adapterMain.clear();
		adapterMain.add(new ItemListFunction.Builder(getBaseActivity()).setType(ItemListFunction.TYPE_FUNCTION_CLASSIC)
				.setDescResId(R.string.function_desc_communication)
				.setItemClickId(ItemListFunction.FUNCTION_GROUP_COMUNICATION).setColorResId(R.color.white).build());
		// add information item
		adapterMain.add(new ItemListFunction.Builder(getBaseActivity()).setType(ItemListFunction.TYPE_FUNCTION_CLASSIC)
				.setDescResId(R.string.function_desc_information)
				.setItemClickId(ItemListFunction.FUNCTION_GROUP_INFORMATION).setColorResId(R.color.white).build());
		// add emergency item
		adapterMain.add(new ItemListFunction.Builder(getBaseActivity()).setType(ItemListFunction.TYPE_FUNCTION_CLASSIC)
				.setDescResId(R.string.function_desc_emergency)
				.setItemClickId(ItemListFunction.FUNCTION_GROUP_EMERGENCY).setColorResId(R.color.white).build());
		// add administration
		adapterMain.add(new ItemListFunction.Builder(getBaseActivity()).setType(ItemListFunction.TYPE_FUNCTION_CLASSIC)
				.setDescResId(R.string.function_desc_admin).setItemClickId(ItemListFunction.FUNCTION_GROUP_ADMIN)
				.setColorResId(R.color.white).build());
		adapterMain.notifyDataSetChanged();
	}

	private void showCommunicationMenu() {
		mMenuIndex = 1;
		adapterMain.clear();
		// add sent text message
		adapterMain.add(new ItemListFunction.Builder(getBaseActivity()).setType(ItemListFunction.TYPE_FUNCTION_CLASSIC)
				.setDescResId(R.string.function_sent_text_sms).setItemClickId(ItemListFunction.FUNCTION_SENT_MESSAGE)
				.setColorResId(R.color.white).build());
		// add image message
		adapterMain.add(new ItemListFunction.Builder(getBaseActivity()).setType(ItemListFunction.TYPE_FUNCTION_CLASSIC)
				.setDescResId(R.string.function_calendar).setItemClickId(ItemListFunction.FUNCTION_CHECK_MY_CALENDAR)
				.setColorResId(R.color.white).build());
		// add sms status
		adapterMain.add(new ItemListFunction.Builder(getBaseActivity()).setType(ItemListFunction.TYPE_FUNCTION_CLASSIC)
				.setDescResId(R.string.function_desc_getting_data).setItemClickId(ItemListFunction.FUNCTION_READ_SMS)
				.setColorResId(R.color.event1_color).build());
		// add email status
		adapterMain.add(new ItemListFunction.Builder(getBaseActivity()).setType(ItemListFunction.TYPE_FUNCTION_CLASSIC)
				.setDescResId(R.string.function_desc_getting_data).setItemClickId(ItemListFunction.FUNCTION_READ_EMAIL)
				.setColorResId(R.color.event2_color).build());
		// add set reminder
		adapterMain.add(new ItemListFunction.Builder(getBaseActivity()).setType(ItemListFunction.TYPE_FUNCTION_CLASSIC)
				.setDescResId(R.string.function_desc_set_reminder)
				.setItemClickId(ItemListFunction.FUNCTION_SET_REMINDER).setColorResId(R.color.white).build());
		// add back item
		adapterMain.add(new ItemListFunction.Builder(getBaseActivity()).setType(ItemListFunction.TYPE_FUNCTION_CLASSIC)
				.setDescResId(R.string.function_desc_back).setItemClickId(ItemListFunction.FUNCTION_BACK_TO_COMMAND)
				.setColorResId(R.color.buttonFocused1).build());
		int unread = 0;
		if (ReadSMSTask.mListSMS != null) {
			unread = 0;
			for (int i = 0; i < ReadSMSTask.mListSMS.size(); i++) {
				if (ReadSMSTask.mListSMS.get(i).read == 0) {
					unread++;
				}
			}
			changeSmsItem(unread);
		} else {
			changeSmsItem(0);
		}
		if (ReadEmailTask.emails != null) {
			unread = 0;
			for (int i = 0; i < ReadEmailTask.emails.size(); i++) {
				if (!ReadEmailTask.emails.get(i).seen) {
					unread++;
				}
			}
			changeEmailItem(unread, readFail);
		} else {
			changeEmailItem(0, readFail);
		}
	}

	private void showInformationMenu() {
		mMenuIndex = 2;
		adapterMain.clear();
		// add sent text message
		adapterMain.add(new ItemListFunction.Builder(getBaseActivity()).setType(ItemListFunction.TYPE_FUNCTION_CLASSIC)
				.setDescResId(R.string.function_desc_news).setItemClickId(ItemListFunction.FUNCTION_CHECK_NEWS)
				.setColorResId(R.color.white).build());
		// add image message
		adapterMain.add(new ItemListFunction.Builder(getBaseActivity()).setType(ItemListFunction.TYPE_FUNCTION_CLASSIC)
				.setDescResId(R.string.function_desc_weather).setItemClickId(ItemListFunction.FUNCTION_CHECK_WEATHER)
				.setColorResId(R.color.white).build());
		// add back item
		adapterMain.add(new ItemListFunction.Builder(getBaseActivity()).setType(ItemListFunction.TYPE_FUNCTION_CLASSIC)
				.setDescResId(R.string.function_desc_back).setItemClickId(ItemListFunction.FUNCTION_BACK_TO_COMMAND)
				.setColorResId(R.color.buttonFocused1).build());
		adapterMain.notifyDataSetChanged();
	}

	private void showAdminMenu() {
		mMenuIndex = 3;
		adapterMain.clear();
		// // add sent text message
		// adapterMain.add(new
		// ItemListFunction.Builder(getBaseActivity()).setType(ItemListFunction.TYPE_FUNCTION_CLASSIC)
		// .setDescResId(R.string.function_desc_set_alarm).setItemClickId(ItemListFunction.FUNCTION_SET_ALARM)
		// .setColorResId(R.color.white).build());
		// add image message
		adapterMain.add(new ItemListFunction.Builder(getBaseActivity()).setType(ItemListFunction.TYPE_FUNCTION_CLASSIC)
				.setDescResId(R.string.function_desc_set_reminder)
				.setItemClickId(ItemListFunction.FUNCTION_SET_REMINDER).setColorResId(R.color.white).build());
		// add setup email
		adapterMain.add(new ItemListFunction.Builder(getBaseActivity()).setType(ItemListFunction.TYPE_FUNCTION_CLASSIC)
				.setDescResId(R.string.function_setup_email_account)
				.setItemClickId(ItemListFunction.FUNCTION_SETUP_EMAIL_ACCOUNT).setColorResId(R.color.white).build());
		// add back item
		adapterMain.add(new ItemListFunction.Builder(getBaseActivity()).setType(ItemListFunction.TYPE_FUNCTION_CLASSIC)
				.setDescResId(R.string.function_desc_back).setItemClickId(ItemListFunction.FUNCTION_BACK_TO_COMMAND)
				.setColorResId(R.color.buttonFocused1).build());
		adapterMain.notifyDataSetChanged();
	}

	@Override
	public void doFunction(int id) {
		switch (id) {
			case ItemListFunction.FUNCTION_BACK_TO_COMMAND:
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
				ModuleManager.getInstance().runModule(Module.MODULE_SET_REMINDER);
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
