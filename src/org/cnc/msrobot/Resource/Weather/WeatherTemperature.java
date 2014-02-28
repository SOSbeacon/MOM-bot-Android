package org.cnc.msrobot.resource.Weather;

import com.google.gson.annotations.SerializedName;

public class WeatherTemperature {

	@SerializedName("temp")
	public float temp;

	@SerializedName("temp_min")
	public float tempMin;

	@SerializedName("temp_max")
	public float tempMax;

	@SerializedName("pressure")
	public float pressure;

	@SerializedName("sea_level")
	public float seaLevel;

	@SerializedName("grnd_level")
	public float grndLevel;

	@SerializedName("humidity")
	public float humidity;

	public WeatherTemperature() {
	}

	public WeatherTemperature(float temp, float tempMin, float tempMax, float pressure, float seaLevel,
			float grndLevel, float humidity) {
		super();
		this.temp = temp;
		this.tempMin = tempMin;
		this.tempMax = tempMax;
		this.pressure = pressure;
		this.seaLevel = seaLevel;
		this.grndLevel = grndLevel;
		this.humidity = humidity;
	}
}
