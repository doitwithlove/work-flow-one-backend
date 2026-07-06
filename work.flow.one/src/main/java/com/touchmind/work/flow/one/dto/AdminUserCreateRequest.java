package com.touchmind.work.flow.one.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

public record AdminUserCreateRequest(

        @NotBlank(message = "Username is required")
        @Size(min = 4, max = 64, message = "Username must be between 4 and 64 characters")
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 254, message = "Email must be at most 254 characters")
        String email,

        @Size(max = 120, message = "Full name must be at most 120 characters")
        String fullName,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
        String password,

        @NotEmpty(message = "At least one role must be provided")
        Set<String> roles,

        boolean enabled,

        @Size(max = 32, message = "Phone number must be at most 32 characters")
        String phoneNumber,

        LocalDate birthday,

        @Size(max = 120, message = "Position must be at most 120 characters")
        String position,

        @Size(max = 2048, message = "Profile picture URL must be at most 2048 characters")
        String profilePictureUrl,

        Map<String, String> socialContacts

) {
}
