package com.sanskar.libracore.catalog;

import com.sanskar.libracore.common.ApiException;

public final class Isbn13 {
    private Isbn13() {
    }

    public static String normalizeNullable(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String digits = raw.replace("-", "").replace(" ", "").trim();
        if (!digits.matches("\\d{13}")) {
            throw ApiException.badRequest("invalid_isbn13", "ISBN-13 must contain exactly 13 digits.");
        }

        int sum = 0;
        for (int index = 0; index < 12; index++) {
            int digit = digits.charAt(index) - '0';
            sum += index % 2 == 0 ? digit : digit * 3;
        }
        int expectedCheckDigit = (10 - (sum % 10)) % 10;
        int actualCheckDigit = digits.charAt(12) - '0';
        if (expectedCheckDigit != actualCheckDigit) {
            throw ApiException.badRequest("invalid_isbn13_checksum", "ISBN-13 checksum is invalid.");
        }
        return digits;
    }
}
