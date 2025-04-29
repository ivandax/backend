package com.backend.demo.service.mailing;

import com.backend.demo.config.ConfigProperties;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.resend.*;

@Component
public class ResendEmailService {

    @Value("${resend.sender}")
    private String sender;

    private final Resend resend;

    @Autowired
    private ConfigProperties configProperties;

    public ResendEmailService(@Value("${resend.api.key}") String apiKey) {
        this.resend = new Resend(apiKey);
    }

    public void sendHTMLMessage(String to, String subject, String htmlContent) {
        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(sender)
                .to(to)
                .subject(subject)
                .html(htmlContent)
                .build();

        try {
            CreateEmailResponse data = resend.emails().send(params);
            System.out.println("Email sent with ID: " + data.getId());
        } catch (ResendException e) {
            e.printStackTrace();
        }
    }

    public void sendUserVerificationTokenMessage(String to, String subject,
                                                 String verificationToken) {
        String allowedOrigin = configProperties.getAllowedOrigin();
        String htmlContent = "<h3>Hello from Todolist app!</h3><p>Follow this <a" +
                " href=\"" + allowedOrigin + "/verify-token/" + verificationToken +
                "\" target=\"_blank\">link</a> to verify your account</p>";
        sendHTMLMessage(to, subject, htmlContent);
    }

    public void sendPasswordRecoveryMessage(String to, String subject,
                                            String passwordRecoveryToken) {
        String allowedOrigin = configProperties.getAllowedOrigin();
        String htmlContent = "<h3>Hello from Todolist!</h3><p>Follow this <a" +
                " href=\"" + allowedOrigin + "/set-new-password/" + passwordRecoveryToken +
                "\" target=\"_blank\">link</a> to reset your password</p>";
        sendHTMLMessage(to, subject, htmlContent);
    }

    public void sendTestMessage(String toEmail) {
        String htmlContent = "<h3>This is a test message</h3>"
                + "<p>This is just some placeholder test</p>";

        sendHTMLMessage(toEmail, "Test message from Todolist app", htmlContent);
    }
}
