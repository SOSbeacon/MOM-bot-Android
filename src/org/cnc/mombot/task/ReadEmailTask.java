package org.cnc.mombot.task;

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
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;

import org.cnc.mombot.R;
import org.cnc.mombot.activity.MainActivity;
import org.cnc.mombot.resource.Email;

import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.text.Html;
import android.text.TextUtils;

import com.sun.mail.imap.IMAPFolder;

public class ReadEmailTask extends AsyncTask<String, Void, Boolean> {
	public static ArrayList<Email> emails;
	private MainActivity mContext;
	private String email;
	private String pass;

	public ReadEmailTask(MainActivity context) {
		mContext = context;
	}

	@Override
	protected void onPreExecute() {
		mContext.changeEmailLoading();
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
		if (mContext == null) return;
		if (result && emails != null) {
			int unReadCount = emails.size();
			String unReadString;
			if (unReadCount == 0) {
				unReadString = mContext.getString(R.string.email_no_message);
			} else if (unReadCount > 1) {
				unReadString = mContext.getString(R.string.email_unreads, unReadCount);
			} else {
				unReadString = mContext.getString(R.string.email_unread, unReadCount);
			}
			// speech
			mContext.getTextToSpeech().speak(unReadString, TextToSpeech.QUEUE_ADD);
			mContext.changeEmailItem(unReadCount, false);
		} else if (result == false) {
			mContext.changeEmailItem(0, true);
		}
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

			IMAPFolder inbox = (IMAPFolder) store.getFolder("Inbox");
			inbox.open(Folder.READ_ONLY);
			FlagTerm ft = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
			Message[] msgs = inbox.search(ft);
			ArrayList<Email> emails = new ArrayList<Email>();
			for (int i = 0; i < msgs.length; i++) {
				Message msg = msgs[i];
				Email e = new Email();
				e.uid = inbox.getUID(msg);
				e.subject = msg.getSubject();
				e.from = msg.getFrom()[0].toString();
				String s = msg.getContent() + "";
				if (s.indexOf("MimeMultipart") != -1) {
					Multipart multipart = (Multipart) msg.getContent();

					// get text content
					BodyPart bodyPart = multipart.getBodyPart(0);
					if (bodyPart.getContent() instanceof MimeMultipart) {
						MimeMultipart minePart = (MimeMultipart) bodyPart.getContent();
						bodyPart = minePart.getBodyPart(0);
						if (minePart.getCount() == 1) {
							e.htmlContent = bodyPart.getContent().toString();
							e.content = Html.fromHtml(e.htmlContent).toString();
						} else {
							e.content = bodyPart.getContent().toString();
							e.htmlContent = minePart.getBodyPart(1).getContent().toString();
						}
					} else {
						if (multipart.getCount() == 1) {
							e.htmlContent = bodyPart.getContent().toString();
							e.content = Html.fromHtml(e.htmlContent).toString();
						} else {
							e.content = bodyPart.getContent().toString();
							e.htmlContent = multipart.getBodyPart(1).getContent().toString();
						}
					}
				} else {
					e.content = e.htmlContent = s;
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
}
