package tn.learniverse.entities;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

public class TimeBasedOneTimePasswordGenerator {

    private static final int TIME_STEP_SECONDS = 30;
    private static final String HMAC_ALGORITHM = "HmacSHA1";
    private static final int OTP_LENGTH = 6;

    public int generateOneTimePassword(SecretKey secretKey, Instant time) throws NoSuchAlgorithmException, InvalidKeyException {
        long timeWindow = time.getEpochSecond() / TIME_STEP_SECONDS;
        byte[] timeBytes = longToBytes(timeWindow);

        Mac hmac = Mac.getInstance(HMAC_ALGORITHM);
        hmac.init(secretKey);
        byte[] hash = hmac.doFinal(timeBytes);

        // Extract the OTP from the hash (using dynamic truncation)
        int offset = hash[19] & 0xF;
        int binary = ((hash[offset] & 0x7f) << 24) | ((hash[offset + 1] & 0xff) << 16) |
                ((hash[offset + 2] & 0xff) << 8) | (hash[offset + 3] & 0xff);

        // Modulo the binary value to get a 6-digit code
        int otp = binary % (int) Math.pow(10, OTP_LENGTH);
        return otp;
    }

    private byte[] longToBytes(long value) {
        byte[] bytes = new byte[8];
        for (int i = 0; i < 8; i++) {
            bytes[7 - i] = (byte) (value & 0xff);
            value >>= 8;
        }
        return bytes;
    }
}
