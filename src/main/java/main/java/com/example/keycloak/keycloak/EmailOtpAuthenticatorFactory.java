package main.java.com.example.keycloak.keycloak;

import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.ArrayList;
import java.util.List;

public class EmailOtpAuthenticatorFactory implements AuthenticatorFactory {

    public static final String PROVIDER_ID = "email-otp-authenticator";
    private static final EmailOtpAuthenticator SINGLETON = new EmailOtpAuthenticator(null);

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayType() {
        return "Email OTP Authentication";
    }

    @Override
    public String getHelpText() {
        return "Authenticates users by sending a One-Time Password (OTP) to their registered email address.";
    }

    @Override
    public String getReferenceCategory() {
        return "otp";
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    public static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.DISABLED
    };

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        List<ProviderConfigProperty> configProperties = new ArrayList<>();

        ProviderConfigProperty property;
        
        property = new ProviderConfigProperty();
        property.setName("otp.length");
        property.setLabel("OTP Length");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("Length of the OTP code (default: 6)");
        configProperties.add(property);

        property = new ProviderConfigProperty();
        property.setName("otp.ttl");
        property.setLabel("OTP Time-to-Live");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("OTP validity duration in seconds (default: 300)");
        configProperties.add(property);

        property = new ProviderConfigProperty();
        property.setName("email.subject");
        property.setLabel("Email Subject");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("Subject for the OTP email");
        configProperties.add(property);

        property = new ProviderConfigProperty();
        property.setName("email.from");
        property.setLabel("Email From Address");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("Sender email address");
        configProperties.add(property);

        return configProperties;
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return new EmailOtpAuthenticator(session);
    }

    @Override
    public void init(org.keycloak.Config.Scope config) {
        // Initialize any default configuration
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // Post initialization if needed
    }

    @Override
    public void close() {
        // Close resources if any
    }
}