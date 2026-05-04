package com.classmanager.cms_backend.util;

import java.security.SecureRandom;

public final class PasswordUtils {

    private static final String UPPER   = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String LOWER   = "abcdefghjkmnpqrstuvwxyz";
    private static final String DIGITS  = "23456789";
    private static final String SPECIAL = "@#$!";
    private static final String ALL     = UPPER + LOWER + DIGITS + SPECIAL;

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtils() {}

    public static String generate(int length) {
        if (length < 8) throw new IllegalArgumentException("Minimum password length is 8");

        char[] password = new char[length];
        // Guarantee at least one from each character class
        password[0] = UPPER.charAt(RANDOM.nextInt(UPPER.length()));
        password[1] = LOWER.charAt(RANDOM.nextInt(LOWER.length()));
        password[2] = DIGITS.charAt(RANDOM.nextInt(DIGITS.length()));
        password[3] = SPECIAL.charAt(RANDOM.nextInt(SPECIAL.length()));

        for (int i = 4; i < length; i++) {
            password[i] = ALL.charAt(RANDOM.nextInt(ALL.length()));
        }

        // Shuffle to avoid predictable position of guaranteed chars
        for (int i = length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char tmp = password[i];
            password[i] = password[j];
            password[j] = tmp;
        }

        return new String(password);
    }

    /** Generates a default 10-character password for new auto-created users. */
    public static String generateDefault() {
        return generate(10);
    }

    /** Validates password complexity for user-set passwords. */
    public static boolean isStrong(String password) {
        if (password == null || password.length() < 8) return false;
        boolean hasUpper   = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower   = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit   = password.chars().anyMatch(Character::isDigit);
        return hasUpper && hasLower && hasDigit;
    }
}
