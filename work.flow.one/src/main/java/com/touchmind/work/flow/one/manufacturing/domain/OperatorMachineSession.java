package com.touchmind.work.flow.one.manufacturing.domain;

import com.touchmind.work.flow.one.manufacturing.enums.OperatorSessionStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "operatorMachineSessions")
public record OperatorMachineSession(

        @Id
        String id,

        @Indexed
        String operatorId,

        @Indexed
        String machineId,

        @Indexed
        String shiftId,

        Instant loginTime,

        Instant logoutTime,

        OperatorSessionStatus status
) {
}
