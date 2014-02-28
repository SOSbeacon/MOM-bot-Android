package org.cnc.msrobot.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.FlagTerm;

import org.cnc.msrobot.R;
import org.cnc.msrobot.fragment.HomeFragment;

import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;

public class ReadEmailTask extends AsyncTask<String, Void, Boolean> {
	private ArrayList<Email> emails;
	private HomeFragment mContext;
	private int unReadCount;
	private String email;
	private String pass;

	public ReadEmailTask(HomeFragment context) {
		mContext = context;
	}

	@Override
	protected Boolean doInBackground(String... params) {
		Properties props = System.getProperties();
		props.setProperty("mail.store.protocol", "imaps");
		email = params[0];
		pass = params[1];
		if (TextUtils.isEmpty(pass) || TextUtils.isEmpty(email)) return false;
		try {
			emails = readEmail(email, pass);
			return true;
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		if (result && emails != null && mContext.getTextToSpeech() != null) {
			unReadCount = emails.size();
			String unReadString;
			if (unReadCount == 0) {
				unReadString = mContext.getResources().getString(R.string.email_no_message);
			} else if (unReadCount > 1) {
				unReadString = mContext.getResources().getString(R.string.email_unreads, unReadCount);
			} else {
				unReadString = mContext.getResources().getString(R.string.email_unread, unReadCount);
			}
			// speech
			mContext.getTextToSpeech().speak(unReadString, TextToSpeech.QUEUE_ADD, null);
		}
		mContext.changeEmailItem(unReadCount);
	}

	public ArrayList<Email> readEmail(String email, String pass) throws NoSuchProviderException, MessagingException,
			IOException {
		Properties props = System.getProperties();
		props.setProperty("mail.store.protocol", "imaps");
		try {
			Session session = Session.getDefaultInstance(props, null);
			Store store = session.getStore("imaps");
			// IMAP host for gmail.
			store.connect("imap.gmail.com", email, pass);

			Folder inbox = store.getFolder("Inbox");
			inbox.open(Folder.READ_ONLY);
			FlagTerm ft = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
			Message[] msgs = inbox.search(ft);
			ArrayList<Email> emails = new ArrayList<Email>();
			for (int i = 0; i < msgs.length; i++) {
				Message msg = msgs[i];
				Email e = new Email();
				e.content = msg.getContent().toString();
				e.from = msg.getFrom()[0].toString();
				String s = msg.getContent() + "";
				if (s.indexOf("MimeMultipart") != -1) {
					Multipart multipart = (Multipart) msg.getContent();

					BodyPart bodyPart = multipart.getBodyPart(0);
					e.content = bodyPart.getContent().toString();
				}

				emails.add(e);
			}
			inbox.close(true);
			store.close();
			return emails;
		} catch (NoSuchProviderException e) {
			throw e;
		} catch (MessagingException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		}
	}

	public class Email {
		public String from, subject, content;
	}

	/**
	 * return unread sms count
	 * 
	 * @return
	 */
	public int getCount() {
		return unReadCount;
	}

	public void speakEmailDetail() {
		if (mContext.getTextToSpeech() == null) return;
		if (emails != null && emails.size() > 0) {
			new MarkEmailSeenTask(mContext).execute(email, pass);
			for (int i = 0; i < emails.size(); i++) {
				Email email = emails.get(i);
				// get string for speech sms
				String readMessage;
				readMessage = mContext.getResources().getString(R.string.email_read_message, email.from, email.content);
				// speech
				mContext.getTextToSpeech().speak(readMessage, TextToSpeech.QUEUE_ADD, null);
			}
		}
	}
}
