package com.touchmind.work.flow.one.sample.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "machines")
public record MachineProfile(

        @Id
        String id,

        String machineId,

        String name,

        String model,

        String manufacturer,

        String controller,

        String status,

        String location,

        int spindleMaxRpm,

        int feedMaxMmMin,

        List<String> axes,

        Instant createdAt,

        Instant updatedAt
) {
}
