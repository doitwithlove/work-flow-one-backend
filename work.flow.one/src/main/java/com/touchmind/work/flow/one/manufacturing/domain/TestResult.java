package com.touchmind.work.flow.one.manufacturing.domain;

import com.touchmind.work.flow.one.manufacturing.enums.TestResultStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "testResults")
public record TestResult(

        @Id
        String id,

        @Indexed
        String partId,

        @Indexed
        String machineId,

        String testType,

        Double expectedValue,

        Double actualValue,

        Double toleranceMin,

        Double toleranceMax,

        TestResultStatus result,

        Instant testedAt
) {
}
