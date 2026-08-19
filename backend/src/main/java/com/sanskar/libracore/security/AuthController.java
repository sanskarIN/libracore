package com.sanskar.libracore.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final String BEARER_PREFIX = "Bearer ";

    private final SessionTokenService sessionTokenService;

    public AuthController(SessionTokenService sessionTokenService) {
        this.sessionTokenService = sessionTokenService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        SessionTokenService.LoginResult result = sessionTokenService.login(
                request.email(),
                request.password(),
                servletRequest.getHeader("User-Agent")
        );
        return new LoginResponse(
                result.token(),
                "Bearer",
                result.expiresAt(),
                UserResponse.from(result.principal())
        );
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal AppPrincipal principal) {
        return UserResponse.from(principal);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @AuthenticationPrincipal AppPrincipal principal,
            HttpServletRequest request
    ) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            sessionTokenService.logout(authorization.substring(BEARER_PREFIX.length()).trim(), principal.userId());
        }
    }

    public record LoginRequest(
            @NotBlank @Email @Size(max = 320) String email,
            @NotBlank @Size(max = 200) String password
    ) {
    }

    public record LoginResponse(
            String accessToken,
            String tokenType,
            OffsetDateTime expiresAt,
            UserResponse user
    ) {
    }

    public record UserResponse(UUID userId, String email, String role, UUID memberId) {
        static UserResponse from(AppPrincipal principal) {
            return new UserResponse(principal.userId(), principal.email(), principal.role(), principal.memberId());
        }
    }
}
