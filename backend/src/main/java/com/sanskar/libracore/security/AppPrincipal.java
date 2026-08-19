package com.sanskar.libracore.security;

import java.util.UUID;

public record AppPrincipal(
        UUID userId,
        String email,
        String role,
        UUID memberId
) {
    public boolean hasRole(String expectedRole) {
        return role != null && role.equalsIgnoreCase(expectedRole);
    }
}
