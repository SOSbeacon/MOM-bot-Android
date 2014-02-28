package org.cnc.msrobot.fragment;

import java.util.ArrayList;

import org.cnc.msrobot.activity.BaseActivity;
import org.cnc.msrobot.activity.BaseActivity.RecognizeVoiceListener;
import org.cnc.msrobot.requestmanager.RequestManager;
import org.cnc.msrobot.utils.DialogUtils;
import org.cnc.msrobot.utils.SharePrefs;

import android.app.Activity;
import android.content.ContentResolver;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;

/**
 * 
 * @author thanhlcm
 * 
 */
public class BaseFragment extends Fragment implements RecognizeVoiceListener {
	protected SharePrefs mSharePrefs = SharePrefs.getInstance();
	protected RequestManager mRequestManager = RequestManager.getInstance();
	private final Object attachingActivityLock = new Object();
	private BaseActivity mActivity;
	private boolean syncVariable = false;
	protected DialogUtils mDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDialog = new DialogUtils(getBaseActivity());
		getBaseActivity().setOnRecogizeVoiceListener(this);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		synchronized (attachingActivityLock) {
			syncVariable = true;
			mActivity = (BaseActivity) activity;
			attachingActivityLock.notifyAll();
		}
	}

	public BaseActivity getBaseActivity() {
		synchronized (attachingActivityLock) {
			while (!syncVariable) {
				try {
					attachingActivityLock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return mActivity;
		}
	}

	/**
	 * Show a toast in the center of screen.
	 * 
	 * @author thanhle
	 * @param message
	 *            message to be shown
	 */
	public void showCenterToast(String message) {
		((BaseActivity) getBaseActivity()).showCenterToast(message);
	}

	/**
	 * Show a toast in the center of screen. Fix: not show if toast is showed
	 * 
	 * @author thanhle
	 * @param resId
	 *            Resource string Id
	 */
	public void showCenterToast(int resId) {
		((BaseActivity) getBaseActivity()).showCenterToast(resId);
	}

	public void showProgress() {
		((BaseActivity) getBaseActivity()).showProgress();
	}

	public void dismissProgress() {
		((BaseActivity) getBaseActivity()).dismissProgress();
	}

	public ContentResolver getContentResolver() {
		return getBaseActivity().getContentResolver();
	}

	public TextToSpeech getTextToSpeech() {
		return getBaseActivity().getTextToSpeech();
	}

	@Override
	public void onRecognize(final ArrayList<String> data) {
	}

	public void listen() {
		getBaseActivity().listen();
	}
}