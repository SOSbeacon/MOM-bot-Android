package org.cnc.msrobot.recognizemodule;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;

public class RecognizeBase {
	private int id;
	private RecognizeModuleListener listener;
	private String speakMessageBeforeListen, messageShowInChatList;
	private boolean showRecognizeDialog;
	private Context context;
	private Resources resource;

	/**
	 * @param id
	 *            id of module
	 * @param speakMessageBeforeListen
	 *            message will speak before listen
	 * @param messageShowInChatList
	 *            message will show in list chat
	 * @param listener
	 *            listener
	 */
	public RecognizeBase(Context context, int id, String speakMessageBeforeListen, String messageShowInChatList,
			RecognizeModuleListener listener, boolean showRecognizeDialog) {
		this.context = context;
		this.resource = context.getResources();
		this.id = id;
		this.speakMessageBeforeListen = speakMessageBeforeListen;
		this.messageShowInChatList = messageShowInChatList;
		this.listener = listener;
		this.showRecognizeDialog = showRecognizeDialog;
	}

	public Resources getResource() {
		return resource;
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public boolean isShowRecognizeDialog() {
		return showRecognizeDialog;
	}

	public void setShowRecognizeDialog(boolean showRecognizeDialog) {
		this.showRecognizeDialog = showRecognizeDialog;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setListener(RecognizeModuleListener listener) {
		this.listener = listener;
	}

	public void setSpeakMessageBeforeListen(String speakMessageBeforeListen) {
		this.speakMessageBeforeListen = speakMessageBeforeListen;
	}

	public void setMessageShowInChatList(String messageShowInChatList) {
		this.messageShowInChatList = messageShowInChatList;
	}

	public String getSpeakMessageBeforeListen() {
		return speakMessageBeforeListen;
	}

	public String getMessageShowInChatList() {
		return messageShowInChatList;
	}

	public RecognizeModuleListener getListener() {
		return listener;
	}

	public interface RecognizeModuleListener {
		void onRecoginze(final ArrayList<String> data);
	}
}
