package org.cnc.msrobot.recognizemodule;

import java.util.ArrayList;

import org.cnc.msrobot.R;
import org.cnc.msrobot.activity.MainActivity;
import org.cnc.msrobot.recognizemodule.RecognizeBase.RecognizeModuleListener;
import org.cnc.msrobot.utils.AppUtils;

public class RecognizeSearch extends RecognizeBase implements RecognizeModuleListener {
	private MainActivity activity;

	public RecognizeSearch(MainActivity activity) {
		super(activity, RecoginizeIds.MODULE_SEARCH, activity.getString(R.string.recognize_search),
				activity.getString(R.string.recognize_search), null, true);
		this.activity = activity;
		setListener(this);
	}

	@Override
	public void onRecoginze(final ArrayList<String> data) {
		AppUtils.showGoogleSearchIntent(activity, data.get(0));
	}
}