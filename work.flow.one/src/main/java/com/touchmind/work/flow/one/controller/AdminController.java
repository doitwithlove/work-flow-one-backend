package com.touchmind.work.flow.one.controller;

import com.touchmind.work.flow.one.dto.AdminUserCreateRequest;
import com.touchmind.work.flow.one.dto.AdminUserUpdateRequest;
import com.touchmind.work.flow.one.dto.UserResponse;
import com.touchmind.work.flow.one.service.UserManagementService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserManagementService userManagementService;

    public AdminController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @GetMapping("/users")
    public Flux<UserResponse> users() {
        return userManagementService.listUsers();
    }

    @PostMapping("/users")
    public Mono<UserResponse> createUser(@Valid @RequestBody AdminUserCreateRequest request) {
        return userManagementService.createUser(request);
    }

    @PutMapping("/users/{id}")
    public Mono<UserResponse> updateUser(@PathVariable String id, @Valid @RequestBody AdminUserUpdateRequest request) {
        return userManagementService.updateUser(id, request);
    }
}
