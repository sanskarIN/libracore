package com.sanskar.libracore.exchange;

import com.sanskar.libracore.common.ApiException;
import com.sanskar.libracore.exchange.DataExchangeModels.ImportResult;
import com.sanskar.libracore.security.AppPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/exchange")
@PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
public class DataExchangeController {
    private static final MediaType CSV = new MediaType("text", "csv", StandardCharsets.UTF_8);

    private final DataExchangeService dataExchangeService;

    public DataExchangeController(DataExchangeService dataExchangeService) {
        this.dataExchangeService = dataExchangeService;
    }

    @PostMapping(value = "/books/export", produces = "text/csv")
    public ResponseEntity<StreamingResponseBody> exportBooks() {
        return csvResponse("libracore-books.csv", outputStream -> {
            Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
            dataExchangeService.writeBooksCsv(writer);
            writer.flush();
        });
    }

    @PostMapping(value = "/members/export", produces = "text/csv")
    public ResponseEntity<StreamingResponseBody> exportMembers() {
        return csvResponse("libracore-members.csv", outputStream -> {
            Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
            dataExchangeService.writeMembersCsv(writer);
            writer.flush();
        });
    }

    @PostMapping(value = "/books/import", consumes = "text/csv", produces = "application/json")
    public ImportResult importBooks(
            HttpServletRequest request,
            @AuthenticationPrincipal AppPrincipal principal
    ) {
        return dataExchangeService.importBooks(csvReader(request), principal.userId());
    }

    @PostMapping(value = "/members/import", consumes = "text/csv", produces = "application/json")
    public ImportResult importMembers(
            HttpServletRequest request,
            @AuthenticationPrincipal AppPrincipal principal
    ) {
        return dataExchangeService.importMembers(csvReader(request), principal.userId());
    }

    private static Reader csvReader(HttpServletRequest request) {
        try {
            var decoder = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT);
            return new BufferedReader(new InputStreamReader(request.getInputStream(), decoder));
        } catch (IOException exception) {
            throw ApiException.badRequest("csv_read_failed", "CSV content could not be read.");
        }
    }

    private static ResponseEntity<StreamingResponseBody> csvResponse(String filename, StreamingResponseBody content) {
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(filename, StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(CSV)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(content);
    }
}
