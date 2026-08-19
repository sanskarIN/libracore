package com.sanskar.libracore.member;

import com.sanskar.libracore.common.ApiException;
import com.sanskar.libracore.member.MemberModels.CreateMemberAccountRequest;
import com.sanskar.libracore.member.MemberModels.CreateMemberRequest;
import com.sanskar.libracore.member.MemberModels.MemberPage;
import com.sanskar.libracore.member.MemberModels.MemberView;
import com.sanskar.libracore.member.MemberModels.UpdateMemberRequest;
import com.sanskar.libracore.member.MemberModels.UpdateMemberStatusRequest;
import com.sanskar.libracore.security.AppPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/members")
public class MemberController {
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public MemberPage searchMembers(
            @RequestParam(defaultValue = "") @Size(max = 200) String q,
            @RequestParam(required = false) UUID branchId,
            @RequestParam(required = false) @Pattern(regexp = "ACTIVE|SUSPENDED|CLOSED") String status,
            @RequestParam(defaultValue = "25") @Min(1) @Max(100) int limit,
            @RequestParam(defaultValue = "0") @Min(0) int offset
    ) {
        return memberService.searchMembers(q, branchId, status, limit, offset);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    @ResponseStatus(HttpStatus.CREATED)
    public MemberView createMember(
            @Valid @RequestBody CreateMemberRequest request,
            @AuthenticationPrincipal AppPrincipal principal
    ) {
        return memberService.createMember(request, principal.userId());
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('MEMBER')")
    public MemberView me(@AuthenticationPrincipal AppPrincipal principal) {
        if (principal.memberId() == null) {
            throw ApiException.notFound("member_profile_not_found", "No member profile is linked to this account.");
        }
        return memberService.getMember(principal.memberId());
    }

    @GetMapping("/{memberId}")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public MemberView getMember(@PathVariable UUID memberId) {
        return memberService.getMember(memberId);
    }

    @PutMapping("/{memberId}")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public MemberView updateMember(
            @PathVariable UUID memberId,
            @Valid @RequestBody UpdateMemberRequest request,
            @AuthenticationPrincipal AppPrincipal principal
    ) {
        return memberService.updateMember(memberId, request, principal.userId());
    }

    @PatchMapping("/{memberId}/status")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public MemberView updateStatus(
            @PathVariable UUID memberId,
            @Valid @RequestBody UpdateMemberStatusRequest request,
            @AuthenticationPrincipal AppPrincipal principal
    ) {
        return memberService.updateStatus(memberId, request.status(), principal.userId());
    }

    @PostMapping("/{memberId}/account")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public MemberView createMemberAccount(
            @PathVariable UUID memberId,
            @Valid @RequestBody CreateMemberAccountRequest request,
            @AuthenticationPrincipal AppPrincipal principal
    ) {
        return memberService.createMemberAccount(memberId, request, principal.userId());
    }
}
