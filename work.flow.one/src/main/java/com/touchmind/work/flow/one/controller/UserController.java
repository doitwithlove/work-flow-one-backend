package com.touchmind.work.flow.one.controller;

import com.touchmind.work.flow.one.dto.ChangePasswordRequest;
import com.touchmind.work.flow.one.dto.AdminUserCreateRequest;
import com.touchmind.work.flow.one.dto.AdminUserUpdateRequest;
import com.touchmind.work.flow.one.dto.ProfileUpdateRequest;
import com.touchmind.work.flow.one.dto.UserResponse;
import com.touchmind.work.flow.one.service.UserManagementService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Validated
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserManagementService userManagementService;

    public UserController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @GetMapping("/me")
    public Mono<UserResponse> me(@AuthenticationPrincipal Jwt jwt) {
        return userManagementService.getCurrentUser(jwt.getSubject());
    }

    @PutMapping("/me")
    public Mono<UserResponse> updateProfile(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ProfileUpdateRequest request) {
        return userManagementService.updateCurrentUser(jwt.getSubject(), request);
    }

    @PutMapping("/me/password")
    public Mono<Void> changePassword(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ChangePasswordRequest request) {
        return userManagementService.changePassword(jwt.getSubject(), request);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_USER', 'ADMIN')")
    public Flux<UserResponse> list() {
        return userManagementService.listUsers();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_USER', 'ADMIN')")
    public Mono<UserResponse> create(@Valid @RequestBody AdminUserCreateRequest request) {
        return userManagementService.createUser(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_USER', 'ADMIN')")
    public Mono<UserResponse> update(@PathVariable String id, @Valid @RequestBody AdminUserUpdateRequest request) {
        return userManagementService.updateUser(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_USER', 'ADMIN')")
    public Mono<Void> delete(@PathVariable String id) {
        return userManagementService.deleteUser(id);
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAnyRole('SUPER_USER', 'ADMIN')")
    public Mono<UserResponse> updateRoles(@PathVariable String id, @Valid @RequestBody AdminUserUpdateRequest request) {
        return userManagementService.updateRoles(id, request);
    }
}
