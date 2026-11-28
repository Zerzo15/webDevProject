package com.example.webApplication.Service;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.example.webApplication.Exception.EmailDeliveryException;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Only place that talks to the mail server. OtpService hands it an email and a
 * raw code, this class turns that into a message and sends it.
 */
@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String mailFrom;
    private final String resendApiKey;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${app.mail.from:no-reply@localhost}") String mailFrom,
            @Value("${app.mail.resend-api-key:}") String resendApiKey) {
        this.mailSender = mailSender;
        this.mailFrom = mailFrom;
        this.resendApiKey = resendApiKey;
    }

    public void sendVerificationEmail(String toEmail, String otpCode, int expiryMinutes) {
        // no credentials configured yet -> print the code so local dev can still verify
        if (resendApiKey == null || resendApiKey.isBlank()) {
            logger.warn("[DEV FALLBACK] RESEND_API_KEY not configured. OTP for {}: {} (expires in {} min)",
                toEmail, otpCode, expiryMinutes);
            return;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, StandardCharsets.UTF_8.name());

            helper.setTo(toEmail);
            helper.setFrom(mailFrom, "WELO");
            helper.setSubject("Your WELO verification code");
            helper.setText(buildBody(otpCode, expiryMinutes), false);

            mailSender.send(mimeMessage);
            logger.info("Verification email sent to {}", toEmail);
        } catch (MessagingException | MailException | java.io.UnsupportedEncodingException e) {
            logger.error("Failed to send verification email to {}: {}", toEmail, e.getMessage());
            throw new EmailDeliveryException("Failed to send verification email. Please request a new code.", e);
        }
    }

    private String buildBody(String otpCode, int expiryMinutes) {
        return "Your WELO verification code is: " + otpCode
            + "\n\nIt expires in " + expiryMinutes + " minutes."
            + "\n\nIf you did not create a WELO account, you can ignore this email.";
    }
}
