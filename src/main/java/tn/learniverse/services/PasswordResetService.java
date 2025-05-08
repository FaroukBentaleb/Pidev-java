package tn.learniverse.services;

import org.mindrot.jbcrypt.BCrypt;
import tn.learniverse.entities.PasswordResetToken;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.time.LocalDateTime;

/**
 * Service class to handle password reset functionality
 */
public class PasswordResetService {
    // In-memory storage for reset tokens
    // In a production app, this should be stored in a database
    private static final Map<String, PasswordResetToken> resetTokens = new HashMap<>();

    /**
     * Generate a unique reset token
     * @return The generated token
     */
    public static String generateToken() {
        return UUID.randomUUID().toString();
    }

    /**
     * Create a new password reset token for a user
     * @param email The email of the user
     * @return The generated token
     */
    public static String createResetToken(String email) {
        String token = generateToken();

        // Token expires in 24 hours
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(24);

        // Store token in the map
        resetTokens.put(token, new PasswordResetToken(token, email, expiryDate));

        return token;
    }

    /**
     * Validate a password reset token
     * @param token The token to validate
     * @return The user's email if the token is valid, null otherwise
     */
    public static String validateToken(String token) {
        PasswordResetToken resetToken = resetTokens.get(token);

        if (resetToken == null) {
            return null;
        }

        // Check if token is expired
        if (resetToken.isExpired()) {
            resetTokens.remove(token);
            return null;
        }

        return resetToken.getUserEmail();
    }

    /**
     * Invalidate a token after it has been used
     * @param token The token to invalidate
     */
    public static void invalidateToken(String token) {
        resetTokens.remove(token);
    }

    /**
     * Hash a password using SHA-256
     * @param password The password to hash
     * @return The hashed password
     */
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
}