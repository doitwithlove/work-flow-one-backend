package com.touchmind.work.flow.one.service;

import com.touchmind.work.flow.one.dto.LoginRequest;
import com.touchmind.work.flow.one.dto.LoginResponse;
import com.touchmind.work.flow.one.dto.RefreshTokenRequest;
import com.touchmind.work.flow.one.dto.RegisterRequest;
import com.touchmind.work.flow.one.dto.UserResponse;
import reactor.core.publisher.Mono;

public interface AuthenticationService {

    Mono<UserResponse> register(RegisterRequest request);

    Mono<LoginResponse> login(LoginRequest request);

    Mono<LoginResponse> refreshToken(RefreshTokenRequest request);

    Mono<Void> logout(RefreshTokenRequest request);

}
