package com.touchmind.work.flow.one.manufacturing.dto;

import java.time.Instant;

public record OperatorResponse(

        String id,

        String employeeCode,

        String firstName,

        String lastName,

        String role,

        String skillLevel,

        Boolean active,

        Instant createdAt,

        Instant updatedAt
) {
}
