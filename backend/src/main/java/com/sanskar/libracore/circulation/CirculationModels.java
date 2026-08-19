package com.sanskar.libracore.circulation;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class CirculationModels {
    private CirculationModels() {
    }

    public record IssueRequest(
            @NotNull UUID copyId,
            @NotNull UUID memberId
    ) {
    }

    public record CreateReservationRequest(
            @NotNull UUID bookId,
            @NotNull UUID pickupBranchId,
            UUID memberId
    ) {
    }

    public record LoanView(
            UUID id,
            UUID copyId,
            String accessionCode,
            UUID bookId,
            String bookTitle,
            UUID memberId,
            String libraryCardNumber,
            String memberName,
            OffsetDateTime issuedAt,
            OffsetDateTime dueAt,
            OffsetDateTime returnedAt,
            int renewalCount,
            String status,
            boolean overdue
    ) {
    }

    public record ReturnResult(
            LoanView loan,
            FineAssessment fine,
            ReservationView promotedReservation
    ) {
    }

    public record ReservationView(
            UUID id,
            UUID bookId,
            String bookTitle,
            UUID memberId,
            String libraryCardNumber,
            UUID pickupBranchId,
            String pickupBranchName,
            UUID assignedCopyId,
            String status,
            OffsetDateTime requestedAt,
            OffsetDateTime readyAt,
            OffsetDateTime expiresAt,
            int queuePosition
    ) {
    }

    public record FineAssessment(
            int overdueDays,
            BigDecimal amount,
            String currencyCode,
            UUID fineChargeId
    ) {
    }

    public record FineRuleView(
            UUID id,
            UUID branchId,
            String branchName,
            String name,
            BigDecimal dailyRate,
            int graceDays,
            BigDecimal maxFine,
            String currencyCode,
            int maxRenewals,
            int loanPeriodDays,
            int reservationHoldDays,
            boolean active,
            OffsetDateTime effectiveFrom,
            OffsetDateTime effectiveUntil
    ) {
    }

    public record CreateFineRuleRequest(
            @NotNull UUID branchId,
            @NotBlank @Size(max = 160) String name,
            @NotNull @DecimalMin("0.00") BigDecimal dailyRate,
            @Min(0) @Max(365) int graceDays,
            @DecimalMin("0.00") BigDecimal maxFine,
            @NotBlank @Pattern(regexp = "[A-Z]{3}") String currencyCode,
            @Min(0) @Max(20) int maxRenewals,
            @Min(1) @Max(365) int loanPeriodDays,
            @Min(1) @Max(30) int reservationHoldDays,
            OffsetDateTime effectiveFrom
    ) {
    }

    public record LoanPage(List<LoanView> items, int limit, int offset, boolean hasMore) {
    }

    public record ReservationPage(List<ReservationView> items, int limit, int offset, boolean hasMore) {
    }
}
