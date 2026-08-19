package com.sanskar.libracore.reporting;

import com.sanskar.libracore.reporting.ReportingModels.AuditEventView;
import com.sanskar.libracore.reporting.ReportingModels.DashboardView;
import com.sanskar.libracore.reporting.ReportingModels.OverdueLoanView;
import com.sanskar.libracore.reporting.ReportingModels.Page;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
public class ReportingController {
    private final ReportingService reportingService;

    public ReportingController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @GetMapping("/dashboard")
    public DashboardView dashboard(@RequestParam(required = false) UUID branchId) {
        return reportingService.dashboard(branchId);
    }

    @GetMapping("/overdue")
    public Page<OverdueLoanView> overdue(
            @RequestParam(required = false) UUID branchId,
            @RequestParam(defaultValue = "25") @Min(1) @Max(100) int limit,
            @RequestParam(defaultValue = "0") @Min(0) int offset
    ) {
        return reportingService.overdueLoans(branchId, limit, offset);
    }

    @GetMapping("/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<AuditEventView> audit(
            @RequestParam(required = false) @Size(max = 120) String action,
            @RequestParam(required = false) @Size(max = 80) String entityType,
            @RequestParam(required = false) @Pattern(regexp = "SUCCESS|DENIED|FAILURE") String outcome,
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int limit,
            @RequestParam(defaultValue = "0") @Min(0) int offset
    ) {
        return reportingService.auditEvents(action, entityType, outcome, limit, offset);
    }
}
