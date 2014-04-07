package org.cnc.msrobot.module;

import org.cnc.msrobot.InputOutput.Input;
import org.cnc.msrobot.InputOutput.Output;
import org.cnc.msrobot.utils.SpeechToText;

import android.content.Context;

public class ModuleManager {
	private Context context;
	private Input input;
	private Output output;
	private static ModuleManager instance = new ModuleManager();

	public static ModuleManager getInstance() {
		return instance;
	}

	public void init(Context ctx, Input input, Output output) {
		this.context = ctx;
		this.input = input;
		this.output = output;
	}

	public void runModule(int moduleId) {
		// check if context, input, output null then return
		if (context == null || input == null || output == null) return;
		if (SpeechToText.getInstance().isRecording) {
			SpeechToText.getInstance().stopListening();
		} else {
			switch (moduleId) {
				case Module.MODULE_COMMAND:
					new CommandModule(context, input, output).run();
					break;
				case Module.MODULE_SEND_MESSAGE:
					new SendMessageModule(context, input, output).run();
					break;
				default:
					break;
			}
		}
	}
}
