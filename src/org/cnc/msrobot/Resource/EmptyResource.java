package org.cnc.msrobot.resource;

import android.content.ContentValues;

public class EmptyResource implements BaseResource {

	@Override
	public ContentValues prepareContentValue() {
		return null;
	}
}