package org.cnc.msrobot.requestmanager.request;

import java.lang.reflect.Type;

import org.cnc.msrobot.requestmanager.RequestBase;
import org.cnc.msrobot.resource.WeatherResource;
import org.cnc.msrobot.utils.Consts;
import org.cnc.msrobot.utils.Consts.URLConsts;
import org.cnc.msrobot.utils.SharePrefs;

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.Request.Method;

public class GetWeatherRequest extends RequestBase<WeatherResource> {

	public GetWeatherRequest(Context context) {
		super(context);
	}

	@Override
	public void postAfterRequest(WeatherResource result) {
		SharePrefs.getInstance().saveCurrentWeather(result);
	}

	@Override
	protected String buildRequestUrl() {
		String id = getExtra().getString(Consts.ID_PARAM);
		String lat = getExtra().getString(Consts.LAT_PARAM);
		String lon = getExtra().getString(Consts.LON_PARAM);
		String url;
		if (TextUtils.isEmpty(id)) {
			url = URLConsts.GET_WEATHER_BY_LOCAITON_INFO.replace(Consts.HOLDER_LAT_PARAM, lat).replace(
					Consts.HOLDER_LON_PARAM, lon);
		} else {
			url = URLConsts.GET_WEATHER_BY_ID_INFO.replace(Consts.HOLDER_ID_PARAM, id);
		}
		return url;
	}

	@Override
	protected Type getClassOf() {
		return WeatherResource.class;
	}

	@Override
	protected int getMethod() {
		return Method.GET;
	}

}
