/**
 *
 */
package org.jmwiki;

import java.util.Date;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.log4j.Logger;

/**
 * Sends mail via SMTP to the specified host. <b>REDISTRIBUTION:</b> you
 * will either have to hard-code your own SMTP host name into the constructor
 * function and recompile, or rewrite the Environment class to record
 * this information in the jmwiki.properties file.
 */
public class WikiMail {

	private static final Logger logger = Logger.getLogger(WikiMail.class);
	private Session session;
	private static WikiMail instance;

	/**
	 * Construct the object by opening a JavaMail session. Use getInstance to provide Singleton behavior.
	 */
	public WikiMail() {
		Properties props = System.getProperties();
		props.setProperty("mail.smtp.host", Environment.getValue(Environment.PROP_EMAIL_SMTP_HOST));
		if (Environment.getValue(Environment.PROP_EMAIL_SMTP_USERNAME).equals("")) {
			session = Session.getInstance(props, null);
		} else {
			session = Session.getInstance(props, new WikiMailAuthenticator());
		}
	}

	/**
	 * Provide a Singleton instance of the object.
	 */
	public static WikiMail getInstance() {
		if (instance == null) instance = new WikiMail();
		return instance;
	}

	/**
	 * Send mail via SMTP. MessagingExceptions are silently dropped.
	 *
	 * @param from the RFC 821 "MAIL FROM" parameter
	 * @param to the RFC 821 "RCPT TO" parameter
	 * @param subject the RFC 822 "Subject" field
	 * @param body the RFC 822 "Body" field
	 */
	public void sendMail(String from, String to, String subject, String body) {
		try {
			MimeMessage message = new MimeMessage(session);
			InternetAddress internetAddress = new InternetAddress(from);
			message.setFrom(internetAddress);
			message.setReplyTo(new InternetAddress[]{internetAddress});
			message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
			message.setSubject(subject);
			message.setText(body);
			message.setSentDate(new Date());
			message.saveChanges();
			Transport.send(message);
		} catch (MessagingException e) {
			logger.warn("Mail error", e);
		}
	}

	/**
	 *
	 */
	public static void init() {
		instance = null;
	}
}
