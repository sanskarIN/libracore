package com.sanskar.libracore.exchange;

import com.sanskar.libracore.exchange.DataExchangeModels.ImportResult;
import com.sanskar.libracore.security.AppPrincipal;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<String> exportBooks() {
        return csvResponse("libracore-books.csv", dataExchangeService.exportBooks());
    }

    @PostMapping(value = "/members/export", produces = "text/csv")
    public ResponseEntity<String> exportMembers() {
        return csvResponse("libracore-members.csv", dataExchangeService.exportMembers());
    }

    @PostMapping(value = "/books/import", consumes = "text/csv", produces = "application/json")
    public ImportResult importBooks(
            @RequestBody String csv,
            @AuthenticationPrincipal AppPrincipal principal
    ) {
        return dataExchangeService.importBooks(csv, principal.userId());
    }

    @PostMapping(value = "/members/import", consumes = "text/csv", produces = "application/json")
    public ImportResult importMembers(
            @RequestBody String csv,
            @AuthenticationPrincipal AppPrincipal principal
    ) {
        return dataExchangeService.importMembers(csv, principal.userId());
    }

    private static ResponseEntity<String> csvResponse(String filename, String content) {
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
