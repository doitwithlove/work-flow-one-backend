package com.touchmind.work.flow.one.service;

import com.touchmind.work.flow.one.dto.LoginRequest;
import com.touchmind.work.flow.one.dto.LoginResponse;
import com.touchmind.work.flow.one.dto.RefreshTokenRequest;
import com.touchmind.work.flow.one.dto.RegisterRequest;
import com.touchmind.work.flow.one.dto.UserResponse;
import com.touchmind.work.flow.one.exception.DuplicateResourceException;
import com.touchmind.work.flow.one.exception.InvalidCredentialsException;
import com.touchmind.work.flow.one.exception.InvalidTokenException;
import com.touchmind.work.flow.one.model.RefreshToken;
import com.touchmind.work.flow.one.model.Role;
import com.touchmind.work.flow.one.model.User;
import com.touchmind.work.flow.one.repository.RefreshTokenRepository;
import com.touchmind.work.flow.one.repository.UserRepository;
import com.touchmind.work.flow.one.security.JwtService;
import com.touchmind.work.flow.one.security.UserPrincipal;
import org.apache.camel.ProducerTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Set;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ProducerTemplate producerTemplate;

    public AuthenticationServiceImpl(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            ProducerTemplate producerTemplate) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.producerTemplate = producerTemplate;
    }

    @Override
    public Mono<UserResponse> register(RegisterRequest request) {
        String username = request.username().trim().toLowerCase();
        String email = request.email().trim().toLowerCase();

        return userRepository.existsByUsername(username)
                .flatMap(usernameExists -> usernameExists
                        ? Mono.error(new DuplicateResourceException("Username is already registered"))
                        : userRepository.existsByEmail(email))
                .flatMap(emailExists -> emailExists
                        ? Mono.error(new DuplicateResourceException("Email is already registered"))
                        : createUser(username, email, request.password()))
                .map(this::toResponse)
                .doOnSuccess(user -> producerTemplate.asyncSendBody("direct:audit", "registered:" + user.username()));
    }

    @Override
    public Mono<LoginResponse> login(LoginRequest request) {
        String identifier = request.username().trim().toLowerCase();

        return userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase(identifier, identifier)
                .filter(User::isEnabled)
                .filter(user -> passwordEncoder.matches(request.password(), user.getPassword()))
                .switchIfEmpty(Mono.error(new InvalidCredentialsException()))
                .flatMap(this::issueTokens)
                .doOnSuccess(response -> producerTemplate.asyncSendBody("direct:audit", "login:" + identifier));
    }

    @Override
    public Mono<LoginResponse> refreshToken(RefreshTokenRequest request) {
        String token = request.refreshToken();

        if (!jwtService.validate(token)) {
            return Mono.error(new InvalidTokenException("Refresh token is invalid or expired"));
        }

        String username = jwtService.extractUsername(token);
        return refreshTokenRepository.findByToken(token)
                .filter(stored -> stored.getExpiryDate().isAfter(Instant.now()))
                .switchIfEmpty(Mono.error(new InvalidTokenException("Refresh token is not active")))
                .flatMap(stored -> userRepository.findByUsername(username))
                .filter(User::isEnabled)
                .switchIfEmpty(Mono.error(new InvalidTokenException("Refresh token user is not active")))
                .flatMap(user -> refreshTokenRepository.deleteByToken(token).then(issueTokens(user)));
    }

    @Override
    public Mono<Void> logout(RefreshTokenRequest request) {
        return refreshTokenRepository.deleteByToken(request.refreshToken());
    }

    private Mono<User> createUser(String username, String email, String rawPassword) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setEnabled(true);
        user.setRoles(Set.of(Role.USER));
        user.setCreatedAt(Instant.now());

        return userRepository.save(user);
    }

    private Mono<LoginResponse> issueTokens(User user) {
        UserPrincipal principal = new UserPrincipal(user);
        String accessToken = jwtService.generateAccessToken(principal);
        String refreshToken = jwtService.generateRefreshToken(principal);

        RefreshToken entity = new RefreshToken();
        entity.setToken(refreshToken);
        entity.setUsername(user.getUsername());
        entity.setExpiryDate(jwtService.refreshTokenExpiry());

        return refreshTokenRepository.deleteByUsername(user.getUsername())
                .then(refreshTokenRepository.save(entity))
                .thenReturn(new LoginResponse(
                        accessToken,
                        refreshToken,
                        "Bearer",
                        jwtService.accessTokenExpiresInSeconds()));
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
