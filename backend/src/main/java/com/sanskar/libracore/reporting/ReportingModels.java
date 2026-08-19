package com.sanskar.libracore.reporting;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class ReportingModels {
    private ReportingModels() {
    }

    public record DashboardView(
            long books,
            long copies,
            long availableCopies,
            long openLoans,
            long overdueLoans,
            long activeMembers,
            long waitingReservations,
            long readyReservations,
            BigDecimal outstandingFines,
            String fineCurrency,
            OffsetDateTime generatedAt
    ) {
    }

    public record OverdueLoanView(
            UUID loanId,
            UUID copyId,
            String accessionCode,
            String bookTitle,
            UUID memberId,
            String libraryCardNumber,
            String memberName,
            String memberEmail,
            OffsetDateTime issuedAt,
            OffsetDateTime dueAt,
            long overdueDays,
            UUID branchId,
            String branchName
    ) {
    }

    public record AuditEventView(
            UUID id,
            OffsetDateTime occurredAt,
            UUID actorUserId,
            String actorEmail,
            String action,
            String entityType,
            String entityId,
            String outcome,
            String correlationId
    ) {
    }

    public record Page<T>(List<T> items, int limit, int offset, boolean hasMore) {
    }
}
