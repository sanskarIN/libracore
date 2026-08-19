package com.sanskar.libracore.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextNormalizerTest {
    @Test
    void collapsesWhitespaceAndNormalizesCompatibilityCharacters() {
        assertEquals("Hello World", TextNormalizer.display("  Hello\t\nWorld  "));
        assertEquals("abc 123", TextNormalizer.key(" ＡＢＣ  123 "));
    }
}
