package com.sanskar.libracore.exchange;

import com.sanskar.libracore.audit.AuditService;
import com.sanskar.libracore.catalog.CatalogModels.CreateBookRequest;
import com.sanskar.libracore.catalog.CatalogService;
import com.sanskar.libracore.common.ApiException;
import com.sanskar.libracore.member.MemberService;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DataExchangeServiceTest {
    private JdbcTemplate jdbcTemplate;
    private CatalogService catalogService;
    private Validator validator;
    private DataExchangeService service;

    @BeforeEach
    void setUp() {
        JdbcClient jdbc = mock(JdbcClient.class);
        jdbcTemplate = mock(JdbcTemplate.class);
        catalogService = mock(CatalogService.class);
        MemberService memberService = mock(MemberService.class);
        AuditService auditService = mock(AuditService.class);
        validator = mock(Validator.class);
        when(validator.validate(any())).thenReturn(Set.of());

        service = new DataExchangeService(
                jdbc,
                jdbcTemplate,
                catalogService,
                memberService,
                auditService,
                validator
        );
    }

    @Test
    void importsBooksDirectlyFromReader() {
        UUID actorUserId = UUID.randomUUID();
        String csv = "title,languageCode\nFirst Book,en\nSecond Book,\n";

        var result = service.importBooks(new StringReader(csv), actorUserId);

        assertEquals("books", result.resource());
        assertEquals(2, result.importedRows());
        assertEquals(0, result.warnings().size());
        verify(catalogService, times(2)).createBook(any(CreateBookRequest.class), eq(actorUserId));
    }

    @Test
    void rejectsEmptyReaderWithStableErrorCode() {
        UUID actorUserId = UUID.randomUUID();

        ApiException exception = assertThrows(
                ApiException.class,
                () -> service.importBooks(new StringReader(""), actorUserId)
        );

        assertEquals("csv_empty", exception.code());
    }

    @Test
    void rejectsOversizedBookExportBeforeWritingRows() {
        when(jdbcTemplate.queryForObject(any(String.class), eq(Integer.class))).thenReturn(10_001);

        ApiException exception = assertThrows(
                ApiException.class,
                () -> service.writeBooksCsv(new StringWriter())
        );

        assertEquals("export_too_large", exception.code());
    }

    @Test
    void rejectsOversizedMemberExportBeforeWritingRows() {
        when(jdbcTemplate.queryForObject(any(String.class), eq(Integer.class))).thenReturn(10_001);

        ApiException exception = assertThrows(
                ApiException.class,
                () -> service.writeMembersCsv(new StringWriter())
        );

        assertEquals("export_too_large", exception.code());
    }
}
