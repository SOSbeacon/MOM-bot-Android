package org.cnc.msrobot.fragment;

import java.util.ArrayList;

import org.cnc.msrobot.activity.BaseActivity;
import org.cnc.msrobot.activity.BaseActivity.FragmentRecognizeVoiceListener;
import org.cnc.msrobot.activity.BaseActivity.SpeakAnimationListener;
import org.cnc.msrobot.requestmanager.RequestManager;
import org.cnc.msrobot.utils.SharePrefs;

import android.app.Activity;
import android.content.ContentResolver;
import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * 
 * @author thanhlcm
 * 
 */
public class BaseFragment extends Fragment implements FragmentRecognizeVoiceListener {
	protected SharePrefs mSharePrefs = SharePrefs.getInstance();
	protected RequestManager mRequestManager = RequestManager.getInstance();
	private final Object attachingActivityLock = new Object();
	private BaseActivity mActivity;
	private boolean syncVariable = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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

	@Override
	public void onDetach() {
		super.onDetach();
		synchronized (attachingActivityLock) {
			syncVariable = true;
			mActivity = null;
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
		if (getBaseActivity() != null) {
			getBaseActivity().showCenterToast(message);
		}
	}

	/**
	 * Show a toast in the center of screen. Fix: not show if toast is showed
	 * 
	 * @author thanhle
	 * @param resId
	 *            Resource string Id
	 */
	public void showCenterToast(int resId) {
		if (getBaseActivity() != null) {
			getBaseActivity().showCenterToast(resId);
		}
	}

	public void showProgress() {
		((BaseActivity) getBaseActivity()).showProgress();
	}

	public void dismissProgress() {
		if (getBaseActivity() != null) {
			getBaseActivity().dismissProgress();
		}
	}

	public ContentResolver getContentResolver() {
		return getBaseActivity().getContentResolver();
	}

	public void speak(String msg, int queueMode) {
		if (getBaseActivity() != null) {
			getBaseActivity().speak(msg, queueMode);
		}
	}

	public void stopSpeak() {
		if (getBaseActivity() != null) {
			getBaseActivity().stopSpeak();
		}
	}

	@Override
	public void onRecognize(final ArrayList<String> data) {
	}

	public void listen() {
		if (getBaseActivity() != null) {
			getBaseActivity().listen();
		}
	}

	public void refresh() {
	}

	public void setOnSpeakAnimationListener(SpeakAnimationListener listener) {
		if (getBaseActivity() != null) {
			getBaseActivity().setOnSpeakAnimationListener(listener);
		}
	}
}