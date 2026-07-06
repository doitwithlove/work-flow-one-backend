package com.touchmind.work.flow.one.manufacturing.dto;

import com.touchmind.work.flow.one.manufacturing.enums.MachineEventType;

import java.time.Instant;
import java.util.Map;

public record MachineEventResponse(

        String id,

        String machineId,

        String partId,

        String operatorId,

        String operatorSessionId,

        MachineEventType eventType,

        String status,

        Map<String, Object> payload,

        Instant receivedAt
) {
}
