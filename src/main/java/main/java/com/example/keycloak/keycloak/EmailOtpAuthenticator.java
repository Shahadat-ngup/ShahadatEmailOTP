package main.java.com.example.keycloak.keycloak;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.events.Errors;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.validation.Validation;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import java.util.UUID;

public class EmailOtpAuthenticator implements Authenticator {

    private final KeycloakSession session;
    private final EmailService emailService;

    public EmailOtpAuthenticator(KeycloakSession session) {
        this.session = session;
        this.emailService = new EmailService(session);
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        UserModel user = context.getUser();
        
        if (user == null) {
            context.failure(AuthenticationFlowError.UNKNOWN_USER);
            return;
        }

        String email = user.getEmail();
        if (email == null || email.trim().isEmpty()) {
            context.getEvent().error(Errors.INVALID_EMAIL);
            Response challenge = context.form()
                    .setError(Messages.INVALID_EMAIL)
                    .createForm(EmailOtpForm.TEMPLATE);
            context.failureChallenge(AuthenticationFlowError.INVALID_USER, challenge);
            return;
        }

        // Generate OTP
        String otp = generateOtp();
        
        // Store OTP in user session note
        context.getAuthenticationSession().setAuthNote("EMAIL_OTP", otp);
        
        // Send OTP via email
        try {
            emailService.sendOtpEmail(user, otp);
        } catch (Exception e) {
            context.getEvent().error(Errors.EMAIL_SEND_FAILED);
            Response challenge = context.form()
                    .setError("Failed to send OTP email. Please try again later.")
                    .createForm(EmailOtpForm.TEMPLATE);
            context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR, challenge);
            return;
        }

        // Show OTP form
        Response challenge = context.form()
                .setAttribute("username", user.getUsername())
                .createForm(EmailOtpForm.TEMPLATE);
        context.challenge(challenge);
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        
        if (formData.containsKey("resend")) {
            // Resend OTP
            authenticate(context);
            return;
        }

        String enteredOtp = formData.getFirst("otp");
        String expectedOtp = context.getAuthenticationSession().getAuthNote("EMAIL_OTP");

        if (expectedOtp == null || !expectedOtp.equals(enteredOtp)) {
            context.getEvent().error(Errors.INVALID_USER_CREDENTIALS);
            Response challenge = context.form()
                    .setAttribute("username", context.getUser().getUsername())
                    .setError(Messages.INVALID_ACCESS_CODE)
                    .createForm(EmailOtpForm.TEMPLATE);
            context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
            return;
        }

        // OTP is valid
        context.success();
    }

    private String generateOtp() {
        // Generate a 6-digit numeric OTP
        return String.format("%06d", UUID.randomUUID().getMostSignificantBits() & 0xFFFFFFFL);
    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return user.getEmail() != null && !user.getEmail().trim().isEmpty();
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        // No required actions needed
    }

    @Override
    public void close() {
        // No resources to close
    }
}