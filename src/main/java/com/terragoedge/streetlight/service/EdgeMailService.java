package com.terragoedge.streetlight.service;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

import com.terragoedge.streetlight.PropertiesReader;

public class EdgeMailService {

	final Logger logger = Logger.getLogger(EdgeMailService.class);
	
	Properties properties = null;

	public EdgeMailService() {
		properties = PropertiesReader.getProperties();
	}

	public void sendMail(String body, String subject) {
		logger.info("Mail Server Triggered");
		Properties props = System.getProperties();
		final String fromEmail = properties.getProperty("email.id");
		final String emailPassword = properties.getProperty("email.password");
		String recipients = properties.getProperty("email.recipients");
		String host = properties.getProperty("email.host");
		String port = properties.getProperty("email.port");
		String[] to = recipients.split(",", -1);
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.user", fromEmail);
		props.put("mail.smtp.password", emailPassword);
		props.put("mail.smtp.port", port);
		props.put("mail.smtp.auth", "true");
		Session session = Session.getInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(fromEmail, emailPassword);
			}
		});
		MimeMessage message = new MimeMessage(session);
		try {
			message.setFrom(new InternetAddress(fromEmail));
			InternetAddress[] toAddress = new InternetAddress[to.length];
			for (int i = 0; i < to.length; i++) {
				toAddress[i] = new InternetAddress(to[i]);
			}
			for (int i = 0; i < toAddress.length; i++) {
				message.addRecipient(Message.RecipientType.TO, toAddress[i]);
			}
			message.setSubject(subject);
			message.setText(body);
			Transport transport = session.getTransport("smtp");
			transport.connect(host, fromEmail, emailPassword);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
			logger.info("Mail Send.");
		} catch (AddressException ae) {
			ae.printStackTrace();
		} catch (Exception me) {
			me.printStackTrace();
			logger.error("Error while sending mail",me);
		}
	}

	public void sendMailInValidSLNumber(final String slNumber) {
		startMailThread("SL number - wrong format",
				"The SL number (" + slNumber + ") of this Pole is not in the expected format. So this (" + slNumber
						+ ") Pole details is skipped and not synced to SLV server.");
	}

	public void sendMailQRCodeMissing(final String slNumber, final String replaceNodeQRCode) {
		startMailThread("QR Code - Missing in Richmond Hill Streetlights",
				"The QR Code ( " + replaceNodeQRCode
						+ " ) is present in Replace Node form but not in Richmond Hill Streetlights form. So this ("
						+ slNumber + ") Pole detail is not synced to SLV server.");
	}

	public void sendMailReplaceOLCsErrorCode(final String slNumber, final String errorCode) {
		startMailThread("ReplaceOLCs Rest Service - Error code response",
				"The service call to replaceOLCs rest service failed. This is the corresponding error code ("
						+ errorCode + ") and pole number (" + slNumber + ")");
	}
	
	
	public void sendMailMacAddressAlreadyUsed(final String macAddress, final String slNumbers) {
		startMailThread("MacAddress - Already in use",
				"The MacAddress ("+macAddress+") is already used by following SLNumbers \n "+slNumbers);
	}

	public void sendMailDeviceNotFound(final String slNumber, final String replaceNodeQrCode,final String richmondQrCode) {
		startMailThread("Device Not Found",
				"The QRcode data is present in both Richmond hill ("+richmondQrCode+") and Replace node ("+replaceNodeQrCode+") forms. But the Device ("+slNumber+") is not present in SLV server.");
	}

	private void startMailThread(final String subject, final String body) {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				sendMail(body,subject);

			}
		});
		thread.start();
	}

}
