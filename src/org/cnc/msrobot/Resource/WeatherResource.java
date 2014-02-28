package org.cnc.msrobot.resource;

import java.util.ArrayList;

import org.cnc.msrobot.resource.Weather.WeatherCondition;
import org.cnc.msrobot.resource.Weather.WeatherTemperature;
import org.cnc.msrobot.resource.Weather.WeatherWind;

import com.google.gson.annotations.SerializedName;

public class WeatherResource {

	@SerializedName("weather")
	public ArrayList<WeatherCondition> condition = new ArrayList<WeatherCondition>();

	@SerializedName("main")
	public WeatherTemperature temperature;

	@SerializedName("wind")
	public WeatherWind wind;

	@SerializedName("name")
	public String cityName;

	@SerializedName("sys")
	private WeatherSys sys;

	public String country() {
		if (sys != null) {
			return sys.country;
		} else {
			return "";
		}
	}

	public WeatherResource() {
	}

	public WeatherResource(WeatherCondition condition, WeatherTemperature temperature, WeatherWind wind) {
		super();
		this.condition.add(condition);
		this.temperature = temperature;
		this.wind = wind;
	}

	private class WeatherSys {
		public String country;
	}
}
