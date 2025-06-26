package main.utils;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class OTPGenerator {
    private static final String DIGITS = "0123456789";
    private static final int OTP_LENGTH = 6;
    private static final SecureRandom random = new SecureRandom();

    /**
     * Generate a random 6-digit OTP
     * @return String containing 6-digit OTP
     */
    public static String generateOTP() {
        StringBuilder otp = new StringBuilder();

        for (int i = 0; i < OTP_LENGTH; i++) {
            int randomIndex = random.nextInt(DIGITS.length());
            otp.append(DIGITS.charAt(randomIndex));
        }

        String generatedOtp = otp.toString();
        System.out.println("Generated OTP: " + generatedOtp + " at " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        return generatedOtp;
    }

    /**
     * Generate OTP with custom length
     * @param length Length of OTP to generate
     * @return String containing OTP of specified length
     */
    public static String generateOTP(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("OTP length must be greater than 0");
        }

        StringBuilder otp = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(DIGITS.length());
            otp.append(DIGITS.charAt(randomIndex));
        }

        return otp.toString();
    }

    /**
     * Generate alphanumeric OTP
     * @return String containing 6-character alphanumeric OTP
     */
    public static String generateAlphanumericOTP() {
        String alphanumeric = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder otp = new StringBuilder();

        for (int i = 0; i < OTP_LENGTH; i++) {
            int randomIndex = random.nextInt(alphanumeric.length());
            otp.append(alphanumeric.charAt(randomIndex));
        }

        return otp.toString();
    }

    /**
     * Validate OTP format
     * @param otp OTP string to validate
     * @return true if OTP format is valid, false otherwise
     */
    public static boolean isValidOTPFormat(String otp) {
        if (otp == null || otp.trim().isEmpty()) {
            return false;
        }

        String trimmedOtp = otp.trim();

        // Check if length is correct
        if (trimmedOtp.length() != OTP_LENGTH) {
            return false;
        }

        // Check if all characters are digits
        return trimmedOtp.matches("\\d{" + OTP_LENGTH + "}");
    }

    /**
     * Generate OTP with timestamp for logging purposes
     * @return OTPData object containing OTP and generation timestamp
     */
    public static OTPData generateOTPWithTimestamp() {
        String otp = generateOTP();
        LocalDateTime timestamp = LocalDateTime.now();
        return new OTPData(otp, timestamp);
    }

    /**
     * Inner class to hold OTP data with timestamp
     */
    public static class OTPData {
        private final String otp;
        private final LocalDateTime generatedAt;

        public OTPData(String otp, LocalDateTime generatedAt) {
            this.otp = otp;
            this.generatedAt = generatedAt;
        }

        public String getOtp() {
            return otp;
        }

        public LocalDateTime getGeneratedAt() {
            return generatedAt;
        }

        public boolean isExpired(int validityMinutes) {
            return LocalDateTime.now().isAfter(generatedAt.plusMinutes(validityMinutes));
        }

        @Override
        public String toString() {
            return "OTPData{" +
                    "otp='" + otp + '\'' +
                    ", generatedAt=" + generatedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) +
                    '}';
        }
    }
}