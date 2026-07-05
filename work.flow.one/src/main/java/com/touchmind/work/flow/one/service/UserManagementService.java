package com.touchmind.work.flow.one.service;

import com.touchmind.work.flow.one.dto.AdminUserCreateRequest;
import com.touchmind.work.flow.one.dto.AdminUserUpdateRequest;
import com.touchmind.work.flow.one.dto.ChangePasswordRequest;
import com.touchmind.work.flow.one.dto.ProfileUpdateRequest;
import com.touchmind.work.flow.one.dto.UserResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserManagementService {

    Mono<UserResponse> getCurrentUser(String username);

    Mono<UserResponse> updateCurrentUser(String username, ProfileUpdateRequest request);

    Mono<Void> changePassword(String username, ChangePasswordRequest request);

    Flux<UserResponse> listUsers();

    Mono<UserResponse> createUser(AdminUserCreateRequest request);

    Mono<UserResponse> updateUser(String id, AdminUserUpdateRequest request);
}
