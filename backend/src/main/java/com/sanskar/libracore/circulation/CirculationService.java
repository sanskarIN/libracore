package com.sanskar.libracore.circulation;

import com.sanskar.libracore.audit.AuditService;
import com.sanskar.libracore.circulation.CirculationModels.CreateReservationRequest;
import com.sanskar.libracore.circulation.CirculationModels.FineAssessment;
import com.sanskar.libracore.circulation.CirculationModels.FineRuleView;
import com.sanskar.libracore.circulation.CirculationModels.IssueRequest;
import com.sanskar.libracore.circulation.CirculationModels.LoanPage;
import com.sanskar.libracore.circulation.CirculationModels.LoanView;
import com.sanskar.libracore.circulation.CirculationModels.ReservationPage;
import com.sanskar.libracore.circulation.CirculationModels.ReservationView;
import com.sanskar.libracore.circulation.CirculationModels.ReturnResult;
import com.sanskar.libracore.common.ApiException;
import com.sanskar.libracore.notification.NotificationService;
import com.sanskar.libracore.security.AppPrincipal;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CirculationService {
    private final JdbcClient jdbc;
    private final FinePolicyService finePolicyService;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final Clock clock;

    public CirculationService(
            JdbcClient jdbc,
            FinePolicyService finePolicyService,
            NotificationService notificationService,
            AuditService auditService,
            Clock clock
    ) {
        this.jdbc = jdbc;
        this.finePolicyService = finePolicyService;
        this.notificationService = notificationService;
        this.auditService = auditService;
        this.clock = clock;
    }

    @Transactional
    public LoanView issue(IssueRequest request, UUID actorUserId) {
        OffsetDateTime now = now();
        MemberForCirculation member = memberForCirculation(request.memberId());
        ensureMemberCanCirculate(member, now);
        CopyForUpdate copy = lockCopy(request.copyId());

        UUID readyReservationId = null;
        if ("RESERVED".equals(copy.status())) {
            readyReservationId = jdbc.sql("""
                            SELECT id
                            FROM reservation
                            WHERE assigned_copy_id = :copyId
                              AND member_id = :memberId
                              AND status = 'READY'
                              AND (expires_at IS NULL OR expires_at > :now)
                            LIMIT 1
                            FOR UPDATE
                            """)
                    .param("copyId", copy.id())
                    .param("memberId", member.id())
                    .param("now", now)
                    .query(UUID.class)
                    .optional()
                    .orElseThrow(() -> ApiException.conflict(
                            "copy_reserved_for_other_member",
                            "This copy is reserved for another member or its hold has expired."
                    ));
        } else if (!"AVAILABLE".equals(copy.status())) {
            throw ApiException.conflict("copy_unavailable", "This copy is not available for issue.");
        }

        FineRuleView rule = finePolicyService.currentRule(copy.branchId(), now);
        UUID loanId = UUID.randomUUID();
        OffsetDateTime dueAt = now.plusDays(rule.loanPeriodDays());

        jdbc.sql("""
                        INSERT INTO loan (
                            id, copy_id, member_id, issued_by_user_id, issued_at, due_at,
                            renewal_count, status, fine_rule_id
                        ) VALUES (
                            :id, :copyId, :memberId, :actorUserId, :issuedAt, :dueAt,
                            0, 'OPEN', :fineRuleId
                        )
                        """)
                .param("id", loanId)
                .param("copyId", copy.id())
                .param("memberId", member.id())
                .param("actorUserId", actorUserId)
                .param("issuedAt", now)
                .param("dueAt", dueAt)
                .param("fineRuleId", rule.id())
                .update();

        jdbc.sql("UPDATE book_copy SET status = 'ON_LOAN', updated_at = :now WHERE id = :id")
                .param("now", now)
                .param("id", copy.id())
                .update();

        if (readyReservationId != null) {
            jdbc.sql("""
                            UPDATE reservation
                            SET status = 'FULFILLED', fulfilled_at = :now
                            WHERE id = :id
                            """)
                    .param("now", now)
                    .param("id", readyReservationId)
                    .update();
        }

        auditService.success(actorUserId, "LOAN_ISSUE", "LOAN", loanId.toString(), null, null);
        notificationService.enqueueEmail(
                member.email(),
                "loan-issued",
                "LibraCore loan issued",
                "Your loan for \"" + copy.bookTitle() + "\" is due on " + dueAt.toLocalDate() + "."
        );
        return getLoan(loanId);
    }

    @Transactional
    public ReturnResult returnLoan(UUID loanId, UUID actorUserId) {
        OffsetDateTime now = now();
        LoanLocked loan = lockLoan(loanId);
        if (!"OPEN".equals(loan.status())) {
            throw ApiException.conflict("loan_not_open", "Only an open loan can be returned.");
        }

        CopyForUpdate copy = lockCopy(loan.copyId());
        if (!"ON_LOAN".equals(copy.status())) {
            throw ApiException.conflict(
                    "circulation_state_conflict",
                    "The copy state does not match the open loan; an administrator must review it."
            );
        }

        FineRuleView rule = loan.fineRuleId() == null
                ? finePolicyService.currentRule(copy.branchId(), loan.issuedAt())
                : finePolicyService.getRule(loan.fineRuleId());
        FineAssessment assessment = finePolicyService.assess(rule, loan.dueAt(), now);

        jdbc.sql("""
                        UPDATE loan
                        SET returned_by_user_id = :actorUserId,
                            returned_at = :returnedAt,
                            status = 'RETURNED',
                            updated_at = :returnedAt
                        WHERE id = :id
                        """)
                .param("actorUserId", actorUserId)
                .param("returnedAt", now)
                .param("id", loan.id())
                .update();

        if (assessment.amount().compareTo(BigDecimal.ZERO) > 0) {
            UUID fineId = UUID.randomUUID();
            jdbc.sql("""
                            INSERT INTO fine_charge (
                                id, loan_id, member_id, amount, currency_code, status, reason, assessed_at
                            ) VALUES (
                                :id, :loanId, :memberId, :amount, :currencyCode, 'OUTSTANDING', :reason, :assessedAt
                            )
                            """)
                    .param("id", fineId)
                    .param("loanId", loan.id())
                    .param("memberId", loan.memberId())
                    .param("amount", assessment.amount())
                    .param("currencyCode", assessment.currencyCode())
                    .param("reason", "Overdue return: " + assessment.overdueDays() + " day(s)")
                    .param("assessedAt", now)
                    .update();
            assessment = new FineAssessment(
                    assessment.overdueDays(),
                    assessment.amount(),
                    assessment.currencyCode(),
                    fineId
            );
        }

        ReservationView promoted = promoteNextReservation(copy.id(), copy.bookId(), copy.branchId(), now).orElse(null);
        auditService.success(actorUserId, "LOAN_RETURN", "LOAN", loan.id().toString(), null, null);

        notificationService.enqueueEmail(
                loan.memberEmail(),
                "loan-returned",
                "LibraCore return recorded",
                assessment.amount().signum() > 0
                        ? "Your return for \"" + copy.bookTitle() + "\" was recorded. An overdue charge of "
                        + assessment.currencyCode() + " " + assessment.amount() + " was assessed."
                        : "Your return for \"" + copy.bookTitle() + "\" was recorded."
        );

        return new ReturnResult(getLoan(loan.id()), assessment, promoted);
    }

    @Transactional
    public LoanView renew(UUID loanId, AppPrincipal actor) {
        OffsetDateTime now = now();
        LoanLocked loan = lockLoan(loanId);
        if (!"OPEN".equals(loan.status())) {
            throw ApiException.conflict("loan_not_open", "Only an open loan can be renewed.");
        }
        if (actor.hasRole("MEMBER") && !loan.memberId().equals(actor.memberId())) {
            throw ApiException.forbidden("loan_not_owned", "A member can renew only their own loan.");
        }
        if (now.isAfter(loan.dueAt())) {
            throw ApiException.conflict("loan_overdue", "An overdue loan must be reviewed before renewal.");
        }

        MemberForCirculation member = memberForCirculation(loan.memberId());
        ensureMemberCanCirculate(member, now);
        FineRuleView rule = loan.fineRuleId() == null
                ? finePolicyService.currentRule(loan.branchId(), now)
                : finePolicyService.getRule(loan.fineRuleId());

        if (loan.renewalCount() >= rule.maxRenewals()) {
            throw ApiException.conflict("renewal_limit_reached", "The renewal limit for this loan has been reached.");
        }

        int waiting = jdbc.sql("""
                        SELECT COUNT(*)
                        FROM reservation
                        WHERE book_id = :bookId
                          AND member_id <> :memberId
                          AND status IN ('WAITING','READY')
                        """)
                .param("bookId", loan.bookId())
                .param("memberId", loan.memberId())
                .query(Integer.class)
                .single();
        if (waiting > 0) {
            throw ApiException.conflict("renewal_blocked_by_waitlist", "Another member is waiting for this title.");
        }

        OffsetDateTime newDueAt = loan.dueAt().plusDays(rule.loanPeriodDays());
        jdbc.sql("""
                        UPDATE loan
                        SET due_at = :dueAt,
                            renewal_count = renewal_count + 1,
                            last_renewed_at = :now,
                            updated_at = :now
                        WHERE id = :id
                        """)
                .param("dueAt", newDueAt)
                .param("now", now)
                .param("id", loan.id())
                .update();
        auditService.success(actor.userId(), "LOAN_RENEW", "LOAN", loan.id().toString(), null, null);
        notificationService.enqueueEmail(
                loan.memberEmail(),
                "loan-renewed",
                "LibraCore loan renewed",
                "Your loan for \"" + loan.bookTitle() + "\" is now due on " + newDueAt.toLocalDate() + "."
        );
        return getLoan(loan.id());
    }

    @Transactional
    public ReservationView createReservation(CreateReservationRequest request, AppPrincipal actor) {
        UUID memberId = actor.hasRole("MEMBER") ? actor.memberId() : request.memberId();
        if (memberId == null) {
            throw ApiException.badRequest("member_required", "A member is required for this reservation.");
        }
        if (actor.hasRole("MEMBER") && request.memberId() != null && !request.memberId().equals(actor.memberId())) {
            throw ApiException.forbidden("reservation_member_mismatch", "A member can reserve only for their own account.");
        }

        OffsetDateTime now = now();
        MemberForCirculation member = memberForCirculation(memberId);
        ensureMemberCanCirculate(member, now);
        ensureBookAndBranch(request.bookId(), request.pickupBranchId());
        FineRuleView rule = finePolicyService.currentRule(request.pickupBranchId(), now);

        int activeExisting = jdbc.sql("""
                        SELECT COUNT(*) FROM reservation
                        WHERE book_id = :bookId AND member_id = :memberId AND status IN ('WAITING','READY')
                        """)
                .param("bookId", request.bookId())
                .param("memberId", memberId)
                .query(Integer.class)
                .single();
        if (activeExisting > 0) {
            throw ApiException.conflict("reservation_exists", "The member already has an active reservation for this title.");
        }

        Optional<UUID> availableCopy = jdbc.sql("""
                        SELECT id
                        FROM book_copy
                        WHERE book_id = :bookId
                          AND branch_id = :branchId
                          AND status = 'AVAILABLE'
                        ORDER BY accession_code, id
                        LIMIT 1
                        FOR UPDATE
                        """)
                .param("bookId", request.bookId())
                .param("branchId", request.pickupBranchId())
                .query(UUID.class)
                .optional();

        UUID reservationId = UUID.randomUUID();
        String status = availableCopy.isPresent() ? "READY" : "WAITING";
        OffsetDateTime expiresAt = availableCopy.isPresent() ? now.plusDays(rule.reservationHoldDays()) : null;
        jdbc.sql("""
                        INSERT INTO reservation (
                            id, book_id, member_id, pickup_branch_id, assigned_copy_id,
                            status, requested_at, ready_at, expires_at
                        ) VALUES (
                            :id, :bookId, :memberId, :branchId, :assignedCopyId,
                            :status, :requestedAt, :readyAt, :expiresAt
                        )
                        """)
                .param("id", reservationId)
                .param("bookId", request.bookId())
                .param("memberId", memberId)
                .param("branchId", request.pickupBranchId())
                .param("assignedCopyId", availableCopy.orElse(null))
                .param("status", status)
                .param("requestedAt", now)
                .param("readyAt", availableCopy.isPresent() ? now : null)
                .param("expiresAt", expiresAt)
                .update();

        if (availableCopy.isPresent()) {
            jdbc.sql("UPDATE book_copy SET status = 'RESERVED', updated_at = :now WHERE id = :id")
                    .param("now", now)
                    .param("id", availableCopy.get())
                    .update();
        }

        ReservationView view = getReservation(reservationId);
        auditService.success(actor.userId(), "RESERVATION_CREATE", "RESERVATION", reservationId.toString(), null, null);
        notificationService.enqueueEmail(
                member.email(),
                availableCopy.isPresent() ? "reservation-ready" : "reservation-waiting",
                availableCopy.isPresent() ? "LibraCore reservation ready" : "LibraCore reservation waitlisted",
                availableCopy.isPresent()
                        ? "Your reservation for \"" + view.bookTitle() + "\" is ready until " + expiresAt.toLocalDate() + "."
                        : "Your reservation for \"" + view.bookTitle() + "\" is on the waitlist."
        );
        return view;
    }

    @Transactional
    public ReservationView cancelReservation(UUID reservationId, AppPrincipal actor) {
        ReservationLocked reservation = lockReservation(reservationId);
        if (actor.hasRole("MEMBER") && !reservation.memberId().equals(actor.memberId())) {
            throw ApiException.forbidden("reservation_not_owned", "A member can cancel only their own reservation.");
        }
        if (!"WAITING".equals(reservation.status()) && !"READY".equals(reservation.status())) {
            throw ApiException.conflict("reservation_not_active", "Only a waiting or ready reservation can be cancelled.");
        }

        OffsetDateTime now = now();
        jdbc.sql("""
                        UPDATE reservation
                        SET status = 'CANCELLED', cancelled_at = :now
                        WHERE id = :id
                        """)
                .param("now", now)
                .param("id", reservation.id())
                .update();

        if (reservation.assignedCopyId() != null) {
            promoteNextReservation(
                    reservation.assignedCopyId(),
                    reservation.bookId(),
                    reservation.pickupBranchId(),
                    now
            );
        }

        auditService.success(actor.userId(), "RESERVATION_CANCEL", "RESERVATION", reservation.id().toString(), null, null);
        notificationService.enqueueEmail(
                reservation.memberEmail(),
                "reservation-cancelled",
                "LibraCore reservation cancelled",
                "Your reservation was cancelled."
        );
        return getReservation(reservation.id());
    }

    public LoanView getLoan(UUID loanId) {
        OffsetDateTime now = now();
        return jdbc.sql(loanSelect() + " WHERE l.id = :id")
                .param("id", loanId)
                .query((rs, rowNum) -> mapLoan(rs, now))
                .optional()
                .orElseThrow(() -> ApiException.notFound("loan_not_found", "Loan was not found."));
    }

    public ReservationView getReservation(UUID reservationId) {
        return jdbc.sql(reservationSelect() + " WHERE r.id = :id")
                .param("id", reservationId)
                .query(CirculationService::mapReservation)
                .optional()
                .orElseThrow(() -> ApiException.notFound("reservation_not_found", "Reservation was not found."));
    }

    public LoanPage listLoans(UUID memberId, String status, int requestedLimit, int requestedOffset) {
        int limit = Math.max(1, Math.min(requestedLimit, 100));
        int offset = Math.max(0, requestedOffset);
        StringBuilder sql = new StringBuilder(loanSelect() + " WHERE l.member_id = :memberId");
        if (status != null && !status.isBlank()) {
            sql.append(" AND l.status = :status");
        }
        sql.append(" ORDER BY l.issued_at DESC, l.id LIMIT :fetchLimit OFFSET :offset");

        JdbcClient.StatementSpec statement = jdbc.sql(sql.toString())
                .param("memberId", memberId)
                .param("fetchLimit", limit + 1)
                .param("offset", offset);
        if (status != null && !status.isBlank()) {
            statement = statement.param("status", status);
        }
        OffsetDateTime now = now();
        List<LoanView> rows = statement.query((rs, rowNum) -> mapLoan(rs, now)).list();
        boolean hasMore = rows.size() > limit;
        return new LoanPage(rows.stream().limit(limit).toList(), limit, offset, hasMore);
    }

    public ReservationPage listReservations(UUID memberId, int requestedLimit, int requestedOffset) {
        int limit = Math.max(1, Math.min(requestedLimit, 100));
        int offset = Math.max(0, requestedOffset);
        List<ReservationView> rows = jdbc.sql(reservationSelect() + """
                        WHERE r.member_id = :memberId
                        ORDER BY r.requested_at DESC, r.id
                        LIMIT :fetchLimit OFFSET :offset
                        """)
                .param("memberId", memberId)
                .param("fetchLimit", limit + 1)
                .param("offset", offset)
                .query(CirculationService::mapReservation)
                .list();
        boolean hasMore = rows.size() > limit;
        return new ReservationPage(rows.stream().limit(limit).toList(), limit, offset, hasMore);
    }

    @Scheduled(fixedDelayString = "${app.reservations.expiry-scan-ms:60000}")
    @Transactional
    public void expireReadyReservations() {
        OffsetDateTime now = now();
        List<ReservationLocked> expired = jdbc.sql("""
                        SELECT r.id, r.book_id, r.member_id, r.pickup_branch_id, r.assigned_copy_id,
                               r.status, m.email AS member_email
                        FROM reservation r
                        JOIN member m ON m.id = r.member_id
                        WHERE r.status = 'READY'
                          AND r.expires_at IS NOT NULL
                          AND r.expires_at <= :now
                        ORDER BY r.expires_at, r.id
                        LIMIT 50
                        FOR UPDATE SKIP LOCKED
                        """)
                .param("now", now)
                .query(CirculationService::mapReservationLocked)
                .list();

        for (ReservationLocked reservation : expired) {
            jdbc.sql("UPDATE reservation SET status = 'EXPIRED' WHERE id = :id")
                    .param("id", reservation.id())
                    .update();
            if (reservation.assignedCopyId() != null) {
                promoteNextReservation(
                        reservation.assignedCopyId(),
                        reservation.bookId(),
                        reservation.pickupBranchId(),
                        now
                );
            }
            auditService.success(null, "RESERVATION_EXPIRE", "RESERVATION", reservation.id().toString(), null, null);
            notificationService.enqueueEmail(
                    reservation.memberEmail(),
                    "reservation-expired",
                    "LibraCore reservation expired",
                    "Your reservation hold expired."
            );
        }
    }

    private Optional<ReservationView> promoteNextReservation(UUID copyId, UUID bookId, UUID branchId, OffsetDateTime now) {
        Optional<ReservationCandidate> next = jdbc.sql("""
                        SELECT r.id, r.member_id, m.email
                        FROM reservation r
                        JOIN member m ON m.id = r.member_id
                        WHERE r.book_id = :bookId
                          AND r.pickup_branch_id = :branchId
                          AND r.status = 'WAITING'
                          AND m.status = 'ACTIVE'
                          AND (m.expires_at IS NULL OR m.expires_at > :now)
                        ORDER BY r.requested_at, r.id
                        LIMIT 1
                        FOR UPDATE
                        """)
                .param("bookId", bookId)
                .param("branchId", branchId)
                .param("now", now)
                .query((rs, rowNum) -> new ReservationCandidate(
                        rs.getObject("id", UUID.class),
                        rs.getObject("member_id", UUID.class),
                        rs.getString("email")
                ))
                .optional();

        if (next.isEmpty()) {
            jdbc.sql("UPDATE book_copy SET status = 'AVAILABLE', updated_at = :now WHERE id = :id")
                    .param("now", now)
                    .param("id", copyId)
                    .update();
            return Optional.empty();
        }

        FineRuleView rule = finePolicyService.currentRule(branchId, now);
        OffsetDateTime expiresAt = now.plusDays(rule.reservationHoldDays());
        jdbc.sql("""
                        UPDATE reservation
                        SET status = 'READY', assigned_copy_id = :copyId,
                            ready_at = :now, expires_at = :expiresAt
                        WHERE id = :id
                        """)
                .param("copyId", copyId)
                .param("now", now)
                .param("expiresAt", expiresAt)
                .param("id", next.get().id())
                .update();
        jdbc.sql("UPDATE book_copy SET status = 'RESERVED', updated_at = :now WHERE id = :id")
                .param("now", now)
                .param("id", copyId)
                .update();

        ReservationView view = getReservation(next.get().id());
        notificationService.enqueueEmail(
                next.get().email(),
                "reservation-ready",
                "LibraCore reservation ready",
                "Your reservation for \"" + view.bookTitle() + "\" is ready until " + expiresAt.toLocalDate() + "."
        );
        return Optional.of(view);
    }

    private MemberForCirculation memberForCirculation(UUID memberId) {
        return jdbc.sql("""
                        SELECT id, email, status, expires_at, library_card_number,
                               first_name, last_name
                        FROM member
                        WHERE id = :id
                        """)
                .param("id", memberId)
                .query((rs, rowNum) -> new MemberForCirculation(
                        rs.getObject("id", UUID.class),
                        rs.getString("email"),
                        rs.getString("status"),
                        rs.getObject("expires_at", OffsetDateTime.class),
                        rs.getString("library_card_number"),
                        rs.getString("first_name") + " " + rs.getString("last_name")
                ))
                .optional()
                .orElseThrow(() -> ApiException.notFound("member_not_found", "Member was not found."));
    }

    private static void ensureMemberCanCirculate(MemberForCirculation member, OffsetDateTime now) {
        if (!"ACTIVE".equals(member.status())) {
            throw ApiException.conflict("member_not_active", "The member is not active for circulation.");
        }
        if (member.expiresAt() != null && !member.expiresAt().isAfter(now)) {
            throw ApiException.conflict("member_expired", "The member account has expired.");
        }
    }

    private CopyForUpdate lockCopy(UUID copyId) {
        return jdbc.sql("""
                        SELECT c.id, c.book_id, c.branch_id, c.status, c.accession_code, b.title
                        FROM book_copy c
                        JOIN book b ON b.id = c.book_id
                        WHERE c.id = :id
                        FOR UPDATE
                        """)
                .param("id", copyId)
                .query((rs, rowNum) -> new CopyForUpdate(
                        rs.getObject("id", UUID.class),
                        rs.getObject("book_id", UUID.class),
                        rs.getObject("branch_id", UUID.class),
                        rs.getString("status"),
                        rs.getString("accession_code"),
                        rs.getString("title")
                ))
                .optional()
                .orElseThrow(() -> ApiException.notFound("copy_not_found", "Book copy was not found."));
    }

    private LoanLocked lockLoan(UUID loanId) {
        return jdbc.sql("""
                        SELECT l.id, l.copy_id, c.book_id, c.branch_id, l.member_id,
                               m.email AS member_email, b.title AS book_title,
                               l.issued_at, l.due_at, l.renewal_count, l.status, l.fine_rule_id
                        FROM loan l
                        JOIN book_copy c ON c.id = l.copy_id
                        JOIN book b ON b.id = c.book_id
                        JOIN member m ON m.id = l.member_id
                        WHERE l.id = :id
                        FOR UPDATE
                        """)
                .param("id", loanId)
                .query((rs, rowNum) -> new LoanLocked(
                        rs.getObject("id", UUID.class),
                        rs.getObject("copy_id", UUID.class),
                        rs.getObject("book_id", UUID.class),
                        rs.getObject("branch_id", UUID.class),
                        rs.getObject("member_id", UUID.class),
                        rs.getString("member_email"),
                        rs.getString("book_title"),
                        rs.getObject("issued_at", OffsetDateTime.class),
                        rs.getObject("due_at", OffsetDateTime.class),
                        rs.getInt("renewal_count"),
                        rs.getString("status"),
                        rs.getObject("fine_rule_id", UUID.class)
                ))
                .optional()
                .orElseThrow(() -> ApiException.notFound("loan_not_found", "Loan was not found."));
    }

    private ReservationLocked lockReservation(UUID reservationId) {
        return jdbc.sql("""
                        SELECT r.id, r.book_id, r.member_id, r.pickup_branch_id, r.assigned_copy_id,
                               r.status, m.email AS member_email
                        FROM reservation r
                        JOIN member m ON m.id = r.member_id
                        WHERE r.id = :id
                        FOR UPDATE
                        """)
                .param("id", reservationId)
                .query(CirculationService::mapReservationLocked)
                .optional()
                .orElseThrow(() -> ApiException.notFound("reservation_not_found", "Reservation was not found."));
    }

    private void ensureBookAndBranch(UUID bookId, UUID branchId) {
        int bookCount = jdbc.sql("SELECT COUNT(*) FROM book WHERE id = :id")
                .param("id", bookId)
                .query(Integer.class)
                .single();
        if (bookCount == 0) {
            throw ApiException.notFound("book_not_found", "Book was not found.");
        }
        int branchCount = jdbc.sql("SELECT COUNT(*) FROM branch WHERE id = :id AND active = TRUE")
                .param("id", branchId)
                .query(Integer.class)
                .single();
        if (branchCount == 0) {
            throw ApiException.notFound("branch_not_found", "Active branch was not found.");
        }
    }

    private OffsetDateTime now() {
        return OffsetDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
    }

    private static String loanSelect() {
        return """
                SELECT l.id, l.copy_id, c.accession_code, c.book_id, b.title AS book_title,
                       l.member_id, m.library_card_number,
                       (m.first_name || ' ' || m.last_name) AS member_name,
                       l.issued_at, l.due_at, l.returned_at, l.renewal_count, l.status
                FROM loan l
                JOIN book_copy c ON c.id = l.copy_id
                JOIN book b ON b.id = c.book_id
                JOIN member m ON m.id = l.member_id
                """;
    }

    private static LoanView mapLoan(java.sql.ResultSet rs, OffsetDateTime now) throws java.sql.SQLException {
        OffsetDateTime dueAt = rs.getObject("due_at", OffsetDateTime.class);
        String status = rs.getString("status");
        return new LoanView(
                rs.getObject("id", UUID.class),
                rs.getObject("copy_id", UUID.class),
                rs.getString("accession_code"),
                rs.getObject("book_id", UUID.class),
                rs.getString("book_title"),
                rs.getObject("member_id", UUID.class),
                rs.getString("library_card_number"),
                rs.getString("member_name"),
                rs.getObject("issued_at", OffsetDateTime.class),
                dueAt,
                rs.getObject("returned_at", OffsetDateTime.class),
                rs.getInt("renewal_count"),
                status,
                "OPEN".equals(status) && dueAt.isBefore(now)
        );
    }

    private static String reservationSelect() {
        return """
                SELECT r.id, r.book_id, b.title AS book_title, r.member_id,
                       m.library_card_number, r.pickup_branch_id, br.name AS pickup_branch_name,
                       r.assigned_copy_id, r.status, r.requested_at, r.ready_at, r.expires_at,
                       CASE WHEN r.status = 'WAITING' THEN 1 + (
                           SELECT COUNT(*) FROM reservation earlier
                           WHERE earlier.book_id = r.book_id
                             AND earlier.pickup_branch_id = r.pickup_branch_id
                             AND earlier.status = 'WAITING'
                             AND earlier.requested_at < r.requested_at
                       ) ELSE 0 END AS queue_position
                FROM reservation r
                JOIN book b ON b.id = r.book_id
                JOIN member m ON m.id = r.member_id
                JOIN branch br ON br.id = r.pickup_branch_id
                """;
    }

    private static ReservationView mapReservation(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new ReservationView(
                rs.getObject("id", UUID.class),
                rs.getObject("book_id", UUID.class),
                rs.getString("book_title"),
                rs.getObject("member_id", UUID.class),
                rs.getString("library_card_number"),
                rs.getObject("pickup_branch_id", UUID.class),
                rs.getString("pickup_branch_name"),
                rs.getObject("assigned_copy_id", UUID.class),
                rs.getString("status"),
                rs.getObject("requested_at", OffsetDateTime.class),
                rs.getObject("ready_at", OffsetDateTime.class),
                rs.getObject("expires_at", OffsetDateTime.class),
                rs.getInt("queue_position")
        );
    }

    private static ReservationLocked mapReservationLocked(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new ReservationLocked(
                rs.getObject("id", UUID.class),
                rs.getObject("book_id", UUID.class),
                rs.getObject("member_id", UUID.class),
                rs.getObject("pickup_branch_id", UUID.class),
                rs.getObject("assigned_copy_id", UUID.class),
                rs.getString("status"),
                rs.getString("member_email")
        );
    }

    private record MemberForCirculation(
            UUID id,
            String email,
            String status,
            OffsetDateTime expiresAt,
            String libraryCardNumber,
            String name
    ) {
    }

    private record CopyForUpdate(
            UUID id,
            UUID bookId,
            UUID branchId,
            String status,
            String accessionCode,
            String bookTitle
    ) {
    }

    private record LoanLocked(
            UUID id,
            UUID copyId,
            UUID bookId,
            UUID branchId,
            UUID memberId,
            String memberEmail,
            String bookTitle,
            OffsetDateTime issuedAt,
            OffsetDateTime dueAt,
            int renewalCount,
            String status,
            UUID fineRuleId
    ) {
    }

    private record ReservationLocked(
            UUID id,
            UUID bookId,
            UUID memberId,
            UUID pickupBranchId,
            UUID assignedCopyId,
            String status,
            String memberEmail
    ) {
    }

    private record ReservationCandidate(UUID id, UUID memberId, String email) {
    }
}
