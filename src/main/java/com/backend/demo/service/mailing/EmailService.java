package com.backend.demo.service.mailing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Component
public class EmailService {
    @Autowired
    private JavaMailSender emailSender;


    public void sendSimpleMessage(
            String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("ivandax_89@hotmail.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
    }

    public void sendHTMLMessage(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage mimeMessage = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
        helper.setText(htmlContent, true);
        helper.setFrom("info@culturetxt.com");
        helper.setTo(to);
        helper.setSubject(subject);
        emailSender.send(mimeMessage);
    }

    public void sendUserVerificationTokenMessage(String to, String subject,
                                                 String verificationToken) throws MessagingException {
        String htmlContent = "<h3>Hello from taskmaster!</h3><p>Follow this <a" +
                " href=\"http://localhost:3000"
                + "/verify-token/"
                + verificationToken
                + "\" target=\"_blank\">link</a> to verify your account</p>";
        sendHTMLMessage(to, subject, htmlContent);
    }

    public void setPasswordRecoveryMessage(String to, String subject,
                                           String passwordRecoveryToken) throws MessagingException {
        String htmlContent = "<h3>Hello from taskmaster!</h3><p>Follow this <a" +
                " href=\"http://localhost:3000"
                + "/set-new-password/"
                + passwordRecoveryToken
                + "\" target=\"_blank\">link</a> to reset your password</p>";
        sendHTMLMessage(to, subject, htmlContent);
    }

    public void sendUserCreatedMessage(String to, String subject,
                                       String verificationToken,
                                       String temporaryPassword) throws MessagingException {
        String htmlContent = "<h3>An account has been created for you</h3><p>Follow this <a" +
                " href=\"http://localhost:3000"
                + "/verify-token/"
                + verificationToken
                + "\" target=\"_blank\">link</a> to verify your account</p>" + "<p>You " +
                "can then login with this temporary Password: " + temporaryPassword + "</p>";
        sendHTMLMessage(to, subject, htmlContent);
    }
}
