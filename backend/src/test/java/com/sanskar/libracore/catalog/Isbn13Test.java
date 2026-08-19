package com.sanskar.libracore.catalog;

import com.sanskar.libracore.common.ApiException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Isbn13Test {
    @Test
    void acceptsAndCanonicalizesValidIsbn13() {
        assertEquals("9780306406157", Isbn13.normalizeNullable("978-0-306-40615-7"));
    }

    @Test
    void acceptsMissingOptionalIsbn() {
        assertNull(Isbn13.normalizeNullable("   "));
    }

    @Test
    void rejectsInvalidChecksum() {
        ApiException exception = assertThrows(ApiException.class,
                () -> Isbn13.normalizeNullable("9780306406158"));
        assertEquals("invalid_isbn13_checksum", exception.code());
    }

    @Test
    void rejectsNonDigitsAfterCanonicalSeparators() {
        ApiException exception = assertThrows(ApiException.class,
                () -> Isbn13.normalizeNullable("97803064061X7"));
        assertEquals("invalid_isbn13", exception.code());
    }
}
