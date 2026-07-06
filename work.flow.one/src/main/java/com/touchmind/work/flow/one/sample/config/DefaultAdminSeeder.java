package com.touchmind.work.flow.one.sample.config;

import com.touchmind.work.flow.one.model.User;
import com.touchmind.work.flow.one.model.UserRole;
import com.touchmind.work.flow.one.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class DefaultAdminSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DefaultAdminSeeder.class);

    private final DefaultAdminSeederProperties properties;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DefaultAdminSeeder(
            DefaultAdminSeederProperties properties,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.properties = properties;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.isEnabled()) {
            return;
        }

        upsertDevelopmentUsers().block();
    }

    private Mono<Void> upsertDevelopmentUsers() {
        // Development-only seed users. Default passwords must be changed before production.
        List<User> users = List.of(
                createUser("admin", "admin@local.dev", "System Administrator", "admin123", UserRole.ADMIN),
                createUser("manager", "manager@local.dev", "Production Manager", "manager123", UserRole.MANAGER),
                createUser("supervisor", "supervisor@local.dev", "Area Supervisor", "supervisor123", UserRole.SUPERVISOR),
                createUser("operator", "operator@local.dev", "Machine Operator", "operator123", UserRole.OPERATOR),
                createUser("inspector", "inspector@local.dev", "Quality Inspector", "inspector123", UserRole.QUALITY_INSPECTOR));

        List<Mono<User>> saves = new ArrayList<>();
        for (User seedUser : users) {
            saves.add(userRepository.findByUsername(seedUser.getUsername())
                    .flatMap(existing -> {
                        existing.setEmail(seedUser.getEmail());
                        existing.setFullName(seedUser.getFullName());
                        existing.setPassword(seedUser.getPassword());
                        existing.setEnabled(true);
                        existing.setRoles(seedUser.getRoles());
                        existing.setUpdatedAt(Instant.now());
                        return userRepository.save(existing);
                    })
                    .switchIfEmpty(userRepository.save(seedUser)));
        }

        return Mono.when(saves)
                .doOnSuccess(ignored -> log.warn("Development seed users created. Default passwords must be changed before production."));
    }

    private User createUser(String username, String email, String fullName, String password, UserRole role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPassword(passwordEncoder.encode(password));
        user.setEnabled(true);
        user.setRoles(Set.of(role.authority()));
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        return user;
    }
}
