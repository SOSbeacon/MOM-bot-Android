package org.cnc.msrobot.fragment;

import java.util.ArrayList;

import org.cnc.msrobot.R;
import org.cnc.msrobot.activity.EmailSetupActivity;
import org.cnc.msrobot.activity.MainActivity;
import org.cnc.msrobot.adapter.MainAdapter;
import org.cnc.msrobot.adapter.MainAdapter.OnFunctionDoListener;
import org.cnc.msrobot.resource.ItemListFunction;
import org.cnc.msrobot.resource.WeatherResource;
import org.cnc.msrobot.task.ReadEmailTask;
import org.cnc.msrobot.task.ReadSMSTask;
import org.cnc.msrobot.utils.Actions;
import org.cnc.msrobot.utils.Consts;
import org.cnc.msrobot.utils.Consts.RequestCode;
import org.cnc.msrobot.utils.CustomActionBar;
import org.cnc.msrobot.utils.LocationUtils;
import org.cnc.msrobot.utils.LocationUtils.LocationUtilsListener;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.android.volley.Response.Listener;

public class ClassicFragment extends BaseFragment implements OnFunctionDoListener {
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

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mLayout = inflater.inflate(R.layout.fragment_classic, container, false);

		ListView lv = (ListView) mLayout.findViewById(R.id.lvMain);
		lv.setAdapter(adapterMain);

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
		getBaseActivity().getCusomActionBar().setType(CustomActionBar.TYPE_CLASSIC);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
		readSmsTask.cancel(true);
		if (readEmailTask != null) {
			readEmailTask.cancel(true);
		}
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
		CustomActionBar actionBar = getBaseActivity().getCusomActionBar();
		if (actionBar == null) return;
		if (count > 0) {
			actionBar.setSMSText(getString(R.string.function_desc_unread_sms, count));
		} else {
			actionBar.setSMSText(getString(R.string.function_desc_no_sms));
		}
	}

	public void changeEmailLoading() {
	}

	public void changeEmailItem(int count, boolean readFail) {
		this.readFail = readFail;
		CustomActionBar actionBar = getBaseActivity().getCusomActionBar();
		if (actionBar == null) return;
		if (!readFail) {
			if (count > 0) {
				actionBar.setEmailText(getString(R.string.function_desc_unread_email, count));
			} else {
				actionBar.setEmailText(getString(R.string.function_desc_no_email));
			}
		} else {
			actionBar.setEmailText(getString(R.string.function_desc_no_email));
		}
	}

	private void initWeahter() {
		mLocationUtils = new LocationUtils();
		requestLocation();
	}

	private void showWeather(WeatherResource response) {
		CustomActionBar actionBar = getBaseActivity().getCusomActionBar();
		if (actionBar == null) return;
		String condition = response.condition.get(0).main;
		String degress = getString(R.string.common_degress);
		String tempp = Math.round(response.temperature.temp) + " " + degress + "C";
		// actionBar.setWeatherText(condition + " (" + tempp + ")");
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
		// add back item
		adapterMain.add(new ItemListFunction.Builder(getBaseActivity()).setType(ItemListFunction.TYPE_FUNCTION_CLASSIC)
				.setDescResId(R.string.function_desc_back).setItemClickId(ItemListFunction.FUNCTION_BACK_TO_COMMAND)
				.setColorResId(R.color.white).build());
		// add sent text message
		adapterMain.add(new ItemListFunction.Builder(getBaseActivity()).setType(ItemListFunction.TYPE_FUNCTION_CLASSIC)
				.setDescResId(R.string.function_sent_text_sms).setItemClickId(ItemListFunction.FUNCTION_SENT_TEXT_SMS)
				.setColorResId(R.color.white).build());
		// add image message
		adapterMain.add(new ItemListFunction.Builder(getBaseActivity()).setType(ItemListFunction.TYPE_FUNCTION_CLASSIC)
				.setDescResId(R.string.function_sent_picture_mms)
				.setItemClickId(ItemListFunction.FUNCTION_SENT_PICTURE_MMS).setColorResId(R.color.white).build());
		// add information item
		adapterMain.add(new ItemListFunction.Builder(getBaseActivity()).setType(ItemListFunction.TYPE_FUNCTION_CLASSIC)
				.setDescResId(R.string.function_sent_text_email)
				.setItemClickId(ItemListFunction.FUNCTION_SENT_TEXT_EMAIL).setColorResId(R.color.white).build());
		// add emergency item
		adapterMain.add(new ItemListFunction.Builder(getBaseActivity()).setType(ItemListFunction.TYPE_FUNCTION_CLASSIC)
				.setDescResId(R.string.function_sent_picture_email)
				.setItemClickId(ItemListFunction.FUNCTION_SENT_PICTURE_EMAIL).setColorResId(R.color.white).build());
		adapterMain.notifyDataSetChanged();
	}

	private void showInformationMenu() {
		mMenuIndex = 2;
		adapterMain.clear();
		// add back item
		adapterMain.add(new ItemListFunction.Builder(getBaseActivity()).setType(ItemListFunction.TYPE_FUNCTION_CLASSIC)
				.setDescResId(R.string.function_desc_back).setItemClickId(ItemListFunction.FUNCTION_BACK_TO_COMMAND)
				.setColorResId(R.color.white).build());
		// add sent text message
		adapterMain.add(new ItemListFunction.Builder(getBaseActivity()).setType(ItemListFunction.TYPE_FUNCTION_CLASSIC)
				.setDescResId(R.string.function_desc_news).setItemClickId(ItemListFunction.FUNCTION_CHECK_NEWS)
				.setColorResId(R.color.white).build());
		// add image message
		adapterMain.add(new ItemListFunction.Builder(getBaseActivity()).setType(ItemListFunction.TYPE_FUNCTION_CLASSIC)
				.setDescResId(R.string.function_desc_weather).setItemClickId(ItemListFunction.FUNCTION_CHECK_WEATHER)
				.setColorResId(R.color.white).build());
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
		}
	}
}
