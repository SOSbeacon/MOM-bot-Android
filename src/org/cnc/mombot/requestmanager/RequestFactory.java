package org.cnc.mombot.requestmanager;

import org.cnc.mombot.requestmanager.request.CreateEmergencyRequest;
import org.cnc.mombot.requestmanager.request.CreateEventRequest;
import org.cnc.mombot.requestmanager.request.GetListContactRequest;
import org.cnc.mombot.requestmanager.request.GetListEventRequest;
import org.cnc.mombot.requestmanager.request.GetWeatherRequest;
import org.cnc.mombot.requestmanager.request.LoginRequest;
import org.cnc.mombot.requestmanager.request.LogoutRequest;
import org.cnc.mombot.requestmanager.request.UpdateEmergencyRequest;
import org.cnc.mombot.utils.Actions;

import android.content.Context;

/**
 * return request class base on action
 * 
 * @author thanhle
 *
 */
public class RequestFactory {
	@SuppressWarnings("rawtypes")
	public static RequestBase getRequest(Context context, int action) {
		switch (action) {
			case Actions.ACTION_GET_WEATHER:
				return new GetWeatherRequest(context);
			case Actions.ACTION_LOGIN:
				return new LoginRequest(context);
			case Actions.ACTION_LOGOUT:
				return new LogoutRequest(context);
			case Actions.ACTION_GET_LIST_EVENT:
				return new GetListEventRequest(context);
			case Actions.ACTION_CREATE_EVENT:
				return new CreateEventRequest(context);
			case Actions.ACTION_GET_LIST_CONTACT:
				return new GetListContactRequest(context);
			case Actions.ACTION_CREATE_EMERGENCY:
				return new CreateEmergencyRequest(context);
			case Actions.ACTION_UPDATE_EMERGENCY:
				return new UpdateEmergencyRequest(context);
			default:
				return null;
		}
	}
}
