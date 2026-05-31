package com.example.FinalFileSharing.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.example.FinalFileSharing.model.FileShare;

@Component
public class MailShareEmailSender implements ShareEmailSender {

	private static final Logger log = LoggerFactory.getLogger(MailShareEmailSender.class);

	private final ObjectProvider<JavaMailSender> mailSender;
	private final boolean mailEnabled;
	private final String fromAddress;

	public MailShareEmailSender(ObjectProvider<JavaMailSender> mailSender,
			@Value("${app.mail.enabled:false}") boolean mailEnabled,
			@Value("${app.mail.from:no-reply@filesharing.local}") String fromAddress) {
		this.mailSender = mailSender;
		this.mailEnabled = mailEnabled;
		this.fromAddress = fromAddress;
	}

	@Override
	public void sendShareEmail(FileShare share, String shareUrl) {
		if (!mailEnabled) {
			log.info("Mail disabled. Share email to {} for file '{}'. Message: {} Link: {}",
					share.getRecipientEmail(),
					share.getStoredFile().getOriginalFilename(),
					share.getMessage(),
					shareUrl);
			return;
		}

		JavaMailSender sender = mailSender.getIfAvailable();
		if (sender == null) {
			log.warn("Mail is enabled, but no JavaMailSender is configured. Share link for {}: {}",
					share.getRecipientEmail(),
					shareUrl);
			return;
		}

		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(fromAddress);
		message.setTo(share.getRecipientEmail());
		message.setSubject("A file has been shared with you");
		message.setText("""
				%s

				File: %s
				Link: %s
				Expires: %s
				""".formatted(
				share.getMessage(),
				share.getStoredFile().getOriginalFilename(),
				shareUrl,
				share.getExpiresAt()));
		sender.send(message);
	}
}
