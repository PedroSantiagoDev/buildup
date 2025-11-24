package com.maistech.buildup.auth.utils;

import java.security.SecureRandom;

public class CodeGenerator {

    private static final SecureRandom random = new SecureRandom();

    public static String generateVerificationCode() {
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    public static String generateVerificationCode(int length) {
        if (length <= 0 || length > 10) {
            throw new IllegalArgumentException("Length must be between 1 and 10");
        }

        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(random.nextInt(10));
        }

        if (code.charAt(0) == '0' && length > 1) {
            code.setCharAt(0, (char) ('1' + random.nextInt(9)));
        }

        return code.toString();
    }
}
