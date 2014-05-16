package org.cnc.mombot.resource;

import android.content.ContentValues;

public class UserResource implements BaseResource {
	public String auth_token;
	public User user;

	@Override
	public ContentValues prepareContentValue() {
		return null;
	}
}