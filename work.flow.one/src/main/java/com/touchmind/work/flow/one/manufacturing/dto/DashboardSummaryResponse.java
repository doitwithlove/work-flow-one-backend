package com.touchmind.work.flow.one.manufacturing.dto;

public record DashboardSummaryResponse(

        long runningMachines,

        long idleMachines,

        long errorMachines,

        long partsInProcess,

        long partsPassed,

        long partsFailed,

        long partsReadyForNextPhase
) {
}
