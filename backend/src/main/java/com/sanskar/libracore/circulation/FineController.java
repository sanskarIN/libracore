package com.sanskar.libracore.circulation;

import com.sanskar.libracore.circulation.FineModels.FineChargeView;
import com.sanskar.libracore.circulation.FineModels.FinePage;
import com.sanskar.libracore.circulation.FineModels.SettleFineRequest;
import com.sanskar.libracore.common.ApiException;
import com.sanskar.libracore.security.AppPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/fines")
public class FineController {
    private final FineService fineService;

    public FineController(FineService fineService) {
        this.fineService = fineService;
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('MEMBER')")
    public FinePage myFines(
            @AuthenticationPrincipal AppPrincipal principal,
            @RequestParam(required = false) @Pattern(regexp = "OUTSTANDING|PAID|WAIVED") String status,
            @RequestParam(defaultValue = "25") @Min(1) @Max(100) int limit,
            @RequestParam(defaultValue = "0") @Min(0) int offset
    ) {
        if (principal.memberId() == null) {
            throw ApiException.notFound("member_profile_not_found", "No member profile is linked to this account.");
        }
        return fineService.listFines(principal.memberId(), status, limit, offset);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public FinePage memberFines(
            @RequestParam UUID memberId,
            @RequestParam(required = false) @Pattern(regexp = "OUTSTANDING|PAID|WAIVED") String status,
            @RequestParam(defaultValue = "25") @Min(1) @Max(100) int limit,
            @RequestParam(defaultValue = "0") @Min(0) int offset
    ) {
        return fineService.listFines(memberId, status, limit, offset);
    }

    @GetMapping("/{fineId}")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public FineChargeView fine(@PathVariable UUID fineId) {
        return fineService.getFine(fineId);
    }

    @PostMapping("/{fineId}/settle")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public FineChargeView settle(
            @PathVariable UUID fineId,
            @Valid @RequestBody SettleFineRequest request,
            @AuthenticationPrincipal AppPrincipal principal
    ) {
        return fineService.settle(fineId, request, principal.userId());
    }
}
