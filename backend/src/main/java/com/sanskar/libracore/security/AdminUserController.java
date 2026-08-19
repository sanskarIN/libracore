package com.sanskar.libracore.security;

import com.sanskar.libracore.security.AdminUserModels.CreateStaffUserRequest;
import com.sanskar.libracore.security.AdminUserModels.ResetStaffPasswordRequest;
import com.sanskar.libracore.security.AdminUserModels.SetUserEnabledRequest;
import com.sanskar.libracore.security.AdminUserModels.StaffUserPage;
import com.sanskar.libracore.security.AdminUserModels.StaffUserView;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public StaffUserPage list(
            @RequestParam(required = false) @Pattern(regexp = "ADMIN|LIBRARIAN") String role,
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int limit,
            @RequestParam(defaultValue = "0") @Min(0) int offset
    ) {
        return adminUserService.list(role, limit, offset);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StaffUserView create(
            @Valid @RequestBody CreateStaffUserRequest request,
            @AuthenticationPrincipal AppPrincipal principal
    ) {
        return adminUserService.create(request, principal.userId());
    }

    @PatchMapping("/{userId}/enabled")
    public StaffUserView setEnabled(
            @PathVariable UUID userId,
            @Valid @RequestBody SetUserEnabledRequest request,
            @AuthenticationPrincipal AppPrincipal principal
    ) {
        return adminUserService.setEnabled(userId, request.enabled(), principal.userId());
    }

    @PostMapping("/{userId}/password")
    public StaffUserView resetPassword(
            @PathVariable UUID userId,
            @Valid @RequestBody ResetStaffPasswordRequest request,
            @AuthenticationPrincipal AppPrincipal principal
    ) {
        return adminUserService.resetPassword(userId, request.password(), principal.userId());
    }
}
