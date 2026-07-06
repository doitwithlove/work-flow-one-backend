package com.touchmind.work.flow.one.sample.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "alarms")
public record AlarmHistory(

        @Id
        String id,

        @Indexed
        Instant timestamp,

        @Indexed
        String machineId,

        String alarmCode,

        String alarmType,

        String severity,

        String description,

        int durationSec,

        String status,

        String operatorAction

) {
}
