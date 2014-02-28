package org.cnc.msrobot.activity;

import org.cnc.msrobot.R;
import org.cnc.msrobot.resource.WeatherResource;
import org.cnc.msrobot.utils.Actions;
import org.cnc.msrobot.utils.Consts;
import org.cnc.msrobot.utils.LocationUtils;
import org.cnc.msrobot.utils.LocationUtils.LocationUtilsListener;

import android.location.Location;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response.Listener;

public class WeatherActivity extends BaseActivity {
	private TextView tvCityText;
	private TextView tvCondDescr;
	private TextView tvTemp;
	private TextView tvPress;
	private TextView tvWindSpeed;
	private TextView tvWindDeg;
	private TextView tvHum;
	private ImageView imgView;
	private LocationUtils mLocationUtils;
	private Location mLastLocation;

	private Listener<WeatherResource> mRequestWeatherListener = new Listener<WeatherResource>() {
		@Override
		public void onResponse(WeatherResource response) {
			showWeather(response, true);
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
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_weather);
		init();
		requestLocation();
	}

	private void init() {
		tvCityText = (TextView) findViewById(R.id.cityText);
		tvCondDescr = (TextView) findViewById(R.id.condDescr);
		tvTemp = (TextView) findViewById(R.id.temp);
		tvHum = (TextView) findViewById(R.id.hum);
		tvPress = (TextView) findViewById(R.id.press);
		tvWindSpeed = (TextView) findViewById(R.id.windSpeed);
		tvWindDeg = (TextView) findViewById(R.id.windDeg);
		imgView = (ImageView) findViewById(R.id.condIcon);

		mLocationUtils = new LocationUtils();
		WeatherResource currentWeather = mSharePrefs.getCurrentWeather();
		showWeather(currentWeather, false);
	}

	private void showWeather(WeatherResource response, boolean speech) {
		String location = response.cityName + ", " + response.country();
		String condition = response.condition.get(0).main + "(" + response.condition.get(0).description + ")";
		String degress = getString(R.string.common_degress);
		String tempp = response.temperature.temp + " " + degress + "C";
		tvCityText.setText(location);
		tvCondDescr.setText(condition);
		tvTemp.setText(tempp);
		tvHum.setText("" + response.temperature.humidity + "%");
		tvPress.setText("" + response.temperature.pressure + " hPa");
		tvWindSpeed.setText("" + response.wind.speed + " mps");
		tvWindDeg.setText("" + response.wind.deg + " " + degress);
		if (speech) {
			String locationSpeech = getString(R.string.weather_location, location);
			String temp = Math.round(response.temperature.temp) + " " + degress + "C";
			String weatherSpeech = getString(R.string.weather_infomation, condition, temp);
			if (mTts != null) {
				mTts.speak(locationSpeech, TextToSpeech.QUEUE_FLUSH, null);
				mTts.speak(weatherSpeech, TextToSpeech.QUEUE_ADD, null);
			}
		}
	}

	private void requestLocation() {
		mLastLocation = mLocationUtils.getLocation(this, mLocationRecall, false);
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
}
