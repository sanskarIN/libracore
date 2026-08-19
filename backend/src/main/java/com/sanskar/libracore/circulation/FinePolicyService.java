package com.sanskar.libracore.circulation;

import com.sanskar.libracore.audit.AuditService;
import com.sanskar.libracore.circulation.CirculationModels.CreateFineRuleRequest;
import com.sanskar.libracore.circulation.CirculationModels.FineAssessment;
import com.sanskar.libracore.circulation.CirculationModels.FineRuleView;
import com.sanskar.libracore.common.ApiException;
import com.sanskar.libracore.common.TextNormalizer;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class FinePolicyService {
    private final JdbcClient jdbc;
    private final AuditService auditService;
    private final Clock clock;

    public FinePolicyService(JdbcClient jdbc, AuditService auditService, Clock clock) {
        this.jdbc = jdbc;
        this.auditService = auditService;
        this.clock = clock;
    }

    @Transactional
    public FineRuleView createRule(CreateFineRuleRequest request, UUID actorUserId) {
        int branchCount = jdbc.sql("SELECT COUNT(*) FROM branch WHERE id = :id AND active = TRUE")
                .param("id", request.branchId())
                .query(Integer.class)
                .single();
        if (branchCount == 0) {
            throw ApiException.notFound("branch_not_found", "Active branch was not found.");
        }

        OffsetDateTime now = now();
        OffsetDateTime effectiveFrom = request.effectiveFrom() == null ? now : request.effectiveFrom();
        if (effectiveFrom.isBefore(now.minusMinutes(5))) {
            throw ApiException.badRequest("policy_effective_date_past", "A new policy cannot be backdated.");
        }

        int futureRules = jdbc.sql("""
                        SELECT COUNT(*)
                        FROM fine_rule
                        WHERE branch_id = :branchId
                          AND active = TRUE
                          AND effective_from >= :effectiveFrom
                        """)
                .param("branchId", request.branchId())
                .param("effectiveFrom", effectiveFrom)
                .query(Integer.class)
                .single();
        if (futureRules > 0) {
            throw ApiException.conflict(
                    "policy_schedule_conflict",
                    "A policy already starts at or after the requested effective time for this branch."
            );
        }

        jdbc.sql("""
                        UPDATE fine_rule
                        SET effective_until = :effectiveFrom
                        WHERE branch_id = :branchId
                          AND active = TRUE
                          AND effective_from < :effectiveFrom
                          AND (effective_until IS NULL OR effective_until > :effectiveFrom)
                        """)
                .param("effectiveFrom", effectiveFrom)
                .param("branchId", request.branchId())
                .update();

        UUID id = UUID.randomUUID();
        jdbc.sql("""
                        INSERT INTO fine_rule (
                            id, branch_id, name, daily_rate, grace_days, max_fine,
                            currency_code, max_renewals, loan_period_days,
                            reservation_hold_days, active, effective_from
                        ) VALUES (
                            :id, :branchId, :name, :dailyRate, :graceDays, :maxFine,
                            :currencyCode, :maxRenewals, :loanPeriodDays,
                            :reservationHoldDays, TRUE, :effectiveFrom
                        )
                        """)
                .param("id", id)
                .param("branchId", request.branchId())
                .param("name", TextNormalizer.display(request.name()))
                .param("dailyRate", request.dailyRate().setScale(2, RoundingMode.HALF_UP))
                .param("graceDays", request.graceDays())
                .param("maxFine", request.maxFine() == null ? null : request.maxFine().setScale(2, RoundingMode.HALF_UP))
                .param("currencyCode", request.currencyCode())
                .param("maxRenewals", request.maxRenewals())
                .param("loanPeriodDays", request.loanPeriodDays())
                .param("reservationHoldDays", request.reservationHoldDays())
                .param("effectiveFrom", effectiveFrom)
                .update();
        auditService.success(actorUserId, "FINE_RULE_CREATE", "FINE_RULE", id.toString(), null, null);
        return getRule(id);
    }

    public FineRuleView currentRule(UUID branchId) {
        return currentRule(branchId, now());
    }

    public FineRuleView currentRule(UUID branchId, OffsetDateTime at) {
        return jdbc.sql(ruleSelect() + """
                        WHERE fr.branch_id = :branchId
                          AND fr.active = TRUE
                          AND fr.effective_from <= :at
                          AND (fr.effective_until IS NULL OR fr.effective_until > :at)
                        ORDER BY fr.effective_from DESC
                        LIMIT 1
                        """)
                .param("branchId", branchId)
                .param("at", at)
                .query(FinePolicyService::mapRule)
                .optional()
                .orElseThrow(() -> ApiException.conflict(
                        "circulation_policy_missing",
                        "No effective circulation policy exists for this branch."
                ));
    }

    public FineRuleView getRule(UUID ruleId) {
        return jdbc.sql(ruleSelect() + " WHERE fr.id = :id")
                .param("id", ruleId)
                .query(FinePolicyService::mapRule)
                .optional()
                .orElseThrow(() -> ApiException.notFound("fine_rule_not_found", "Circulation policy was not found."));
    }

    public List<FineRuleView> listRules(UUID branchId) {
        if (branchId == null) {
            return jdbc.sql(ruleSelect() + " ORDER BY b.name, fr.effective_from DESC")
                    .query(FinePolicyService::mapRule)
                    .list();
        }
        return jdbc.sql(ruleSelect() + " WHERE fr.branch_id = :branchId ORDER BY fr.effective_from DESC")
                .param("branchId", branchId)
                .query(FinePolicyService::mapRule)
                .list();
    }

    public FineAssessment assess(FineRuleView rule, OffsetDateTime dueAt, OffsetDateTime returnedAt) {
        OffsetDateTime chargeBegins = dueAt.plusDays(rule.graceDays());
        if (!returnedAt.isAfter(chargeBegins) || rule.dailyRate().signum() == 0) {
            return new FineAssessment(0, BigDecimal.ZERO.setScale(2), rule.currencyCode(), null);
        }

        long secondsLate = Duration.between(chargeBegins, returnedAt).getSeconds();
        int overdueDays = Math.toIntExact(Math.min(Integer.MAX_VALUE, (secondsLate + 86_399L) / 86_400L));
        BigDecimal amount = rule.dailyRate().multiply(BigDecimal.valueOf(overdueDays));
        if (rule.maxFine() != null && amount.compareTo(rule.maxFine()) > 0) {
            amount = rule.maxFine();
        }
        return new FineAssessment(overdueDays, amount.setScale(2, RoundingMode.HALF_UP), rule.currencyCode(), null);
    }

    private OffsetDateTime now() {
        return OffsetDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
    }

    private static String ruleSelect() {
        return """
                SELECT fr.id, fr.branch_id, b.name AS branch_name, fr.name, fr.daily_rate,
                       fr.grace_days, fr.max_fine, fr.currency_code, fr.max_renewals,
                       fr.loan_period_days, fr.reservation_hold_days, fr.active,
                       fr.effective_from, fr.effective_until
                FROM fine_rule fr
                JOIN branch b ON b.id = fr.branch_id
                """;
    }

    private static FineRuleView mapRule(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new FineRuleView(
                rs.getObject("id", UUID.class),
                rs.getObject("branch_id", UUID.class),
                rs.getString("branch_name"),
                rs.getString("name"),
                rs.getBigDecimal("daily_rate"),
                rs.getInt("grace_days"),
                rs.getBigDecimal("max_fine"),
                rs.getString("currency_code"),
                rs.getInt("max_renewals"),
                rs.getInt("loan_period_days"),
                rs.getInt("reservation_hold_days"),
                rs.getBoolean("active"),
                rs.getObject("effective_from", OffsetDateTime.class),
                rs.getObject("effective_until", OffsetDateTime.class)
        );
    }
}
