package org.cnc.msrobot.Activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.cnc.msrobot.R;
import org.cnc.msrobot.Utils.ReadSMSTask;
import org.cnc.msrobot.Utils.SpeakUtils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class MainActivity extends BaseActivity implements OnInitListener {
	private static final int	CODE_CHECK_TTS		= 1;
	private final int			NUM_RESULT			= 3;
	private final int			CODE_LISTEN			= 2;
	private final float			SPEECH_RATE			= 0.5f;
	private TextToSpeech		mTts;
	private boolean				mRecognizeEnabled	= false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ListView lv = (ListView) findViewById(R.id.lvMain);
		String[] l = getResources().getStringArray(R.array.array_list_function);
		ArrayList<String> list = new ArrayList<String>();
		for (String s : l) {
			list.add(s);
		}
		lv.setAdapter(new MainAdapter(this, list));

		initVoiceRecognizor();
		checkTTS();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mTts != null) {
			mTts.shutdown();
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CODE_CHECK_TTS) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				// success, create the TTS instance
				mTts = new TextToSpeech(this, this);
			} else {
				// missing data, install it
				Intent installIntent = new Intent();
				installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installIntent);
			}
		} else if (requestCode == CODE_LISTEN && resultCode == RESULT_OK) {
			ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

		}
	}

	private void listen() {
		Intent listenIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		listenIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
		listenIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		listenIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, NUM_RESULT);
		startActivityForResult(listenIntent, CODE_LISTEN);
	}

	private void checkTTS() {
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, CODE_CHECK_TTS);
	}

	private void initVoiceRecognizor() {
		PackageManager packageManager = getPackageManager();
		List<ResolveInfo> list = packageManager.queryIntentActivities(new Intent(
				RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);

		if (!list.isEmpty()) {
			mRecognizeEnabled = true;
		} else {
			mRecognizeEnabled = false;
			showCenterToast("Recognizer not present");
		}
	}

	public class MainAdapter extends ArrayAdapter<String> implements OnClickListener {
		private LayoutInflater	mInfalter;

		public MainAdapter(Context context, ArrayList<String> list) {
			super(context, 0, list);
			mInfalter = LayoutInflater.from(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			String divine = getItem(position);
			ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInfalter.inflate(R.layout.item_list_main, parent, false);
				holder.btnFunction = (Button) convertView.findViewById(R.id.btnFunctions);
				holder.btnFunction.setOnClickListener(this);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.btnFunction.setTag(position);
			holder.btnFunction.setText(divine);
			convertView.setTag(holder);
			return convertView;
		}

		public class ViewHolder {
			Button	btnFunction;
		}

		@Override
		public void onClick(View v) {
			int pos = (Integer) v.getTag();
			switch (pos) {
				case 0:
					new ReadSMSTask(getContext(), mTts).execute();
					break;
				case 1:
					mTts.stop();
					break;
				case 6:
					if (mTts != null) {
						mTts.speak(SpeakUtils.formatCurrentTime(getContext()), TextToSpeech.QUEUE_FLUSH, null);
					}
					break;
			}
		}
	}

	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			if (mTts.isLanguageAvailable(Locale.US) == TextToSpeech.LANG_AVAILABLE) mTts.setLanguage(Locale.US);
			mTts.setSpeechRate(SPEECH_RATE);
		} else {
			showCenterToast("TTS engine not present");
		}
	}
}
