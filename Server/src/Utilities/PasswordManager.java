package Utilities;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordManager {
    private static final String CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*";
    public static String generateSalt() {

        StringBuilder salt = new StringBuilder();
        int length = (int) (Math.random() * 6) + 5;
        for (int i = 0; i < length; i++) {
            int randomIndex = (int) (Math.random() * (CHARS.length()-1));
            salt.append(CHARS.charAt(randomIndex));
        }
        return salt.toString();

    }
    public static String hashPassword(String password, String salt) {
        try {
            String passwordWithSalt = password + salt;
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(passwordWithSalt.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b: hash) {
                hexString.append(String.format("%02x", b));
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

}

