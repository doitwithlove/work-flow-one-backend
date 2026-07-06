package com.touchmind.work.flow.one.superuser.dto;

public record ProcessStepProgressResponse(

        String stepId,

        int stepNumber,

        String stepName,

        long totalParts,

        long completedParts,

        long inProcessParts,

        long failedParts,

        long waitingParts,

        double progressPercentage
) {
}
