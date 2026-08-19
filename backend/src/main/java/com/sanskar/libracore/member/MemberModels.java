package com.sanskar.libracore.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class MemberModels {
    private MemberModels() {
    }

    public record CreateMemberRequest(
            @NotNull UUID homeBranchId,
            @NotBlank @Size(max = 80) String libraryCardNumber,
            @NotBlank @Size(max = 120) String firstName,
            @NotBlank @Size(max = 120) String lastName,
            @NotBlank @Email @Size(max = 320) String email,
            @Size(max = 40) String phone,
            OffsetDateTime expiresAt,
            @Size(max = 1000) String notes,
            @Size(min = 12, max = 200) String accountPassword
    ) {
    }

    public record UpdateMemberRequest(
            @NotNull UUID homeBranchId,
            @NotBlank @Size(max = 120) String firstName,
            @NotBlank @Size(max = 120) String lastName,
            @NotBlank @Email @Size(max = 320) String email,
            @Size(max = 40) String phone,
            OffsetDateTime expiresAt,
            @Size(max = 1000) String notes
    ) {
    }

    public record UpdateMemberStatusRequest(
            @NotBlank @Pattern(regexp = "ACTIVE|SUSPENDED|CLOSED") String status
    ) {
    }

    public record CreateMemberAccountRequest(
            @NotBlank @Size(min = 12, max = 200) String password
    ) {
    }

    public record MemberView(
            UUID id,
            UUID homeBranchId,
            String homeBranchName,
            String libraryCardNumber,
            String firstName,
            String lastName,
            String email,
            String phone,
            String status,
            OffsetDateTime joinedAt,
            OffsetDateTime expiresAt,
            String notes,
            int openLoanCount,
            int activeReservationCount,
            BigDecimal outstandingFine,
            boolean accountEnabled
    ) {
    }

    public record MemberPage(
            List<MemberView> items,
            int limit,
            int offset,
            boolean hasMore
    ) {
    }
}
