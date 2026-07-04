package com.touchmind.work.flow.one.service;

import com.touchmind.work.flow.one.dto.LoginRequest;
import com.touchmind.work.flow.one.dto.LoginResponse;
import com.touchmind.work.flow.one.dto.RefreshTokenRequest;
import com.touchmind.work.flow.one.dto.RegisterRequest;
import reactor.core.publisher.Mono;

public interface AuthenticationService {

    /**
     * Register a new user.
     *
     * @param request Registration request
     * @return Mono<Void>
     */
    Mono<Void> register(RegisterRequest request);

    /**
     * Authenticate a user and return JWT tokens.
     *
     * @param request Login request
     * @return LoginResponse
     */
    Mono<LoginResponse> login(LoginRequest request);

    /**
     * Generate a new access token using a refresh token.
     *
     * @param request Refresh token request
     * @return LoginResponse
     */
    Mono<LoginResponse> refreshToken(RefreshTokenRequest request);

    /**
     * Logout a user by invalidating the refresh token.
     *
     * @param request Refresh token request
     * @return Mono<Void>
     */
    Mono<Void> logout(RefreshTokenRequest request);

}