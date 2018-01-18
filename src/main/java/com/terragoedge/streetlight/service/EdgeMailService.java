package com.terragoedge.streetlight.service;

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
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;

import com.terragoedge.streetlight.PropertiesReader;

public class EdgeMailService {

	final Logger logger = Logger.getLogger(EdgeMailService.class);
	
	Properties properties = null;

	public EdgeMailService() {
		properties = PropertiesReader.getProperties();
	}

	public void sendMail(String dupMacAddressFile,String dailyReportFile) {
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
			message.setSubject("Daily Report - Automated");

			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setText("Please find attached the csv with the today's data.");

			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);

			messageBodyPart = new MimeBodyPart();

			DataSource source = new FileDataSource("./report/" + dailyReportFile);
			messageBodyPart.setDataHandler(new DataHandler(source));
			messageBodyPart.setFileName(dailyReportFile);
			multipart.addBodyPart(messageBodyPart);

			if (dupMacAddressFile != null) {
				source = new FileDataSource("./report/" + dupMacAddressFile);
				messageBodyPart.setDataHandler(new DataHandler(source));
				messageBodyPart.setFileName(dupMacAddressFile);
				multipart.addBodyPart(messageBodyPart);
			}

			message.setContent(multipart);

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

	

}
