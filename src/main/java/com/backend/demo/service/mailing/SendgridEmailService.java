package com.backend.demo.service.mailing;

import com.backend.demo.config.ConfigProperties;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SendgridEmailService {

    @Value("${spring.mail.password}")
    private String sendGridApiKey;

    @Autowired
    private ConfigProperties configProperties;

    private static final String FROM_EMAIL = "info@culturetxt.com";

    private void sendMail(Mail mail) throws IOException {
        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();

        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());
        Response response = sg.api(request);
        System.out.println("Response status: " + response.getStatusCode());
        System.out.println("Response body: " + response.getBody());
        System.out.println("Response headers: " + response.getHeaders());
    }

    private void sendEmail(String toEmail, String subject, String htmlContent) throws IOException {
        Email from = new Email(FROM_EMAIL);
        Email to = new Email(toEmail);
        Content emailContent = new Content("text/html", htmlContent);
        Mail mail = new Mail(from, subject, to, emailContent);

        sendMail(mail);
    }

    public void sendUserVerificationFromSendgrid(String toEmail, String subject,
                                                 String verificationToken) throws IOException {
        String verificationLink =
                configProperties.getAllowedOrigin() + "/verify-token/" + verificationToken;
        String htmlContent = "<h3>Hello from Todolist app!</h3>"
                + "<p>Follow this <a href=\"" + verificationLink + "\" target=\"_blank\">link</a>" +
                " to verify your account.</p>";

        sendEmail(toEmail, subject, htmlContent);
    }

    public void setPasswordRecoveryMessage(String toEmail, String subject,
                                           String passwordRecoveryToken) throws IOException {
        String recoveryLink =
                configProperties.getAllowedOrigin() + "/set-new-password/" + passwordRecoveryToken;
        String htmlContent = "<h3>Hello from Todolist!</h3>"
                + "<p>Follow this <a href=\"" + recoveryLink + "\" target=\"_blank\">link</a> to " +
                "reset your password.</p>";

        sendEmail(toEmail, subject, htmlContent);
    }

    public void sendTestMessage(String toEmail) throws IOException {
        String htmlContent = "<h3>This is a test message</h3>"
                + "<p>This is just some placeholder test</p>";

        sendEmail(toEmail, "Test message from todolist app", htmlContent);
    }
}
