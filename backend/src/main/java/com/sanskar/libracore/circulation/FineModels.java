package com.sanskar.libracore.circulation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class FineModels {
    private FineModels() {
    }

    public record SettleFineRequest(
            @NotBlank @Pattern(regexp = "PAID|WAIVED") String status,
            @Size(max = 500) String note
    ) {
    }

    public record FineChargeView(
            UUID id,
            UUID loanId,
            UUID memberId,
            String libraryCardNumber,
            String memberName,
            UUID bookId,
            String bookTitle,
            BigDecimal amount,
            String currencyCode,
            String status,
            String reason,
            OffsetDateTime assessedAt,
            OffsetDateTime settledAt,
            String settlementNote
    ) {
    }

    public record FinePage(
            List<FineChargeView> items,
            int limit,
            int offset,
            boolean hasMore
    ) {
    }
}
