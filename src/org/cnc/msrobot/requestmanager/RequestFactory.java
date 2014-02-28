package org.cnc.msrobot.requestmanager;

import org.cnc.msrobot.requestmanager.request.GetWeatherRequest;
import org.cnc.msrobot.utils.Actions;

import android.content.Context;

public class RequestFactory {
	@SuppressWarnings("rawtypes")
	public static RequestBase getRequest(Context context, int action) {
		switch (action) {
			case Actions.ACTION_GET_WEATHER:
				return new GetWeatherRequest(context);
			default:
				return null;
		}
	}
}
