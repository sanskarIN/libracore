package com.sanskar.libracore.circulation;

import com.sanskar.libracore.circulation.CirculationModels.FineAssessment;
import com.sanskar.libracore.circulation.CirculationModels.FineRuleView;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FinePolicyServiceTest {
    private final FinePolicyService service = new FinePolicyService(null, null, Clock.systemUTC());

    @Test
    void appliesGracePeriodAndRoundsPartialDaysUp() {
        FineRuleView rule = rule(new BigDecimal("2.00"), 1, new BigDecimal("500.00"));
        OffsetDateTime due = OffsetDateTime.of(2026, 8, 1, 10, 0, 0, 0, ZoneOffset.UTC);

        FineAssessment assessment = service.assess(rule, due, due.plusDays(2).plusMinutes(1));

        assertEquals(2, assessment.overdueDays());
        assertEquals(new BigDecimal("4.00"), assessment.amount());
    }

    @Test
    void capsFineAtConfiguredMaximum() {
        FineRuleView rule = rule(new BigDecimal("20.00"), 0, new BigDecimal("50.00"));
        OffsetDateTime due = OffsetDateTime.of(2026, 8, 1, 10, 0, 0, 0, ZoneOffset.UTC);

        FineAssessment assessment = service.assess(rule, due, due.plusDays(10));

        assertEquals(new BigDecimal("50.00"), assessment.amount());
    }

    @Test
    void chargesNothingDuringGracePeriod() {
        FineRuleView rule = rule(new BigDecimal("2.00"), 2, new BigDecimal("500.00"));
        OffsetDateTime due = OffsetDateTime.of(2026, 8, 1, 10, 0, 0, 0, ZoneOffset.UTC);

        FineAssessment assessment = service.assess(rule, due, due.plusDays(2));

        assertEquals(0, assessment.overdueDays());
        assertEquals(new BigDecimal("0.00"), assessment.amount());
    }

    private static FineRuleView rule(BigDecimal dailyRate, int graceDays, BigDecimal maxFine) {
        return new FineRuleView(
                UUID.randomUUID(), UUID.randomUUID(), "Main", "Test rule", dailyRate,
                graceDays, maxFine, "INR", 2, 14, 3, true,
                OffsetDateTime.now(ZoneOffset.UTC), null
        );
    }
}
