package org.cnc.msrobot.resource.weather;

import com.google.gson.annotations.SerializedName;

public class WeatherCondition {

	@SerializedName("main")
	public String	main;

	@SerializedName("description")
	public String	description;

	@SerializedName("icon")
	public String	icon;

	public WeatherCondition() {
	}

	public WeatherCondition(String main, String description, String icon) {
		this.main = main;
		this.description = description;
		this.icon = icon;
	}
}
