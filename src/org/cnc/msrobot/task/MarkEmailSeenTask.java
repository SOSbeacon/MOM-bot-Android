package org.cnc.msrobot.task;

import java.util.Properties;

import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

import org.cnc.msrobot.resource.Email;
import org.cnc.msrobot.utils.SharePrefs;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.sun.mail.imap.IMAPFolder;

public class MarkEmailSeenTask extends AsyncTask<Void, Void, Boolean> {
	private int pos;

	/**
	 * 
	 * @param context
	 *            MainActivity
	 * @param pos
	 *            position of email want to mark seen
	 */
	public MarkEmailSeenTask(int pos) {
		this.pos = pos;
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		if (ReadEmailTask.emails == null || pos < 0 || pos >= ReadEmailTask.emails.size()) return false;
		Email e = ReadEmailTask.emails.get(pos);
		if (e.seen) return false;
		Properties props = System.getProperties();
		props.setProperty("mail.store.protocol", "imaps");
		String email = SharePrefs.getInstance().getGmailUsername();
		String pass = SharePrefs.getInstance().getGmailPass();
		if (TextUtils.isEmpty(pass) || TextUtils.isEmpty(email)) return false;
		try {
			e.seen = true;
			markEmailSeen(email, pass, e.uid);
			return true;
		} catch (NoSuchProviderException ex) {
			ex.printStackTrace();
		} catch (MessagingException ex) {
			ex.printStackTrace();
		}
		return false;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		if (result) {

		}
	}

	public void markEmailSeen(String email, String pass, long uid) throws NoSuchProviderException, MessagingException {
		Properties props = System.getProperties();
		props.setProperty("mail.store.protocol", "imaps");
		try {
			Session session = Session.getDefaultInstance(props, null);
			Store store = session.getStore("imaps");
			// IMAP host for gmail.
			store.connect("imap.gmail.com", email, pass);
			IMAPFolder inbox = (IMAPFolder) store.getFolder("Inbox");
			inbox.open(Folder.READ_WRITE);
			Message[] msgs = inbox.getMessagesByUID(new long[] { uid });
			for (Message msg : msgs) {
				msg.setFlag(Flag.SEEN, true);
			}
			inbox.close(true);
			store.close();
		} catch (NoSuchProviderException e) {
			throw e;
		} catch (MessagingException e) {
			throw e;
		}
	}
}
