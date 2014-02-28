package org.cnc.msrobot.requestmanager;

public interface RequestListener<T> {
	public void postAfterRequest(T result);
}
