package com.sanskar.libracore.exchange;

import com.sanskar.libracore.common.ApiException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CsvCodecTest {
    @Test
    void roundTripsQuotedCells() {
        String csv = CsvCodec.row(List.of("title", "description"))
                + CsvCodec.row(List.of("A, B", "He said \"hello\"\nnext line"));

        List<List<String>> rows = CsvCodec.parse(csv);

        assertEquals(List.of("title", "description"), rows.get(0));
        assertEquals(List.of("A, B", "He said \"hello\"\nnext line"), rows.get(1));
    }

    @Test
    void rejectsUnclosedQuote() {
        ApiException exception = assertThrows(ApiException.class, () -> CsvCodec.parse("a,b\n\"broken"));
        assertEquals("csv_unclosed_quote", exception.code());
    }

    @Test
    void rejectsNulCharacters() {
        ApiException exception = assertThrows(ApiException.class, () -> CsvCodec.parse("a\0b"));
        assertEquals("csv_invalid_character", exception.code());
    }
}
