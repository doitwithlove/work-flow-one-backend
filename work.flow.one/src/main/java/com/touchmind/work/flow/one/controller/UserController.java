package com.touchmind.work.flow.one.controller;

import com.touchmind.work.flow.one.dto.UserResponse;
import com.touchmind.work.flow.one.exception.ApiException;
import com.touchmind.work.flow.one.model.User;
import com.touchmind.work.flow.one.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public Mono<UserResponse> me(@AuthenticationPrincipal Jwt jwt) {
        return userRepository.findByUsername(jwt.getSubject())
                .switchIfEmpty(Mono.error(new ApiException(HttpStatus.NOT_FOUND, "User not found")))
                .map(this::toResponse);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRoles(),
                user.isEnabled(),
                user.getCreatedAt());
    }
}
