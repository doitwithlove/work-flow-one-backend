package com.touchmind.work.flow.one.superuser.dto;

import com.touchmind.work.flow.one.dto.UserResponse;
import com.touchmind.work.flow.one.manufacturing.dto.OperatorMachineSessionResponse;
import com.touchmind.work.flow.one.manufacturing.dto.PartResponse;
import com.touchmind.work.flow.one.manufacturing.dto.ProductivitySummaryResponse;
import com.touchmind.work.flow.one.manufacturing.dto.TestResultResponse;

import java.util.List;

public record SuperUserDashboardResponse(

        long totalMachines,

        long runningMachines,

        long idleMachines,

        long errorMachines,

        long stoppedMachines,

        long totalParts,

        long partsInProcess,

        long partsWaitingForTest,

        long partsPassed,

        long partsFailed,

        long partsReadyForNextPhase,

        long activeOperators,

        long activeSessions,

        List<ProductivitySummaryResponse> productivitySummary,

        List<MachineProgressResponse> machineProgressList,

        List<ProcessStepProgressResponse> processStepProgressList,

        List<OperatorMachineSessionResponse> activeSessionList,

        List<TestResultResponse> recentTestResults,

        List<PartResponse> failedParts,

        List<PartResponse> partsWaitingForTestList,

        List<UserResponse> operatorList
) {
}
