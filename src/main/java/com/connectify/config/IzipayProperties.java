package com.connectify.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "izipay")
public class IzipayProperties {
    private boolean enabled;
    private String mode = "TEST";
    private String endpoint;
    private String username;
    private String password;
    private String publicKey;
    private String hmacSha256Key;
    private String smartFormScriptUrl;
    private String successUrl;
    private String errorUrl;
    private String cancelUrl;
    private String notificationUrl;
    private String currency = "PEN";

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPublicKey() { return publicKey; }
    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }

    public String getHmacSha256Key() { return hmacSha256Key; }
    public void setHmacSha256Key(String hmacSha256Key) { this.hmacSha256Key = hmacSha256Key; }

    public String getSmartFormScriptUrl() { return smartFormScriptUrl; }
    public void setSmartFormScriptUrl(String smartFormScriptUrl) { this.smartFormScriptUrl = smartFormScriptUrl; }

    public String getSuccessUrl() { return successUrl; }
    public void setSuccessUrl(String successUrl) { this.successUrl = successUrl; }

    public String getErrorUrl() { return errorUrl; }
    public void setErrorUrl(String errorUrl) { this.errorUrl = errorUrl; }

    public String getCancelUrl() { return cancelUrl; }
    public void setCancelUrl(String cancelUrl) { this.cancelUrl = cancelUrl; }

    public String getNotificationUrl() { return notificationUrl; }
    public void setNotificationUrl(String notificationUrl) { this.notificationUrl = notificationUrl; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
