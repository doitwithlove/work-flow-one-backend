package com.touchmind.work.flow.one.manufacturing.dto;

import java.util.List;

public record PartHistoryResponse(

        PartResponse part,

        List<MachineEventResponse> machineEvents,

        List<TestResultResponse> testResults
) {
}
