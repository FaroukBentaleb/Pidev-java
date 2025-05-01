package tn.learniverse.entities;

import java.time.LocalDateTime;

public class PasswordResetToken {
    private String token;
    private String userEmail;
    private LocalDateTime expiryDate;

    public PasswordResetToken(String token, String userEmail, LocalDateTime expiryDate) {
        this.token = token;
        this.userEmail = userEmail;
        this.expiryDate = expiryDate;
    }

    public String getToken() {
        return token;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
}