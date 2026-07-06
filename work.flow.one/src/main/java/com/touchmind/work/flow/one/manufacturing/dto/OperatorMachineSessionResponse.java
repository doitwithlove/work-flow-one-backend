package com.touchmind.work.flow.one.manufacturing.dto;

import com.touchmind.work.flow.one.manufacturing.enums.OperatorSessionStatus;

import java.time.Instant;

public record OperatorMachineSessionResponse(

        String id,

        String operatorId,

        String operatorName,

        String employeeCode,

        String machineId,

        String machineName,

        String shiftId,

        String shiftName,

        Instant loginTime,

        Instant logoutTime,

        OperatorSessionStatus status
) {
}
