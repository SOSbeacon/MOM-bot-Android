package org.cnc.mombot.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Security;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import android.text.TextUtils;

public class GMailSender extends javax.mail.Authenticator {
	private String mailhost = "smtp.gmail.com";
	private String user;
	private String password;
	private Session session;

	static {
		Security.addProvider(new JSSEProvider());
	}

	public GMailSender(String user, String password) {
		this.user = user;
		this.password = password;

		Properties props = new Properties();
		props.setProperty("mail.transport.protocol", "smtp");
		props.setProperty("mail.host", mailhost);
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "465");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.socketFactory.fallback", "false");
		props.setProperty("mail.smtp.quitwait", "false");
		// There is something wrong with MailCap, javamail can not find a handler for the multipart/mixed part, so this
		// bit needs to be added.
		// MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
		// mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
		// mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
		// mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
		// mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
		// mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
		// CommandMap.setDefaultCommandMap(mc);
		session = Session.getInstance(props, this);
	}

	protected PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(user, password);
	}

	public synchronized void sendMail(String subject, String body, String sender, String recipients, String image)
			throws Exception {

		MimeMessage message = new MimeMessage(session);
		message.setSender(new InternetAddress(sender));
		message.setSubject(subject);
		DataHandler handler = new DataHandler(new ByteArrayDataSource(body.getBytes(), "text/plain"));
		message.setDataHandler(handler);
		if (!TextUtils.isEmpty(image)) {
			Multipart multiPart = new MimeMultipart();
			// add body part
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setText(body);
			multiPart.addBodyPart(messageBodyPart);
			// add image part
			DataSource source = new FileDataSource(image);
			messageBodyPart = new MimeBodyPart();
			messageBodyPart.setDataHandler(new DataHandler(source));
			messageBodyPart.setFileName(source.getName());
			multiPart.addBodyPart(messageBodyPart);
			message.setContent(multiPart);
		}
		if (recipients.indexOf(',') > 0) message.setRecipients(Message.RecipientType.TO,
				InternetAddress.parse(recipients));
		else
			message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients));
		Transport.send(message);

	}

	public class ByteArrayDataSource implements DataSource {
		private byte[] data;
		private String type;

		public ByteArrayDataSource(byte[] data, String type) {
			super();
			this.data = data;
			this.type = type;
		}

		public ByteArrayDataSource(byte[] data) {
			super();
			this.data = data;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getContentType() {
			if (type == null) return "application/octet-stream";
			else
				return type;
		}

		public InputStream getInputStream() throws IOException {
			return new ByteArrayInputStream(data);
		}

		public String getName() {
			return "ByteArrayDataSource";
		}

		public OutputStream getOutputStream() throws IOException {
			throw new IOException("Not Supported");
		}
	}
}