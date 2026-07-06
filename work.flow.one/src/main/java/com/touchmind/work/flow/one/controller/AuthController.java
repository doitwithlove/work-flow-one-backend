package com.touchmind.work.flow.one.controller;

import com.touchmind.work.flow.one.dto.ApiResponse;
import com.touchmind.work.flow.one.dto.LoginRequest;
import com.touchmind.work.flow.one.dto.LoginResponse;
import com.touchmind.work.flow.one.dto.RefreshTokenRequest;
import com.touchmind.work.flow.one.dto.RegisterRequest;
import com.touchmind.work.flow.one.dto.UserResponse;
import com.touchmind.work.flow.one.service.AuthenticationService;
import com.touchmind.work.flow.one.service.UserManagementService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Validated
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final UserManagementService userManagementService;

    public AuthController(AuthenticationService authenticationService, UserManagementService userManagementService) {
        this.authenticationService = authenticationService;
        this.userManagementService = userManagementService;
    }

    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    public Mono<ResponseEntity<ApiResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return authenticationService.register(request)
                .map(user -> response(HttpStatus.CREATED, "User registered successfully", user));
    }

    @Operation(summary = "Login with username and password")
    @PostMapping("/login")
    public Mono<ResponseEntity<ApiResponse>> login(@Valid @RequestBody LoginRequest request) {
        return authenticationService.login(request)
                .map(tokens -> response(HttpStatus.OK, "Login successful", tokens));
    }

    @Operation(summary = "Refresh an access token")
    @PostMapping("/refresh")
    public Mono<ResponseEntity<ApiResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authenticationService.refreshToken(request)
                .map(tokens -> response(HttpStatus.OK, "Token refreshed successfully", tokens));
    }

    @Operation(summary = "Logout and revoke a refresh token")
    @PostMapping("/logout")
    public Mono<ResponseEntity<ApiResponse>> logout(@Valid @RequestBody RefreshTokenRequest request) {
        return authenticationService.logout(request)
                .thenReturn(response(HttpStatus.OK, "Logout successful", null));
    }

    @Operation(summary = "Get the current authenticated user")
    @GetMapping("/me")
    public Mono<ResponseEntity<ApiResponse>> me(@AuthenticationPrincipal Jwt jwt) {
        return userManagementService.getCurrentUser(jwt.getSubject())
                .map(user -> response(HttpStatus.OK, "Current user loaded", user));
    }

    private ResponseEntity<ApiResponse> response(HttpStatus status, String message, Object data) {
        return ResponseEntity.status(status)
                .cacheControl(CacheControl.noStore())
                .body(new ApiResponse(Instant.now(), status.value(), message, data));
    }
}
