package com.touchmind.work.flow.one.manufacturing.domain;

import com.touchmind.work.flow.one.manufacturing.enums.MachineStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "machines")
public record Machine(

        @Id
        String id,

        @Indexed
        String machineCode,

        String name,

        String type,

        MachineStatus status,

        Instant lastSignalAt
) {
}
