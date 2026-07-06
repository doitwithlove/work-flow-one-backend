package com.touchmind.work.flow.one.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

public record UserResponse(

        String id,

        String username,

        String email,

        String fullName,

        Set<String> roles,

        boolean enabled,

        boolean active,

        Instant createdAt,

        String phoneNumber,

        LocalDate birthday,

        String position,

        String profilePictureUrl,

        Map<String, String> socialContacts

) {
}
