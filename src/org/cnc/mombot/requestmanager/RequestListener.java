package org.cnc.mombot.requestmanager;

public interface RequestListener<T> {
	public void postAfterRequest(T result);
}
