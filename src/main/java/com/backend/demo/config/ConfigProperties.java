package com.backend.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationProperties(prefix = "custom")
@ConfigurationPropertiesScan
public class ConfigProperties {
    private String authenticationSecret;
    private String verificationSecret;
    private String passwordRecoverySecret;

    private String allowedOrigin;

    public ConfigProperties() {
    }

    public ConfigProperties(String authenticationSecret, String verificationSecret,
                            String passwordRecoverySecret, String allowedOrigin) {
        this.authenticationSecret = authenticationSecret;
        this.verificationSecret = verificationSecret;
        this.passwordRecoverySecret = passwordRecoverySecret;
        this.allowedOrigin = allowedOrigin;
    }

    public String getAuthenticationSecret() {
        return authenticationSecret;
    }

    public void setAuthenticationSecret(String authenticationSecret) {
        this.authenticationSecret = authenticationSecret;
    }

    public String getVerificationSecret() {
        return verificationSecret;
    }

    public void setVerificationSecret(String verificationSecret) {
        this.verificationSecret = verificationSecret;
    }

    public String getPasswordRecoverySecret() {
        return passwordRecoverySecret;
    }

    public void setPasswordRecoverySecret(String passwordRecoverySecret) {
        this.passwordRecoverySecret = passwordRecoverySecret;
    }

    public String getAllowedOrigin() {
        return allowedOrigin;
    }

    public void setAllowedOrigin(String allowedOrigin) {
        this.allowedOrigin = allowedOrigin;
    }
}
