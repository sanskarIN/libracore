package com.sanskar.libracore.exchange;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.io.ByteArrayOutputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class DataExchangeControllerTest {
    @Test
    void streamsBookExportWithoutBuildingControllerStringBody() throws Exception {
        DataExchangeService service = mock(DataExchangeService.class);
        DataExchangeController controller = new DataExchangeController(service);
        doAnswer(invocation -> {
            Writer writer = invocation.getArgument(0);
            writer.write("title,languageCode\nBook,en\n");
            return null;
        }).when(service).writeBooksCsv(any(Writer.class));

        var response = controller.exportBooks();
        var body = response.getBody();
        assertNotNull(body);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        body.writeTo(output);

        assertEquals("title,languageCode\nBook,en\n", output.toString(StandardCharsets.UTF_8));
        assertEquals("no-store", response.getHeaders().getFirst(HttpHeaders.CACHE_CONTROL));
        assertTrue(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION).contains("libracore-books.csv"));
        verify(service).writeBooksCsv(any(Writer.class));
    }

    @Test
    void streamsMemberExportWithoutBuildingControllerStringBody() throws Exception {
        DataExchangeService service = mock(DataExchangeService.class);
        DataExchangeController controller = new DataExchangeController(service);
        doAnswer(invocation -> {
            Writer writer = invocation.getArgument(0);
            writer.write("libraryCardNumber,email\nLC-1,reader@example.invalid\n");
            return null;
        }).when(service).writeMembersCsv(any(Writer.class));

        var response = controller.exportMembers();
        var body = response.getBody();
        assertNotNull(body);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        body.writeTo(output);

        assertEquals(
                "libraryCardNumber,email\nLC-1,reader@example.invalid\n",
                output.toString(StandardCharsets.UTF_8)
        );
        assertTrue(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION).contains("libracore-members.csv"));
        verify(service).writeMembersCsv(any(Writer.class));
    }
}
