package com.sanskar.libracore.security;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class AdminUserModels {
    private AdminUserModels() {
    }

    public record CreateStaffUserRequest(
            @NotBlank @Email @Size(max = 320) String email,
            @NotBlank @Size(min = 12, max = 200) String password,
            @NotBlank @Pattern(regexp = "ADMIN|LIBRARIAN") String role
    ) {
    }

    public record SetUserEnabledRequest(boolean enabled) {
    }

    public record ResetStaffPasswordRequest(
            @NotBlank @Size(min = 12, max = 200) String password
    ) {
    }

    public record StaffUserView(
            UUID id,
            String email,
            String role,
            boolean enabled,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt,
            int activeSessionCount
    ) {
    }

    public record StaffUserPage(
            List<StaffUserView> items,
            int limit,
            int offset,
            boolean hasMore
    ) {
    }
}
