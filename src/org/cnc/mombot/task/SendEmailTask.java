package org.cnc.mombot.task;

import org.cnc.mombot.R;
import org.cnc.mombot.activity.BaseActivity;
import org.cnc.mombot.utils.GMailSender;
import org.cnc.mombot.utils.SharePrefs;

import android.os.AsyncTask;
import android.text.TextUtils;

public class SendEmailTask extends AsyncTask<Void, Void, Boolean> {
	private final String to, subject, body, username, password, image;
	private BaseActivity activity;

	public SendEmailTask(BaseActivity activity, String to, String subject, String body, String image) {
		this.activity = activity;
		this.username = SharePrefs.getInstance().getGmailUsername();;
		this.password = SharePrefs.getInstance().getGmailPass();
		this.to = to;
		this.subject = subject;
		this.body = body;
		this.image = image;
	}

	@Override
	protected void onPreExecute() {
		if (TextUtils.isEmpty(username)) {
			activity.showCenterToast(R.string.msg_info_setup_email);
		}
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		if (TextUtils.isEmpty(username)) { return false; }
		GMailSender sender = new GMailSender(username, password);
		try {
			sender.sendMail(subject, body, username, to, image);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		if (result) {
			activity.showCenterToast(R.string.msg_info_send_email_success);
		} else {
			activity.showCenterToast(R.string.msg_err_send_email_fail);
		}
	}

}
