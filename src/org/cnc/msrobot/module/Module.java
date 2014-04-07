package org.cnc.msrobot.module;

import org.cnc.msrobot.InputOutput.Input;
import org.cnc.msrobot.InputOutput.Input.InputReceiveCallback;
import org.cnc.msrobot.InputOutput.Output;
import org.cnc.msrobot.utils.TextToSpeechUtils.SpeechListener;

import android.content.Context;
import android.content.res.Resources;

public abstract class Module implements InputReceiveCallback, SpeechListener {

	public static final int MODULE_COMMAND = 1;
	public static final int MODULE_SEND_MESSAGE = 2;
	public static final int MODULE_SEARCH = 3;
	public static final int MODULE_ALARM = 4;
	public static final int MODULE_ASK_YES_NO = 5;
	
	private Input input;
	private Output output;
	private Context context;
	private Resources resource;

	/**
	 * @param speakMessageBeforeListen
	 *            message will speak before listen
	 * @param messageShowInChatList
	 *            message will show in list chat
	 * @param listener
	 *            listener
	 */
	public Module(Context context, Input input, Output output) {
		this.context = context;
		this.resource = context.getResources();
		this.input = input;
		this.output = output;
		input.setReceiveCallback(this);
		output.setSpeechListener(this);
	}

	public Resources getResource() {
		return resource;
	}

	public void setResource(Resources resource) {
		this.resource = resource;
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public Input getInput() {
		return input;
	}

	public void setInput(Input input) {
		this.input = input;
	}

	public Output getOutput() {
		return output;
	}

	public void setOutput(Output output) {
		this.output = output;
	}

	public abstract void run();
}
