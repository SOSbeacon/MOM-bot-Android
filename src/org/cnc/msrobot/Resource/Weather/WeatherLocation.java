package org.cnc.msrobot.resource.Weather;

public class WeatherLocation {
	public int		id;
	public String	city;
	public String	country;
	public String	lng, lat;

	public WeatherLocation() {
	}

	public WeatherLocation(int id, String city, String country, String lng, String lat) {
		this.id = id;
		this.city = city;
		this.country = country;
		this.lng = lng;
		this.lat = lat;
	}
}
