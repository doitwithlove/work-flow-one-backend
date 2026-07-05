package com.touchmind.work.flow.one.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Map;

public record ProfileUpdateRequest(

        @Email(message = "Email must be valid")
        @Size(max = 254, message = "Email must be at most 254 characters")
        String email,

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
