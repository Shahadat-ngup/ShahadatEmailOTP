package main.java.com.example.keycloak.keycloak;

import org.keycloak.email.EmailException;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;

import java.util.HashMap;
import java.util.Map;

public class EmailService {

    private final KeycloakSession session;

    public EmailService(KeycloakSession session) {
        this.session = session;
    }

    public void sendOtpEmail(UserModel user, String otp) throws EmailException {
        EmailTemplateProvider emailProvider = session.getProvider(EmailTemplateProvider.class);
        emailProvider.setRealm(session.getContext().getRealm());
        emailProvider.setUser(user);
        
        String subject = "Your One-Time Password (OTP)";
        
        // Create template attributes
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("otp", otp);
        attributes.put("username", user.getUsername());
        
        try {
            // Correct send method signature:
            // void send(String subject, String template, Map<String, Object> attributes) throws EmailException
            emailProvider.send(
                subject, 
                "email-otp-template",  // This should match your template name
                attributes
            );
        } catch (Exception e) {
            throw new EmailException("Failed to send OTP email", e);
        }
    }
}