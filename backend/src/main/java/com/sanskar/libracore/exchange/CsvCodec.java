package com.sanskar.libracore.exchange;

import com.sanskar.libracore.common.ApiException;

import java.util.ArrayList;
import java.util.List;

public final class CsvCodec {
    public static final int MAX_INPUT_CHARS = 2_000_000;
    public static final int MAX_ROWS = 10_000;
    public static final int MAX_COLUMNS = 64;
    public static final int MAX_CELL_CHARS = 20_000;

    private CsvCodec() {
    }

    public static List<List<String>> parse(String input) {
        if (input == null) {
            throw ApiException.badRequest("csv_missing", "CSV content is required.");
        }
        if (input.length() > MAX_INPUT_CHARS) {
            throw ApiException.badRequest("csv_too_large", "CSV content exceeds the supported import size.");
        }
        if (input.indexOf('\0') >= 0) {
            throw ApiException.badRequest("csv_invalid_character", "CSV content contains an invalid NUL character.");
        }

        List<List<String>> rows = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder cell = new StringBuilder();
        boolean quoted = false;

        for (int index = 0; index < input.length(); index++) {
            char ch = input.charAt(index);
            if (quoted) {
                if (ch == '"') {
                    if (index + 1 < input.length() && input.charAt(index + 1) == '"') {
                        appendCellChar(cell, '"');
                        index++;
                    } else {
                        quoted = false;
                    }
                } else {
                    appendCellChar(cell, ch);
                }
                continue;
            }

            if (ch == '"') {
                if (!cell.isEmpty()) {
                    throw ApiException.badRequest("csv_invalid_quote", "A quoted field must begin at the start of a cell.");
                }
                quoted = true;
            } else if (ch == ',') {
                addCell(row, cell);
            } else if (ch == '\n') {
                addCell(row, cell);
                addRow(rows, row);
                row = new ArrayList<>();
            } else if (ch == '\r') {
                if (index + 1 < input.length() && input.charAt(index + 1) == '\n') {
                    index++;
                }
                addCell(row, cell);
                addRow(rows, row);
                row = new ArrayList<>();
            } else {
                appendCellChar(cell, ch);
            }
        }

        if (quoted) {
            throw ApiException.badRequest("csv_unclosed_quote", "CSV contains an unclosed quoted field.");
        }
        if (!row.isEmpty() || !cell.isEmpty()) {
            addCell(row, cell);
            addRow(rows, row);
        }
        return List.copyOf(rows);
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

    private static void addRow(List<List<String>> rows, List<String> row) {
        boolean blank = row.stream().allMatch(String::isBlank);
        if (blank && rows.isEmpty()) {
            return;
        }
        if (rows.size() >= MAX_ROWS) {
            throw ApiException.badRequest("csv_too_many_rows", "CSV contains too many rows.");
        }
        rows.add(List.copyOf(row));
    }
}
