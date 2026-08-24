package com.sanskar.libracore.exchange;

import com.sanskar.libracore.common.ApiException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
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
    void streamsRowsWithoutMaterializingTheWholeDocument() {
        List<Integer> rowNumbers = new ArrayList<>();
        List<List<String>> rows = new ArrayList<>();

        CsvCodec.forEachRow(
                new StringReader("title,description\r\nBook,\"line one\nline two\"\r\n"),
                (rowNumber, row) -> {
                    rowNumbers.add(rowNumber);
                    rows.add(row);
                }
        );

        assertEquals(List.of(1, 2), rowNumbers);
        assertEquals(List.of("title", "description"), rows.get(0));
        assertEquals(List.of("Book", "line one\nline two"), rows.get(1));
    }

    @Test
    void treatsCrLfAsOneRowBoundary() {
        List<List<String>> rows = CsvCodec.parse("a,b\r\nc,d\r\n");

        assertEquals(2, rows.size());
        assertEquals(List.of("a", "b"), rows.get(0));
        assertEquals(List.of("c", "d"), rows.get(1));
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

    @Test
    void rejectsInputsAboveCharacterBudget() {
        String line = "a".repeat(400) + "\n";
        String csv = line.repeat((CsvCodec.MAX_INPUT_CHARS / line.length()) + 2);

        ApiException exception = assertThrows(ApiException.class, () -> CsvCodec.parse(csv));

        assertEquals("csv_too_large", exception.code());
    }

    @Test
    void rejectsDocumentsAboveRowBudget() {
        String csv = "a\n".repeat(CsvCodec.MAX_ROWS + 1);

        ApiException exception = assertThrows(ApiException.class, () -> CsvCodec.parse(csv));

        assertEquals("csv_too_many_rows", exception.code());
    }

    @Test
    void rejectsRowsAboveColumnBudget() {
        String csv = ",".repeat(CsvCodec.MAX_COLUMNS);

        ApiException exception = assertThrows(ApiException.class, () -> CsvCodec.parse(csv));

        assertEquals("csv_too_many_columns", exception.code());
    }

    @Test
    void rejectsCellsAboveCharacterBudget() {
        String csv = "a".repeat(CsvCodec.MAX_CELL_CHARS + 1);

        ApiException exception = assertThrows(ApiException.class, () -> CsvCodec.parse(csv));

        assertEquals("csv_cell_too_large", exception.code());
    }

    @Test
    void mapsReaderFailuresToStableApiError() {
        Reader brokenReader = new Reader() {
            @Override
            public int read(char[] buffer, int offset, int length) throws IOException {
                throw new IOException("synthetic read failure");
            }

            @Override
            public void close() {
            }
        };

        ApiException exception = assertThrows(
                ApiException.class,
                () -> CsvCodec.forEachRow(brokenReader, (rowNumber, row) -> {
                })
        );

        assertEquals("csv_read_failed", exception.code());
    }
}
