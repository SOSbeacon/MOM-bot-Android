package org.cnc.msrobot.task;

import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.FlagTerm;

import org.cnc.msrobot.fragment.HomeFragment;

import android.os.AsyncTask;
import android.text.TextUtils;

public class MarkEmailSeenTask extends AsyncTask<String, Void, Boolean> {

	private HomeFragment mContext;

	public MarkEmailSeenTask(HomeFragment context) {
		mContext = context;
	}

	@Override
	protected Boolean doInBackground(String... params) {
		Properties props = System.getProperties();
		props.setProperty("mail.store.protocol", "imaps");
		String email = params[0];
		String pass = params[1];
		if (TextUtils.isEmpty(pass) || TextUtils.isEmpty(email)) return false;
		try {
			markEmailSeen(email, pass);
			return true;
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		if (result) {
			mContext.changeEmailItem(0, false);
		}
	}

	public void markEmailSeen(String email, String pass) throws NoSuchProviderException, MessagingException {
		Properties props = System.getProperties();
		props.setProperty("mail.store.protocol", "imaps");
		try {
			Session session = Session.getDefaultInstance(props, null);
			Store store = session.getStore("imaps");
			// IMAP host for gmail.
			store.connect("imap.gmail.com", email, pass);

			Folder inbox = store.getFolder("Inbox");
			inbox.open(Folder.READ_WRITE);
			FlagTerm ft = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
			Message[] msgs = inbox.search(ft);
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
