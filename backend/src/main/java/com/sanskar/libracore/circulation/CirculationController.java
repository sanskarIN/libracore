package com.sanskar.libracore.circulation;

import com.sanskar.libracore.circulation.CirculationModels.CreateFineRuleRequest;
import com.sanskar.libracore.circulation.CirculationModels.CreateReservationRequest;
import com.sanskar.libracore.circulation.CirculationModels.FineRuleView;
import com.sanskar.libracore.circulation.CirculationModels.IssueRequest;
import com.sanskar.libracore.circulation.CirculationModels.LoanPage;
import com.sanskar.libracore.circulation.CirculationModels.LoanView;
import com.sanskar.libracore.circulation.CirculationModels.ReservationPage;
import com.sanskar.libracore.circulation.CirculationModels.ReservationView;
import com.sanskar.libracore.circulation.CirculationModels.ReturnResult;
import com.sanskar.libracore.common.ApiException;
import com.sanskar.libracore.security.AppPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/circulation")
public class CirculationController {
    private final CirculationService circulationService;
    private final FinePolicyService finePolicyService;

    public CirculationController(CirculationService circulationService, FinePolicyService finePolicyService) {
        this.circulationService = circulationService;
        this.finePolicyService = finePolicyService;
    }

    @PostMapping("/loans")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    @ResponseStatus(HttpStatus.CREATED)
    public LoanView issue(
            @Valid @RequestBody IssueRequest request,
            @AuthenticationPrincipal AppPrincipal principal
    ) {
        return circulationService.issue(request, principal.userId());
    }

    @PostMapping("/loans/{loanId}/return")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public ReturnResult returnLoan(
            @PathVariable UUID loanId,
            @AuthenticationPrincipal AppPrincipal principal
    ) {
        return circulationService.returnLoan(loanId, principal.userId());
    }

    @PostMapping("/loans/{loanId}/renew")
    public LoanView renew(
            @PathVariable UUID loanId,
            @AuthenticationPrincipal AppPrincipal principal
    ) {
        return circulationService.renew(loanId, principal);
    }

    @GetMapping("/loans/me")
    @PreAuthorize("hasRole('MEMBER')")
    public LoanPage myLoans(
            @AuthenticationPrincipal AppPrincipal principal,
            @RequestParam(required = false) @Pattern(regexp = "OPEN|RETURNED|LOST") String status,
            @RequestParam(defaultValue = "25") @Min(1) @Max(100) int limit,
            @RequestParam(defaultValue = "0") @Min(0) int offset
    ) {
        if (principal.memberId() == null) {
            throw ApiException.notFound("member_profile_not_found", "No member profile is linked to this account.");
        }
        return circulationService.listLoans(principal.memberId(), status, limit, offset);
    }

    @GetMapping("/loans")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public LoanPage memberLoans(
            @RequestParam UUID memberId,
            @RequestParam(required = false) @Pattern(regexp = "OPEN|RETURNED|LOST") String status,
            @RequestParam(defaultValue = "25") @Min(1) @Max(100) int limit,
            @RequestParam(defaultValue = "0") @Min(0) int offset
    ) {
        return circulationService.listLoans(memberId, status, limit, offset);
    }

    @PostMapping("/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationView reserve(
            @Valid @RequestBody CreateReservationRequest request,
            @AuthenticationPrincipal AppPrincipal principal
    ) {
        return circulationService.createReservation(request, principal);
    }

    @PostMapping("/reservations/{reservationId}/cancel")
    public ReservationView cancelReservation(
            @PathVariable UUID reservationId,
            @AuthenticationPrincipal AppPrincipal principal
    ) {
        return circulationService.cancelReservation(reservationId, principal);
    }

    @GetMapping("/reservations/me")
    @PreAuthorize("hasRole('MEMBER')")
    public ReservationPage myReservations(
            @AuthenticationPrincipal AppPrincipal principal,
            @RequestParam(defaultValue = "25") @Min(1) @Max(100) int limit,
            @RequestParam(defaultValue = "0") @Min(0) int offset
    ) {
        if (principal.memberId() == null) {
            throw ApiException.notFound("member_profile_not_found", "No member profile is linked to this account.");
        }
        return circulationService.listReservations(principal.memberId(), limit, offset);
    }

    @GetMapping("/reservations")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public ReservationPage memberReservations(
            @RequestParam UUID memberId,
            @RequestParam(defaultValue = "25") @Min(1) @Max(100) int limit,
            @RequestParam(defaultValue = "0") @Min(0) int offset
    ) {
        return circulationService.listReservations(memberId, limit, offset);
    }

    @GetMapping("/policies")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public List<FineRuleView> policies(@RequestParam(required = false) UUID branchId) {
        return finePolicyService.listRules(branchId);
    }

    @PostMapping("/policies")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public FineRuleView createPolicy(
            @Valid @RequestBody CreateFineRuleRequest request,
            @AuthenticationPrincipal AppPrincipal principal
    ) {
        return finePolicyService.createRule(request, principal.userId());
    }
}
