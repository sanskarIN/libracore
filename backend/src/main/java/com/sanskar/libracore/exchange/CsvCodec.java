package com.sanskar.libracore.exchange;

import com.sanskar.libracore.common.ApiException;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class CsvCodec {
    public static final int MAX_INPUT_CHARS = 2_000_000;
    public static final int MAX_ROWS = 10_000;
    public static final int MAX_COLUMNS = 64;
    public static final int MAX_CELL_CHARS = 20_000;

    private CsvCodec() {
    }

    @FunctionalInterface
    public interface RowConsumer {
        void accept(int rowNumber, List<String> row);
    }

    public static List<List<String>> parse(String input) {
        if (input == null) {
            throw ApiException.badRequest("csv_missing", "CSV content is required.");
        }
        if (input.length() > MAX_INPUT_CHARS) {
            throw ApiException.badRequest("csv_too_large", "CSV content exceeds the supported import size.");
        }

        List<List<String>> rows = new ArrayList<>();
        forEachRow(new StringReader(input), (rowNumber, row) -> rows.add(row));
        return List.copyOf(rows);
    }

    public static void forEachRow(Reader input, RowConsumer consumer) {
        if (input == null) {
            throw ApiException.badRequest("csv_missing", "CSV content is required.");
        }
        Objects.requireNonNull(consumer, "consumer");

        try {
            new StreamingParser(input, consumer).parse();
        } catch (IOException exception) {
            throw ApiException.badRequest("csv_read_failed", "CSV content could not be read.");
        }
    }

    public static String row(List<?> values) {
        StringBuilder output = new StringBuilder();
        for (int index = 0; index < values.size(); index++) {
            if (index > 0) {
                output.append(',');
            }
            output.append(escape(values.get(index) == null ? "" : String.valueOf(values.get(index))));
        }
        output.append('\n');
        return output.toString();
    }

    public static String escape(String value) {
        String safe = value == null ? "" : value;
        boolean quote = safe.indexOf(',') >= 0 || safe.indexOf('"') >= 0
                || safe.indexOf('\r') >= 0 || safe.indexOf('\n') >= 0;
        if (!quote) {
            return safe;
        }
        return '"' + safe.replace("\"", "\"\"") + '"';
    }

    private static void appendCellChar(StringBuilder cell, char ch) {
        if (cell.length() >= MAX_CELL_CHARS) {
            throw ApiException.badRequest("csv_cell_too_large", "A CSV cell exceeds the supported size.");
        }
        cell.append(ch);
    }

    private static void addCell(List<String> row, StringBuilder cell) {
        if (row.size() >= MAX_COLUMNS) {
            throw ApiException.badRequest("csv_too_many_columns", "CSV contains too many columns.");
        }
        row.add(cell.toString());
        cell.setLength(0);
    }

    private static final class StreamingParser {
        private final Reader input;
        private final RowConsumer consumer;
        private List<String> row = new ArrayList<>();
        private final StringBuilder cell = new StringBuilder();
        private int inputChars;
        private int emittedRows;
        private boolean quoted;

        private StreamingParser(Reader input, RowConsumer consumer) {
            this.input = input;
            this.consumer = consumer;
        }

        private void parse() throws IOException {
            int value;
            while ((value = read()) != -1) {
                char ch = (char) value;
                rejectNul(ch);

                if (quoted) {
                    if (ch != '"') {
                        appendCellChar(cell, ch);
                        continue;
                    }

                    int next = read();
                    if (next == '"') {
                        appendCellChar(cell, '"');
                        continue;
                    }

                    quoted = false;
                    if (next == -1) {
                        break;
                    }
                    char nextChar = (char) next;
                    rejectNul(nextChar);
                    processUnquoted(nextChar);
                    continue;
                }

                processUnquoted(ch);
            }

            if (quoted) {
                throw ApiException.badRequest("csv_unclosed_quote", "CSV contains an unclosed quoted field.");
            }
            if (!row.isEmpty() || !cell.isEmpty()) {
                finishRow();
            }
        }

        private int read() throws IOException {
            int value = input.read();
            if (value == -1) {
                return -1;
            }
            inputChars++;
            if (inputChars > MAX_INPUT_CHARS) {
                throw ApiException.badRequest("csv_too_large", "CSV content exceeds the supported import size.");
            }
            return value;
        }

        private void processUnquoted(char ch) {
            if (ch == '"') {
                if (!cell.isEmpty()) {
                    throw ApiException.badRequest("csv_invalid_quote", "A quoted field must begin at the start of a cell.");
                }
                quoted = true;
            } else if (ch == ',') {
                addCell(row, cell);
            } else if (ch == '\n') {
                finishRow();
            } else if (ch == '\r') {
                finishRow();
            } else {
                appendCellChar(cell, ch);
            }
        }

        private void finishRow() {
            addCell(row, cell);
            List<String> completed = List.copyOf(row);
            row = new ArrayList<>();

            boolean blank = completed.stream().allMatch(String::isBlank);
            if (blank && emittedRows == 0) {
                return;
            }
            if (emittedRows >= MAX_ROWS) {
                throw ApiException.badRequest("csv_too_many_rows", "CSV contains too many rows.");
            }
            emittedRows++;
            consumer.accept(emittedRows, completed);
        }

        private static void rejectNul(char ch) {
            if (ch == '\0') {
                throw ApiException.badRequest("csv_invalid_character", "CSV content contains an invalid NUL character.");
            }
        }
    }
}
