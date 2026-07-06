package com.touchmind.work.flow.one.manufacturing.domain;

import com.touchmind.work.flow.one.manufacturing.enums.MachineEventType;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Document(collection = "machineEvents")
public record MachineEvent(

        @Id
        String id,

        @Indexed
        String machineId,

        @Indexed
        String partId,

        String operatorId,

        String operatorSessionId,

        MachineEventType eventType,

        String status,

        Map<String, Object> payload,

        Instant receivedAt
) {
}
