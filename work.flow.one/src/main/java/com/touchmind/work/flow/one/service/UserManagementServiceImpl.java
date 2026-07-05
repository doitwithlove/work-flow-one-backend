package com.touchmind.work.flow.one.service;

import com.touchmind.work.flow.one.dto.AdminUserCreateRequest;
import com.touchmind.work.flow.one.dto.AdminUserUpdateRequest;
import com.touchmind.work.flow.one.dto.ChangePasswordRequest;
import com.touchmind.work.flow.one.dto.ProfileUpdateRequest;
import com.touchmind.work.flow.one.dto.UserResponse;
import com.touchmind.work.flow.one.exception.ApiException;
import com.touchmind.work.flow.one.exception.DuplicateResourceException;
import com.touchmind.work.flow.one.exception.InvalidCredentialsException;
import com.touchmind.work.flow.one.exception.InvalidTokenException;
import com.touchmind.work.flow.one.model.User;
import com.touchmind.work.flow.one.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserManagementServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Mono<UserResponse> getCurrentUser(String username) {
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new ApiException(HttpStatus.NOT_FOUND, "User not found")))
                .map(this::toResponse);
    }

    @Override
    public Mono<UserResponse> updateCurrentUser(String username, ProfileUpdateRequest request) {
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new ApiException(HttpStatus.NOT_FOUND, "User not found")))
                .map(user -> updateProfile(user, request))
                .flatMap(userRepository::save)
                .map(this::toResponse);
    }

    @Override
    public Mono<Void> changePassword(String username, ChangePasswordRequest request) {
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new ApiException(HttpStatus.NOT_FOUND, "User not found")))
                .flatMap(user -> {
                    if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
                        return Mono.error(new InvalidCredentialsException());
                    }

                    user.setPassword(passwordEncoder.encode(request.newPassword()));
                    user.setUpdatedAt(Instant.now());
                    return userRepository.save(user).then();
                });
    }

    @Override
    public Flux<UserResponse> listUsers() {
        return userRepository.findAll().map(this::toResponse);
    }

    @Override
    public Mono<UserResponse> createUser(AdminUserCreateRequest request) {
        String username = request.username().trim().toLowerCase();
        String email = request.email().trim().toLowerCase();

        return ensureUnique(username, email, null)
                .then(Mono.defer(() -> {
                    User user = new User();
                    user.setUsername(username);
                    user.setEmail(email);
                    user.setPassword(passwordEncoder.encode(request.password()));
                    user.setEnabled(request.enabled());
                    user.setRoles(normalizeRoles(request.roles()));
                    user.setPhoneNumber(trimToNull(request.phoneNumber()));
                    user.setBirthday(request.birthday());
                    user.setPosition(trimToNull(request.position()));
                    user.setProfilePictureUrl(trimToNull(request.profilePictureUrl()));
                    user.setSocialContacts(normalizeContacts(request.socialContacts()));
                    user.setCreatedAt(Instant.now());
                    user.setUpdatedAt(Instant.now());
                    return userRepository.save(user);
                }))
                .map(this::toResponse);
    }

    @Override
    public Mono<UserResponse> updateUser(String id, AdminUserUpdateRequest request) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new ApiException(HttpStatus.NOT_FOUND, "User not found")))
                .flatMap(user -> ensureUnique(
                        valueOrCurrent(request.username(), user.getUsername()),
                        valueOrCurrent(request.email(), user.getEmail()),
                        user.getId())
                        .then(Mono.fromSupplier(() -> applyAdminUpdates(user, request))))
                .flatMap(userRepository::save)
                .map(this::toResponse);
    }

    private Mono<Void> ensureUnique(String username, String email, String currentId) {
        return userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase(username, email)
                .flatMap(existing -> {
                    if (currentId != null && currentId.equals(existing.getId())) {
                        return Mono.empty();
                    }

                    if (username.equalsIgnoreCase(existing.getUsername())) {
                        return Mono.error(new DuplicateResourceException("Username is already registered"));
                    }

                    if (email.equalsIgnoreCase(existing.getEmail())) {
                        return Mono.error(new DuplicateResourceException("Email is already registered"));
                    }

                    return Mono.empty();
                });
    }

    private User updateProfile(User user, ProfileUpdateRequest request) {
        String email = trimToNull(request.email());
        if (email != null) {
            user.setEmail(email.toLowerCase());
        }
        if (request.phoneNumber() != null) {
            user.setPhoneNumber(trimToNull(request.phoneNumber()));
        }
        if (request.birthday() != null) {
            user.setBirthday(request.birthday());
        }
        if (request.position() != null) {
            user.setPosition(trimToNull(request.position()));
        }
        if (request.profilePictureUrl() != null) {
            user.setProfilePictureUrl(trimToNull(request.profilePictureUrl()));
        }
        if (request.socialContacts() != null) {
            user.setSocialContacts(normalizeContacts(request.socialContacts()));
        }

        user.setUpdatedAt(Instant.now());
        return user;
    }

    private User applyAdminUpdates(User user, AdminUserUpdateRequest request) {
        String username = trimToNull(request.username());
        if (username != null) {
            user.setUsername(username.toLowerCase());
        }
        String email = trimToNull(request.email());
        if (email != null) {
            user.setEmail(email.toLowerCase());
        }
        if (request.roles() != null) {
            user.setRoles(normalizeRoles(request.roles()));
        }
        if (request.enabled() != null) {
            user.setEnabled(request.enabled());
        }
        if (request.phoneNumber() != null) {
            user.setPhoneNumber(trimToNull(request.phoneNumber()));
        }
        if (request.birthday() != null) {
            user.setBirthday(request.birthday());
        }
        if (request.position() != null) {
            user.setPosition(trimToNull(request.position()));
        }
        if (request.profilePictureUrl() != null) {
            user.setProfilePictureUrl(trimToNull(request.profilePictureUrl()));
        }
        if (request.socialContacts() != null) {
            user.setSocialContacts(normalizeContacts(request.socialContacts()));
        }

        user.setUpdatedAt(Instant.now());
        return user;
    }

    private Set<String> normalizeRoles(Set<String> roles) {
        return roles.stream()
                .filter(role -> role != null && !role.isBlank())
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role.trim().toUpperCase())
                .collect(Collectors.toSet());
    }

    private Map<String, String> normalizeContacts(Map<String, String> socialContacts) {
        Map<String, String> contacts = new LinkedHashMap<>();
        socialContacts.forEach((key, value) -> {
            if (key != null && !key.isBlank() && value != null && !value.isBlank()) {
                contacts.put(key.trim(), value.trim());
            }
        });
        return contacts;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String valueOrCurrent(String value, String current) {
        return value == null ? current : value.trim().toLowerCase();
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRoles(),
                user.isEnabled(),
                user.getCreatedAt(),
                user.getPhoneNumber(),
                user.getBirthday(),
                user.getPosition(),
                user.getProfilePictureUrl(),
                user.getSocialContacts());
    }
}
