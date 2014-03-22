package com.ayushhgoyal.oauth;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.io.OutputStream;
import java.security.Provider;
import java.security.Security;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;

import android.util.Base64;
import android.util.Log;

import com.sun.mail.smtp.SMTPTransport;
import com.sun.mail.util.BASE64EncoderStream;
public class GMailOauthSender {
	private Session session;

	public SMTPTransport connectToSmtp(String host, int port, String userEmail,
			String oauthToken, boolean debug) throws Exception {

		Properties props = new Properties();
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.starttls.required", "true");
		props.put("mail.smtp.sasl.enable", "false");
		session = Session.getInstance(props);
		session.setDebug(debug);

		final URLName unusedUrlName = null;
		SMTPTransport transport = new SMTPTransport(session, unusedUrlName);
		// If the password is non-null, SMTP tries to do AUTH LOGIN.
		final String emptyPassword = null;
		transport.connect(host, port, userEmail, emptyPassword);

		byte[] response = String.format("user=%s\1auth=Bearer %s\1\1",
				userEmail, oauthToken).getBytes();
		response = BASE64EncoderStream.encode(response);

		// String decoded = new String(Base64.decode(response, Base64.DEFAULT));

		transport.issueCommand("AUTH XOAUTH2 " + new String(response), 235);

		return transport;
	}

	public synchronized void sendMail(String subject, String body, String user,
			String oauthToken, String recipients) {

		try {
			// original port was 587
			SMTPTransport smtpTransport = connectToSmtp("smtp.gmail.com", 587,
					user, oauthToken, true);

			MimeMessage message = new MimeMessage(session);
			DataHandler handler = new DataHandler(new ByteArrayDataSource(
					body.getBytes(), "text/plain"));
			message.setSender(new InternetAddress(user));
			message.setSubject(subject);
			message.setDataHandler(handler);
			if (recipients.indexOf(',') > 0)
				message.setRecipients(Message.RecipientType.TO,
						InternetAddress.parse(recipients));
			else
				message.setRecipient(Message.RecipientType.TO,
						new InternetAddress(recipients));
			smtpTransport.sendMessage(message, message.getAllRecipients());

		} catch (Exception e) {
			// Log.d("test", e.getMessage());
			e.printStackTrace();
		}

	}
}