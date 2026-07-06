package com.touchmind.work.flow.one.manufacturing.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "operators")
public record Operator(

        @Id
        String id,

        @Indexed(unique = true)
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
