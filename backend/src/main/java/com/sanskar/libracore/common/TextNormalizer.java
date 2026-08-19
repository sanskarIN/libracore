package com.sanskar.libracore.common;

import java.text.Normalizer;
import java.util.Locale;

public final class TextNormalizer {
    private TextNormalizer() {
    }

    public static String display(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFKC)
                .trim()
                .replaceAll("\\s+", " ");
    }

    public static String key(String value) {
        return display(value).toLowerCase(Locale.ROOT);
    }
}
