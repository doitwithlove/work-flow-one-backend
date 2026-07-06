package com.touchmind.work.flow.one.model;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum UserRole {
    SUPER_USER,
    MANAGER,
    SUPERVISOR,
    OPERATOR,
    QUALITY_INSPECTOR,
    USER,
    ADMIN;

    public String authority() {
        return "ROLE_" + name();
    }

    public static Optional<UserRole> fromValue(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT).replace("ROLE_", "");
        return Arrays.stream(values())
                .filter(role -> role.name().equals(normalized))
                .findFirst();
    }
}
