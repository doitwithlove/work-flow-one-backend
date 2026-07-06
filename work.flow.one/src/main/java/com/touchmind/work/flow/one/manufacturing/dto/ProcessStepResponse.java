package com.touchmind.work.flow.one.manufacturing.dto;

public record ProcessStepResponse(

        String id,

        Integer stepNumber,

        String name,

        String machineType,

        String requiredTestType,

        String nextStepId
) {
}
