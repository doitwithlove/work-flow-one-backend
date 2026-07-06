package com.touchmind.work.flow.one.manufacturing.dto;

import com.touchmind.work.flow.one.manufacturing.enums.TestResultStatus;

import java.time.Instant;

public record TestResultResponse(

        String id,

        String partId,

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
