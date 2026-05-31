package com.example.FinalFileSharing.config;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * Configures a {@link JavaMailSender} bean using Gmail SMTP settings defined in {@code application.properties}.
 * The properties are prefixed with {@code app.mail.smtp} and are injected via {@code @Value}.
 */
@Configuration
public class MailConfig {

    @Value("${app.mail.smtp.host}")
    private String host;

    @Value("${app.mail.smtp.port}")
    private int port;

    @Value("${app.mail.smtp.username}")
    private String username;

    @Value("${app.mail.smtp.password}")
    private String password;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "false");
        return mailSender;
    }
}
