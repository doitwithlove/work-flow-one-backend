package com.touchmind.work.flow.one.manufacturing.domain;

import com.touchmind.work.flow.one.manufacturing.enums.PartStatus;
import com.touchmind.work.flow.one.manufacturing.enums.TestStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "parts")
public record Part(

        @Id
        String id,

        @Indexed
        String partNumber,

        @Indexed
        String batchNumber,

        String currentStepId,

        String currentMachineId,

        PartStatus status,

        TestStatus testStatus,

        Instant createdAt,

        Instant updatedAt
) {
}
